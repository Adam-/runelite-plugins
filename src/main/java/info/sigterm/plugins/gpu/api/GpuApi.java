package info.sigterm.plugins.gpu.api;

public interface GpuApi
{
	int VERSION = 1;

	Registration register(Extension extension);

	interface Registration extends AutoCloseable
	{
		@Override
		void close();
	}

	/** Drawing callbacks must restore any OpenGL state they change. */
	interface Extension
	{
		/** Callbacks run on the client thread with GPU's OpenGL context current. */
		default void start()
		{
		}

		/** Return true to replace GPU's normal skybox. Restore any OpenGL state changed here. */
		default boolean drawSkybox(Frame frame)
		{
			return false;
		}

		default void drawSkyboxOverlay(Frame frame)
		{
		}

		default void drawPostScene(Frame frame)
		{
		}

		/**
		 * Optional GLSL declaring applySceneEffect(in GpuSceneInput, inout GpuSceneOutput).
		 */
		default String sceneFragmentShader()
		{
			return null;
		}

		/** Called with sceneProgram bound so the extension can update its shader uniforms. */
		default void updateScene(Frame frame, int sceneProgram)
		{
		}

		default void stop()
		{
		}
	}

	final class Frame
	{
		public final float[] projectionMatrix;
		public final float cameraX;
		public final float cameraY;
		public final float cameraZ;
		public final float cameraPitch;
		public final float cameraYaw;
		public final int viewportWidth;
		public final int viewportHeight;
		public final int drawDistance;
		public final long timeNanos;
		public final float deltaSeconds;

		public Frame(
			float[] projectionMatrix,
			float cameraX,
			float cameraY,
			float cameraZ,
			float cameraPitch,
			float cameraYaw,
			int viewportWidth,
			int viewportHeight,
			int drawDistance,
			long timeNanos,
			float deltaSeconds)
		{
			this.projectionMatrix = projectionMatrix;
			this.cameraX = cameraX;
			this.cameraY = cameraY;
			this.cameraZ = cameraZ;
			this.cameraPitch = cameraPitch;
			this.cameraYaw = cameraYaw;
			this.viewportWidth = viewportWidth;
			this.viewportHeight = viewportHeight;
			this.drawDistance = drawDistance;
			this.timeNanos = timeNanos;
			this.deltaSeconds = deltaSeconds;
		}
	}

}
