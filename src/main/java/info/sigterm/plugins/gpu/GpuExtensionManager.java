package info.sigterm.plugins.gpu;

import info.sigterm.plugins.gpu.api.GpuApi;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;

@Singleton
@Slf4j
final class GpuExtensionManager implements GpuApi
{
	private final Client client;
	private final ClientThread clientThread;
	private Extension extension;
	private String sceneSource;
	private boolean rendererActive;
	private boolean started;
	private boolean failed;
	private boolean sceneRejected;
	private Runnable rebuildSceneProgram;
	private boolean rebuildQueued;
	private long lastFrameNanos;

	@Inject
	private GpuExtensionManager(Client client, ClientThread clientThread)
	{
		this.client = client;
		this.clientThread = clientThread;
	}

	@Override
	public synchronized Registration register(Extension value)
	{
		if (extension != null)
		{
			throw new IllegalStateException("A GPU extension is already registered");
		}
		Objects.requireNonNull(value);
		String source = value.sceneFragmentShader();
		if (source != null && source.trim().isEmpty())
		{
			source = null;
		}
		extension = value;
		sceneSource = source;
		if (rendererActive)
		{
			clientThread.invokeLater(() ->
			{
				start();
				requestRebuild();
			});
		}
		return () -> unregister(value);
	}

	void startRenderer(Runnable rebuildSceneProgram)
	{
		this.rebuildSceneProgram = Objects.requireNonNull(rebuildSceneProgram);
		rendererActive = true;
		failed = false;
		sceneRejected = false;
		rebuildQueued = false;
		lastFrameNanos = 0;
		start();
	}

	void stopRenderer()
	{
		rendererActive = false;
		stop();
		rebuildSceneProgram = null;
		rebuildQueued = false;
		lastFrameNanos = 0;
	}

	Frame beginFrame(float[] projectionMatrix, float cameraX, float cameraY, float cameraZ,
		float cameraPitch, float cameraYaw, int viewportWidth, int viewportHeight, int drawDistance)
	{
		long now = System.nanoTime();
		float delta = lastFrameNanos == 0 ? 0 : Math.min((now - lastFrameNanos) / 1_000_000_000f, .25f);
		lastFrameNanos = now;
		return new Frame(projectionMatrix, cameraX, cameraY, cameraZ, cameraPitch, cameraYaw,
			viewportWidth, viewportHeight, drawDistance, now, delta);
	}

	boolean drawSkybox(Frame frame)
	{
		if (!canDraw())
		{
			return false;
		}
		try
		{
			return extension.drawSkybox(frame);
		}
		catch (Throwable ex)
		{
			fail(ex);
			return false;
		}
	}

	void drawSkyboxOverlay(Frame frame)
	{
		if (!canDraw())
		{
			return;
		}
		try
		{
			extension.drawSkyboxOverlay(frame);
		}
		catch (Throwable ex)
		{
			fail(ex);
		}
	}

	void drawPostScene(Frame frame)
	{
		if (!canDraw())
		{
			return;
		}
		try
		{
			extension.drawPostScene(frame);
		}
		catch (Throwable ex)
		{
			fail(ex);
		}
	}

	String sceneSource()
	{
		return hasSceneEffect() ? sceneSource : "";
	}

	String sceneConfig()
	{
		return hasSceneEffect() ? "#define GPU_API_SCENE_EFFECT\n" : "";
	}

	boolean hasSceneEffect()
	{
		return canDraw() && sceneSource != null && !sceneRejected;
	}

	void rejectSceneEffect(Throwable cause)
	{
		sceneRejected = true;
		if (extension != null)
		{
			log.warn("Rejecting GPU scene effect from {}", extension.getClass().getName(), cause);
		}
	}

	void updateScene(int sceneProgram, Frame frame)
	{
		if (!hasSceneEffect())
		{
			return;
		}
		try
		{
			extension.updateScene(frame, sceneProgram);
		}
		catch (Throwable ex)
		{
			fail(ex);
		}
	}

	private boolean canDraw()
	{
		return rendererActive && extension != null && started && !failed;
	}

	private void start()
	{
		if (!rendererActive || extension == null || started || failed)
		{
			return;
		}
		try
		{
			extension.start();
			started = true;
		}
		catch (Throwable ex)
		{
			failed = true;
			log.warn("Unable to start GPU extension {}", extension.getClass().getName(), ex);
			try
			{
				extension.stop();
			}
			catch (Throwable stopEx)
			{
				log.debug("Error cleaning up GPU extension {}", extension.getClass().getName(), stopEx);
			}
		}
	}

	private void stop()
	{
		if (!started || extension == null)
		{
			return;
		}
		try
		{
			extension.stop();
		}
		catch (Throwable ex)
		{
			log.debug("Error stopping GPU extension {}", extension.getClass().getName(), ex);
		}
		started = false;
	}

	private void fail(Throwable cause)
	{
		failed = true;
		log.warn("Disabling GPU extension {} after a render error", extension.getClass().getName(), cause);
		stop();
		requestRebuild();
	}

	private void unregister(Extension value)
	{
		Runnable unregister = () ->
		{
			if (extension != value)
			{
				return;
			}
			stop();
			extension = null;
			sceneSource = null;
			failed = false;
			sceneRejected = false;
			requestRebuild();
		};
		if (client.isClientThread())
		{
			unregister.run();
		}
		else
		{
			clientThread.invokeLater(unregister);
		}
	}

	private void requestRebuild()
	{
		if (!rendererActive || rebuildSceneProgram == null || rebuildQueued)
		{
			return;
		}
		rebuildQueued = true;
		clientThread.invokeLater(() ->
		{
			rebuildQueued = false;
			if (rendererActive && rebuildSceneProgram != null)
			{
				rebuildSceneProgram.run();
			}
		});
	}
}
