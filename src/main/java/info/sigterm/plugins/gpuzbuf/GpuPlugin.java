/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package info.sigterm.plugins.gpuzbuf;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;
import com.google.inject.Provides;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.BufferProvider;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.FloatProjection;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Model;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Projection;
import net.runelite.api.Renderable;
import net.runelite.api.Scene;
import net.runelite.api.TextureProvider;
import net.runelite.api.TileObject;
import net.runelite.api.WorldEntity;
import net.runelite.api.WorldView;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.PostClientTick;
import net.runelite.api.hooks.DrawCallbacks;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import info.sigterm.plugins.gpuzbuf.config.AntiAliasingMode;
import info.sigterm.plugins.gpuzbuf.config.UIScalingMode;
import info.sigterm.plugins.gpuzbuf.template.Template;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.DrawManager;
import net.runelite.rlawt.AWTContext;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL43C.*;
import static org.lwjgl.opengl.GL45C.GL_ZERO_TO_ONE;
import static org.lwjgl.opengl.GL45C.glClipControl;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.Configuration;

@PluginDescriptor(
	name = "GPU (experimental)",
	configName = "GpuExperimental",
	description = "Utilizes the GPU",
	enabledByDefault = false,
	tags = {"fog", "draw distance"},
	loadInSafeMode = false,
	conflicts = "GPU"
)
@Slf4j
public class GpuPlugin extends Plugin implements DrawCallbacks
{
	static final int MAX_DISTANCE = 184;
	static final int MAX_FOG_DEPTH = 100;
	static final int SCENE_OFFSET = (Constants.EXTENDED_SCENE_SIZE - Constants.SCENE_SIZE) / 2; // offset for sxy -> msxy
	private static final int UNIFORM_BUFFER_SIZE = 5 * Float.BYTES;
	private static final int NUM_ZONES = Constants.EXTENDED_SCENE_SIZE >> 3;

	@Inject
	private Client client;

	@Inject
	private ClientUI clientUI;

	@Inject
	private ClientThread clientThread;

	@Inject
	private GpuPluginConfig config;

	@Inject
	private TextureManager textureManager;

	@Inject
	private SceneUploader sceneUploader;

	@Inject
	private FacePrioritySorter facePrioritySorter;

	@Inject
	private DrawManager drawManager;

	@Inject
	private PluginManager pluginManager;

	private Canvas canvas;
	private AWTContext awtContext;
	private Callback debugCallback;

	private boolean lwjglInitted = false;
	private GLCapabilities glCapabilities;

	static final Shader PROGRAM = new Shader()
		.add(GL_VERTEX_SHADER, "vert.glsl")
		.add(GL_GEOMETRY_SHADER, "geom.glsl")
		.add(GL_FRAGMENT_SHADER, "frag.glsl");

	static final Shader UI_PROGRAM = new Shader()
		.add(GL_VERTEX_SHADER, "vertui.glsl")
		.add(GL_FRAGMENT_SHADER, "fragui.glsl");

	private int glProgram;
	private int glUiProgram;

	private int interfaceTexture;
	private int interfacePbo;

	private int vaoUiHandle;
	private int vboUiHandle;

	private int fboScene;
	private int rboColorBuffer;
	private int rboDepthBuffer;

	private int textureArrayId;

	private final GLBuffer glUniformBuffer = new GLBuffer("uniform buffer");

	private int lastCanvasWidth;
	private int lastCanvasHeight;
	private int lastStretchedCanvasWidth;
	private int lastStretchedCanvasHeight;
	private AntiAliasingMode lastAntiAliasingMode;
	private int lastAnisotropicFilteringLevel = -1;

	private GpuFloatBuffer uniformBuffer;

	private int minLevel, level, maxLevel;
	private Set<Integer> hideRoofIds;

	static class SceneContext {
		final int sizeX, sizeZ;
		Zone[][] zones;
		VAOList vaoO, vaoA;
		VAOList vaoP;

		SceneContext(int sizeX, int sizeZ) {
			this.sizeX = sizeX;
			this.sizeZ = sizeZ;
			zones = new Zone[sizeX][sizeZ];
			for (int x = 0; x < sizeX; ++x) {
				for (int z = 0; z < sizeZ; ++z) {
					zones[x][z] = new Zone();
				}
			}
			vaoO = new VAOList();
			vaoA = new VAOList();
			vaoP = new VAOList();
		}
	};

	SceneContext root;
	SceneContext[] subs;

	SceneContext context(Scene scene) {
		int wvid = scene.getWorldViewId();
		if (wvid == -1) return root;
		return subs[wvid];
	}

	private Zone[][] nextZones;
	private Map<Integer, Integer> nextRoofChanges;

	// Uniforms
	private int uniColorBlindMode;
	private int uniUiColorBlindMode;
	private int uniUseFog;
	private int uniFogColor;
	private int uniFogDepth;
	private int uniDrawDistance;
	private int uniExpandedMapLoadingChunks;
	private int uniWorldProj;
	private int uniEntityProj;
	private int uniBrightness;
	private int uniTex;
	private int uniTexSamplingMode;
	private int uniTexSourceDimensions;
	private int uniTexTargetDimensions;
	private int uniUiAlphaOverlay;
	private int uniTextures;
	private int uniTextureAnimations;
	private int uniBlockMain;
	private int uniSmoothBanding;
	private int uniTextureLightMode;
	private int uniTick;
	private int uniBase;

	@Override
	protected void startUp()
	{
		root = new SceneContext(NUM_ZONES, NUM_ZONES);
		subs = new SceneContext[2048];
		clientThread.invoke(() ->
		{
			try
			{
				fboScene = -1;
				lastAnisotropicFilteringLevel = -1;

				AWTContext.loadNatives();

				canvas = client.getCanvas();

				synchronized (canvas.getTreeLock())
				{
					if (!canvas.isValid())
					{
						return false;
					}

					awtContext = new AWTContext(canvas);
					awtContext.configurePixelFormat(0, 0, 0);
				}

				awtContext.createGLContext();

				canvas.setIgnoreRepaint(true);

				// lwjgl defaults to lwjgl- + user.name, but this breaks if the username would cause an invalid path
				// to be created.
				Configuration.SHARED_LIBRARY_EXTRACT_DIRECTORY.set("lwjgl-rl");

				glCapabilities = GL.createCapabilities();

				log.info("Using device: {}", glGetString(GL_RENDERER));
				log.info("Using driver: {}", glGetString(GL_VERSION));

				if (!glCapabilities.OpenGL31)
				{
					throw new RuntimeException("OpenGL 3.1 is required but not available");
				}

				lwjglInitted = true;

				checkGLErrors();
				if (log.isDebugEnabled() && glCapabilities.glDebugMessageControl != 0)
				{
					debugCallback = GLUtil.setupDebugMessageCallback();
					if (debugCallback != null)
					{
						// [LWJGL] OpenGL debug message
						//	ID: 0x20071
						//	Source: API
						//	Type: OTHER
						//	Severity: NOTIFICATION
						//	Message: Buffer detailed info: Buffer object 4 (bound to GL_PIXEL_UNPACK_BUFFER_ARB, usage hint is GL_STREAM_DRAW) has been mapped WRITE_ONLY in SYSTEM HEAP memory (fast).
						glDebugMessageControl(GL_DEBUG_SOURCE_API, GL_DEBUG_TYPE_OTHER,
							GL_DONT_CARE, 0x20071, false);

						// [LWJGL] OpenGL debug message
						//	ID: 0x20052
						//	Source: API
						//	Type: PERFORMANCE
						//	Severity: MEDIUM
						//	Message: Pixel-path performance warning: Pixel transfer is synchronized with 3D rendering.
						glDebugMessageControl(GL_DEBUG_SOURCE_API, GL_DEBUG_TYPE_PERFORMANCE,
							GL_DONT_CARE, 0x20052, false);
					}
				}

				setupSyncMode();

				initBuffers();
				initVao();
				try
				{
					initProgram();
				}
				catch (ShaderException ex)
				{
					throw new RuntimeException(ex);
				}
				initInterfaceTexture();
				initUniformBuffer();
				if (glCapabilities.OpenGL45)
				{
					glClipControl(GL_LOWER_LEFT, GL_ZERO_TO_ONE); // 1 near 0 far
				}

				client.setDrawCallbacks(this);
				client.setGpuFlags(DrawCallbacks.GPU
//					| (computeMode == ComputeMode.NONE ? 0 : DrawCallbacks.HILLSKEW)
					| (config.removeVertexSnapping() ? DrawCallbacks.NO_VERTEX_SNAPPING : 0)
					| DrawCallbacks.ZBUF
//					|16
				);
				client.setExpandedMapLoading(config.expandedMapLoadingChunks());

				// force rebuild of main buffer provider to enable alpha channel
				client.resizeCanvas();

				lastCanvasWidth = lastCanvasHeight = -1;
				lastStretchedCanvasWidth = lastStretchedCanvasHeight = -1;
				lastAntiAliasingMode = null;

				textureArrayId = -1;

				if (client.getGameState() == GameState.LOGGED_IN)
				{
					startupWorldLoad();
				}

				checkGLErrors();
			}
			catch (Throwable e)
			{
				log.error("Error starting GPU plugin", e);

				SwingUtilities.invokeLater(() ->
				{
					try
					{
						pluginManager.setPluginEnabled(this, false);
						pluginManager.stopPlugin(this);
					}
					catch (PluginInstantiationException ex)
					{
						log.error("error stopping plugin", ex);
					}
				});

				shutDown();
			}
			return true;
		});
	}

	private void startupWorldLoad() {
		WorldView root = client.getTopLevelWorldView();
		Scene scene = root.getScene();
		loadScene(root, scene);
		swapScene(scene);

		for (WorldEntity subEntity : root.worldEntities()) {
			WorldView sub = subEntity.getWorldView();
			log.debug("loading subscene {}", sub.getId());
			loadSubScene(sub, sub.getScene());
			swapSub(sub.getScene());
		}
	}

	@Override
	protected void shutDown()
	{
		clientThread.invoke(() ->
		{
			client.setGpuFlags(0);
			client.setDrawCallbacks(null);
			client.setUnlockedFps(false);
			client.setExpandedMapLoading(0);

//			sceneUploader.releaseSortingBuffers();

			if (lwjglInitted)
			{
				if (textureArrayId != -1)
				{
					textureManager.freeTextureArray(textureArrayId);
					textureArrayId = -1;
				}

				destroyGlBuffer(glUniformBuffer);

				shutdownInterfaceTexture();
				shutdownProgram();
				shutdownVao();
				shutdownBuffers();
				shutdownFbo();
			}

			// this must shutdown after the clgl buffers are freed
//			openCLManager.cleanup();

			if (awtContext != null)
			{
				awtContext.destroy();
				awtContext = null;
			}

			if (debugCallback != null)
			{
				debugCallback.free();
				debugCallback = null;
			}

			glCapabilities = null;

			// force main buffer provider rebuild to turn off alpha channel
			client.resizeCanvas();
		});
	}

	@Provides
	GpuPluginConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GpuPluginConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (configChanged.getGroup().equals(GpuPluginConfig.GROUP))
		{
			if (configChanged.getKey().equals("unlockFps")
				|| configChanged.getKey().equals("vsyncMode")
				|| configChanged.getKey().equals("fpsTarget"))
			{
				log.debug("Rebuilding sync mode");
				clientThread.invokeLater(this::setupSyncMode);
			}
			else if (configChanged.getKey().equals("expandedMapLoadingChunks"))
			{
				clientThread.invokeLater(() ->
				{
					client.setExpandedMapLoading(config.expandedMapLoadingChunks());
					if (client.getGameState() == GameState.LOGGED_IN)
					{
						client.setGameState(GameState.LOADING);
					}
				});
			}
			else if (configChanged.getKey().equals("removeVertexSnapping"))
			{
				log.debug("Toggle {}", configChanged.getKey());
				client.setGpuFlags(DrawCallbacks.GPU
//					| (computeMode == ComputeMode.NONE ? 0 : DrawCallbacks.HILLSKEW)
					| (config.removeVertexSnapping() ? DrawCallbacks.NO_VERTEX_SNAPPING : 0)
					| DrawCallbacks.ZBUF
				);
			}
		}
	}

	private void setupSyncMode()
	{
		final boolean unlockFps = config.unlockFps();
		client.setUnlockedFps(unlockFps);

		// Without unlocked fps, the client manages sync on its 20ms timer
		GpuPluginConfig.SyncMode syncMode = unlockFps
			? this.config.syncMode()
			: GpuPluginConfig.SyncMode.OFF;

		int swapInterval = 0;
		switch (syncMode)
		{
			case ON:
				swapInterval = 1;
				break;
			case OFF:
				swapInterval = 0;
				break;
			case ADAPTIVE:
				swapInterval = -1;
				break;
		}

		int actualSwapInterval = awtContext.setSwapInterval(swapInterval);
		if (actualSwapInterval != swapInterval)
		{
			log.info("unsupported swap interval {}, got {}", swapInterval, actualSwapInterval);
		}

		client.setUnlockedFpsTarget(actualSwapInterval == 0 ? config.fpsTarget() : 0);
		checkGLErrors();
	}

	private Template createTemplate()
	{
		Template template = new Template();
		template.addInclude(GpuPlugin.class);
		return template;
	}

	private void initProgram() throws ShaderException
	{
		Template template = createTemplate();
		glProgram = PROGRAM.compile(template);
		glUiProgram = UI_PROGRAM.compile(template);

		initUniforms();
	}

	private void initUniforms()
	{
		uniWorldProj = glGetUniformLocation(glProgram, "worldProj");
		uniEntityProj = glGetUniformLocation(glProgram, "entityProj");
		uniBrightness = glGetUniformLocation(glProgram, "brightness");
		uniSmoothBanding = glGetUniformLocation(glProgram, "smoothBanding");
		uniUseFog = glGetUniformLocation(glProgram, "useFog");
		uniFogColor = glGetUniformLocation(glProgram, "fogColor");
		uniFogDepth = glGetUniformLocation(glProgram, "fogDepth");
		uniDrawDistance = glGetUniformLocation(glProgram, "drawDistance");
		uniExpandedMapLoadingChunks = glGetUniformLocation(glProgram, "expandedMapLoadingChunks");
		uniColorBlindMode = glGetUniformLocation(glProgram, "colorBlindMode");
		uniTextureLightMode = glGetUniformLocation(glProgram, "textureLightMode");
		uniTick = glGetUniformLocation(glProgram, "tick");
		uniBlockMain = glGetUniformBlockIndex(glProgram, "uniforms");
		uniTextures = glGetUniformLocation(glProgram, "textures");
		uniTextureAnimations = glGetUniformLocation(glProgram, "textureAnimations");
		uniBase = glGetUniformLocation(glProgram, "base");

		uniTex = glGetUniformLocation(glUiProgram, "tex");
		uniTexSamplingMode = glGetUniformLocation(glUiProgram, "samplingMode");
		uniTexTargetDimensions = glGetUniformLocation(glUiProgram, "targetDimensions");
		uniTexSourceDimensions = glGetUniformLocation(glUiProgram, "sourceDimensions");
		uniUiColorBlindMode = glGetUniformLocation(glUiProgram, "colorBlindMode");
		uniUiAlphaOverlay = glGetUniformLocation(glUiProgram, "alphaOverlay");
	}

	private void shutdownProgram()
	{
		glDeleteProgram(glProgram);
		glProgram = 0;

		glDeleteProgram(glUiProgram);
		glUiProgram = 0;
	}

	private void initVao()
	{
//		vaoO = new VAOList();
//		vaoA = new VAOList();

		// Create UI VAO
		vaoUiHandle = glGenVertexArrays();
		// Create UI buffer
		vboUiHandle = glGenBuffers();
		glBindVertexArray(vaoUiHandle);

		FloatBuffer vboUiBuf = GpuFloatBuffer.allocateDirect(5 * 4);
		vboUiBuf.put(new float[]{
			// positions     // texture coords
			1f, 1f, 0.0f, 1.0f, 0f, // top right
			1f, -1f, 0.0f, 1.0f, 1f, // bottom right
			-1f, -1f, 0.0f, 0.0f, 1f, // bottom left
			-1f, 1f, 0.0f, 0.0f, 0f  // top left
		});
		vboUiBuf.rewind();
		glBindBuffer(GL_ARRAY_BUFFER, vboUiHandle);
		glBufferData(GL_ARRAY_BUFFER, vboUiBuf, GL_STATIC_DRAW);

		// position attribute
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
		glEnableVertexAttribArray(0);

		// texture coord attribute
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
		glEnableVertexAttribArray(1);

		// unbind VAO/VBO
		//glBindVertexArray(0);//XXX required for macos
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

//	private int initVao(VBO vbo) {
//		int vao = glGenVertexArrays();
//		glBindVertexArray(vao);
//
//		glBindBuffer(GL_ARRAY_BUFFER, vbo.bufId);
//		glBufferData(GL_ARRAY_BUFFER, vbo.size, GL_DYNAMIC_DRAW);
//
//		glEnableVertexAttribArray(0);
//		glVertexAttribPointer(0, 3, GL_FLOAT, false, 32, 0);
//
//		glVertexAttribI3i(1, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
//
//		glEnableVertexAttribArray(2);
//		glVertexAttribIPointer(2, 1, GL_INT, 32, 12);
//
//		glEnableVertexAttribArray(3);
//		glVertexAttribIPointer(3, 4, GL_INT, 32, 16);
//
//		glBindBuffer(GL_ARRAY_BUFFER,0);
//		glBindVertexArray(0);
//
//		return vao;
//	}

	private void shutdownVao()
	{
//		if (vaoO != null)
//		{
//			vaoO.destroy();
//			vaoO = null;
//		}
//
//		if (vaoA != null)
//		{
//			vaoA.destroy();
//			vaoA = null;
//		}

		glDeleteBuffers(vboUiHandle);
		vboUiHandle = 0;

		glDeleteVertexArrays(vaoUiHandle);
		vaoUiHandle = 0;
	}

	private void initBuffers()
	{
//		initGlBuffer(sceneVertexBuffer);
//		initGlBuffer(sceneUvBuffer);
//		initGlBuffer(sceneAlphaVertexBuffer);
//		initGlBuffer(sceneAlphaTexBuffer);
//		initGlBuffer(tmpVertexBuffer);
//		initGlBuffer(tmpUvBuffer);
//		initGlBuffer(tmpModelBufferLarge);
//		initGlBuffer(tmpModelBufferSmall);
//		initGlBuffer(tmpModelBufferUnordered);
//		initGlBuffer(tmpOutBuffer);
//		initGlBuffer(tmpOutUvBuffer);
//		initGlBuffer(glOpaqueVertexBuffer);
//		initGlBuffer(glOpaqueTexBuffer);
		uniformBuffer = new GpuFloatBuffer(UNIFORM_BUFFER_SIZE);
//		VBO.active.init0();
//		VBO.activeA.init0();
	}

	private void initGlBuffer(GLBuffer glBuffer)
	{
		glBuffer.glBufferId = glGenBuffers();
	}

	private void shutdownBuffers()
	{
		uniformBuffer = null;
//		destroyGlBuffer(sceneVertexBuffer);
//		destroyGlBuffer(sceneUvBuffer);
//		destroyGlBuffer(sceneAlphaVertexBuffer);
//		destroyGlBuffer(sceneAlphaTexBuffer);
//		destroyGlBuffer(tmpVertexBuffer);
//		destroyGlBuffer(tmpUvBuffer);
//		destroyGlBuffer(tmpModelBufferLarge);
//		destroyGlBuffer(tmpModelBufferSmall);
//		destroyGlBuffer(tmpModelBufferUnordered);
//		destroyGlBuffer(tmpOutBuffer);
//		destroyGlBuffer(tmpOutUvBuffer);
//		destroyGlBuffer(glOpaqueVertexBuffer);
//		destroyGlBuffer(glOpaqueTexBuffer);
	}

	private void destroyGlBuffer(GLBuffer glBuffer)
	{
		if (glBuffer.glBufferId != -1)
		{
			glDeleteBuffers(glBuffer.glBufferId);
			glBuffer.glBufferId = -1;
		}
		glBuffer.size = -1;
	}

	private void initInterfaceTexture()
	{
		interfacePbo = glGenBuffers();

		interfaceTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, interfaceTexture);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	private void shutdownInterfaceTexture()
	{
		glDeleteBuffers(interfacePbo);
		glDeleteTextures(interfaceTexture);
		interfaceTexture = -1;
	}

	private void initUniformBuffer()
	{
		initGlBuffer(glUniformBuffer);

		updateBuffer(glUniformBuffer, GL_UNIFORM_BUFFER, UNIFORM_BUFFER_SIZE, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}

	private void initFbo(int width, int height, int aaSamples)
	{
		final GraphicsConfiguration graphicsConfiguration = clientUI.getGraphicsConfiguration();
		final AffineTransform transform = graphicsConfiguration.getDefaultTransform();

		width = getScaledValue(transform.getScaleX(), width);
		height = getScaledValue(transform.getScaleY(), height);

		if (aaSamples > 0)
		{
			glEnable(GL_MULTISAMPLE);
		}
		else
		{
			glDisable(GL_MULTISAMPLE);
		}

		// Create and bind the FBO
		fboScene = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, fboScene);

		// Color render buffer
		rboColorBuffer = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, rboColorBuffer);
		glRenderbufferStorageMultisample(GL_RENDERBUFFER, aaSamples, GL_RGBA, width, height);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, rboColorBuffer);

		// Depth render buffer
		rboDepthBuffer = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, rboDepthBuffer);
		glRenderbufferStorageMultisample(GL_RENDERBUFFER, aaSamples, GL_DEPTH_COMPONENT32F, width, height);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboDepthBuffer);

		int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
		if (status != GL_FRAMEBUFFER_COMPLETE)
		{
			throw new RuntimeException("FBO is incomplete. status: " + status);
		}

		// Reset
		glBindFramebuffer(GL_FRAMEBUFFER, awtContext.getFramebuffer(false));
		glBindRenderbuffer(GL_RENDERBUFFER, 0);
	}

	private void shutdownFbo()
	{
		if (fboScene != -1)
		{
			glDeleteFramebuffers(fboScene);
			fboScene = -1;
		}

		if (rboColorBuffer != 0)
		{
			glDeleteRenderbuffers(rboColorBuffer);
			rboColorBuffer = 0;
		}

		if (rboDepthBuffer != 0)
		{
			glDeleteRenderbuffers(rboDepthBuffer);
			rboDepthBuffer = 0;
		}
	}

	private Projection lastProjection;

	private void updateEntityProject(Projection projection)
	{
		if (lastProjection != projection)
		{
			float[] p = projection instanceof FloatProjection ? ((FloatProjection) projection).getProjection() : Mat4.identity();
			glUniformMatrix4fv(uniEntityProj, false, p);
			lastProjection = projection;
		}
	}

	@Override
	public void prepareSceneDraw(Scene scene,
								 float cameraX, float cameraY, float cameraZ, float cameraPitch, float cameraYaw,
								 int minLevel, int level, int maxLevel, Set<Integer> hideRoofIds)
	{
		this.minLevel = minLevel;
		this.level = level;
		this.maxLevel = maxLevel;
		this.hideRoofIds = hideRoofIds;

		scene.setDrawDistance(getDrawDistance());

		// UBO
		uniformBuffer.clear();
		uniformBuffer
			.put(cameraYaw)
			.put(cameraPitch)
			.put(cameraX)
			.put(cameraY)
			.put(cameraZ);
		uniformBuffer.flip();

		updateBuffer(glUniformBuffer, GL_UNIFORM_BUFFER, uniformBuffer.getBuffer(), GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		uniformBuffer.clear();

		glBindBufferBase(GL_UNIFORM_BUFFER, 0, glUniformBuffer.glBufferId);

		checkGLErrors();

		final int canvasHeight = client.getCanvasHeight();
		final int canvasWidth = client.getCanvasWidth();

		final int viewportHeight = client.getViewportHeight();
		final int viewportWidth = client.getViewportWidth();

		// Setup FBO and anti-aliasing
		{
			final AntiAliasingMode antiAliasingMode = config.antiAliasingMode();
			final Dimension stretchedDimensions = client.getStretchedDimensions();

			final int stretchedCanvasWidth = client.isStretchedEnabled() ? stretchedDimensions.width : canvasWidth;
			final int stretchedCanvasHeight = client.isStretchedEnabled() ? stretchedDimensions.height : canvasHeight;

			// Re-create fbo
			if (lastStretchedCanvasWidth != stretchedCanvasWidth
				|| lastStretchedCanvasHeight != stretchedCanvasHeight
				|| lastAntiAliasingMode != antiAliasingMode)
			{
				shutdownFbo();

				// Bind default FBO to check whether anti-aliasing is forced
				glBindFramebuffer(GL_FRAMEBUFFER, awtContext.getFramebuffer(false));
				final int forcedAASamples = glGetInteger(GL_SAMPLES);
				final int maxSamples = glGetInteger(GL_MAX_SAMPLES);
				final int samples = forcedAASamples != 0 ? forcedAASamples :
					Math.min(antiAliasingMode.getSamples(), maxSamples);

				log.debug("AA samples: {}, max samples: {}, forced samples: {}", samples, maxSamples, forcedAASamples);

				initFbo(stretchedCanvasWidth, stretchedCanvasHeight, samples);

				lastStretchedCanvasWidth = stretchedCanvasWidth;
				lastStretchedCanvasHeight = stretchedCanvasHeight;
				lastAntiAliasingMode = antiAliasingMode;
			}

			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboScene);
		}

		// Clear scene
		int sky = client.getSkyboxColor();
		glClearColor((sky >> 16 & 0xFF) / 255f, (sky >> 8 & 0xFF) / 255f, (sky & 0xFF) / 255f, 1f);
		glClearDepthf(0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		// Setup anisotropic filtering
		final int anisotropicFilteringLevel = config.anisotropicFilteringLevel();

		if (textureArrayId != -1 && lastAnisotropicFilteringLevel != anisotropicFilteringLevel)
		{
			textureManager.setAnisotropicFilteringLevel(textureArrayId, anisotropicFilteringLevel);
			lastAnisotropicFilteringLevel = anisotropicFilteringLevel;
		}

		// Setup viewport
		int renderWidthOff = client.getViewportXOffset();
		int renderHeightOff = client.getViewportYOffset();
		int renderCanvasHeight = canvasHeight;
		int renderViewportHeight = viewportHeight;
		int renderViewportWidth = viewportWidth;
		if (client.isStretchedEnabled())
		{
			Dimension dim = client.getStretchedDimensions();
			renderCanvasHeight = dim.height;

			double scaleFactorY = dim.getHeight() / canvasHeight;
			double scaleFactorX = dim.getWidth() / canvasWidth;

			// Pad the viewport a little because having ints for our viewport dimensions can introduce off-by-one errors.
			final int padding = 1;

			// Ceil the sizes because even if the size is 599.1 we want to treat it as size 600 (i.e. render to the x=599 pixel).
			renderViewportHeight = (int) Math.ceil(scaleFactorY * (renderViewportHeight)) + padding * 2;
			renderViewportWidth = (int) Math.ceil(scaleFactorX * (renderViewportWidth)) + padding * 2;

			// Floor the offsets because even if the offset is 4.9, we want to render to the x=4 pixel anyway.
			renderHeightOff = (int) Math.floor(scaleFactorY * (renderHeightOff)) - padding;
			renderWidthOff = (int) Math.floor(scaleFactorX * (renderWidthOff)) - padding;
		}

		glDpiAwareViewport(renderWidthOff, renderCanvasHeight - renderViewportHeight - renderHeightOff, renderViewportWidth, renderViewportHeight);

		glUseProgram(glProgram);

		// Setup uniforms
		final int drawDistance = getDrawDistance();
		final int fogDepth = config.fogDepth();
		glUniform1i(uniUseFog, fogDepth > 0 ? 1 : 0);
		glUniform4f(uniFogColor, (sky >> 16 & 0xFF) / 255f, (sky >> 8 & 0xFF) / 255f, (sky & 0xFF) / 255f, 1f);
		glUniform1i(uniFogDepth, fogDepth);
		glUniform1i(uniDrawDistance, drawDistance * Perspective.LOCAL_TILE_SIZE);
		glUniform1i(uniExpandedMapLoadingChunks, client.getExpandedMapLoading());

		// Brightness happens to also be stored in the texture provider, so we use that
		TextureProvider textureProvider = client.getTextureProvider();
		glUniform1f(uniBrightness, (float) textureProvider.getBrightness());
		glUniform1f(uniSmoothBanding, config.smoothBanding() ? 0f : 1f);
		glUniform1i(uniColorBlindMode, config.colorBlindMode().ordinal());
		glUniform1f(uniTextureLightMode, config.brightTextures() ? 1f : 0f);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			// avoid textures animating during loading
			glUniform1i(uniTick, client.getGameCycle() & 127);
		}

		// Calculate projection matrix
		float[] projectionMatrix = Mat4.scale(client.getScale(), client.getScale(), 1);
		Mat4.mul(projectionMatrix, Mat4.projection(viewportWidth, viewportHeight, 50));
		Mat4.mul(projectionMatrix, Mat4.rotateX(cameraPitch));
		Mat4.mul(projectionMatrix, Mat4.rotateY(cameraYaw));
		Mat4.mul(projectionMatrix, Mat4.translate(-cameraX, -cameraY, -cameraZ));
		glUniformMatrix4fv(uniWorldProj, false, projectionMatrix);

		projectionMatrix = Mat4.identity();
		glUniformMatrix4fv(uniEntityProj, false, projectionMatrix);

		// Bind uniforms
		glUniformBlockBinding(glProgram, uniBlockMain, 0);
		glUniform1i(uniTextures, 1); // texture sampler array is bound to texture1

		// Enable face culling
		glEnable(GL_CULL_FACE);

		// Enable blending
		glEnable(GL_BLEND);
		glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE);

		// Enable depth testing
		glDepthFunc(GL_GREATER);
		glEnable(GL_DEPTH_TEST);

		checkGLErrors();
	}

	@Override
	public void drawZone(Projection projection, Scene scene, int pass, int zx, int zz)
	{
		updateEntityProject(projection);

		SceneContext ctx = context(scene);
		if(ctx==null) return;

		if (pass == DrawCallbacks.PASS_OPAQUE)
		{
			Zone z = ctx.zones[zx][zz];
			if (z.glVao == 0)
			{
				return;
			}

			glProgramUniform3i(glProgram, uniBase, zx << 10, 0, zz << 10);

			glBindVertexArray(z.glVao);
			z.computeDrawRanges(false, minLevel, level, maxLevel, hideRoofIds);

			glMultiDrawArrays(GL_TRIANGLES, z.glDrawOffset, z.glDrawLength);
		}
		else if (pass == DrawCallbacks.PASS_ALPHA)
		{
			Zone z = ctx.zones[zx][zz];
			if (z.glVaoA == 0)
			{
				return;
			}

			glProgramUniform3i(glProgram, uniBase, zx << 10, 0, zz << 10);

			glBindVertexArray(z.glVaoA);

			z.computeDrawRanges(true, minLevel, level, maxLevel, hideRoofIds);

			glDepthMask(false);
			glMultiDrawArrays(GL_TRIANGLES, z.glDrawOffset, z.glDrawLength);
			glDepthMask(true);
		}

		checkGLErrors();
	}

	@Override
	public void drawPass(Projection projection, Scene scene, int pass)
	{
		SceneContext ctx = context(scene);
		if(ctx == null) return;
		updateEntityProject(projection);
		if (pass == DrawCallbacks.PASS_OPAQUE)
		{
			{
				var vaos = ctx.vaoO.unmap();

				glProgramUniform3i(glProgram, uniBase, 0, 0, 0);
				for (VAO vao : vaos)
				{
					glBindVertexArray(vao.vao);
					glDrawArrays(GL_TRIANGLES, 0, vao.vbo.len / (VAO.VERT_SIZE / 4));
				}
			}

			{
				var vaos = ctx.vaoP.unmap();
				for (VAO vao : vaos)
				{
					glBindVertexArray(vao.vao);
					glDepthMask(false);
					glDrawArrays(GL_TRIANGLES, 0, vao.vbo.len / (VAO.VERT_SIZE / 4));
					glDepthMask(true);

					glColorMask(false, false, false, false);
					glDrawArrays(GL_TRIANGLES, 0, vao.vbo.len / (VAO.VERT_SIZE / 4));
					glColorMask(true, true, true, true);
				}

			}
		}
		else if (pass == DrawCallbacks.PASS_ALPHA)
		{
			var vaos = ctx.vaoA.unmap();

			glProgramUniform3i(glProgram, uniBase, 0, 0, 0);
			for (VAO vao : vaos)
			{
				glBindVertexArray(vao.vao);
				glDrawArrays(GL_TRIANGLES, 0, vao.vbo.len / (VAO.VERT_SIZE / 4));
			}
		}
		checkGLErrors();
	}

	@Override
	public void drawDynamic(Projection projection, Scene scene, TileObject tileObject, Renderable r, Model m, int orient, int x, int y, int z)
	{
		SceneContext ctx = context(scene);
		int size = m.getFaceCount() * 3 * VAO.VERT_SIZE;
		VAO o = ctx.vaoO.get(size), a = ctx.vaoA.get(size);
		sceneUploader.uploadModelTemp(m, orient, x, y, z, o.vbo.vb, a.vbo.vb);
	}

	@Override
	public void drawTemp(Projection projection, Scene scene, GameObject gameObject, Model m)
	{
		SceneContext ctx = context(scene);
		if (ctx == null) return;

		boolean player = gameObject.getRenderable() instanceof Player;
//		player=false;

		GameObject g = gameObject;
		int size = m.getFaceCount() * 3 * VAO.VERT_SIZE;
		if (!player) {
			VAO o = ctx.vaoO.get(size), a = ctx.vaoA.get(size);
			sceneUploader.uploadModelTemp(m, g.getModelOrientation(), g.getX(), g.getZ(), g.getY(), o.vbo.vb, a.vbo.vb);
		} else {
			m.calculateBoundsCylinder();
			VAO o = ctx.vaoP.get(size), a = o;
			facePrioritySorter.pushSortedModel(projection, m, g.getModelOrientation(), g.getX(), g.getZ(), g.getY(), o.vbo.vb, a.vbo.vb);
		}
	}

	@Override
	public void invalidateZone(Scene scene, int zx, int zz)
	{
		SceneContext ctx = context(scene);
		Zone z = ctx.zones[zx][zz];
		z.invalidate = true;
	}

	@Subscribe
	public void onPostClientTick(PostClientTick event)
	{
		//XXX gs check?
		rebuild(root);
		for(int i = 0; i < 2048 ;++i) {
			rebuild(subs[i]);
		}
	}

	private void rebuild(SceneContext ctx){
		if (ctx==null)return;
		for (int x = 0; x < ctx.sizeX; ++x)
		{
			for (int z = 0; z < ctx.sizeZ; ++z)
			{
				Zone zone = ctx.zones[x][z];
				if (!zone.invalidate)
				{
					continue;
				}

				assert zone.initialized;
				zone.free();
				zone = ctx.zones[x][z] = new Zone();

				Scene scene = client.getScene();
				sceneUploader.zoneSize(scene, zone, x, z);

				int sz = zone.sizeO * Zone.VERT_SIZE * 3;
				if (sz > 0)
				{
					zone.vboO = new VBO(sz);
					zone.vboO.init();
					zone.vboO.map();
				}

				sz = zone.sizeA * Zone.VERT_SIZE * 3;
				if (sz > 0)
				{
					zone.vboA = new VBO(sz);
					zone.vboA.init();
					zone.vboA.map();
				}

				sceneUploader.uploadZone(scene, zone, x, z);

				zone.prepare(); // un maps and uploads
				zone.initialized = true;
				zone.dirty = true;

				log.debug("Reuploaded zone {} {}", x, z);
			}
		}
	}

	@Override
	public void postDrawScene()
	{
		glDisable(GL_BLEND);
		glDisable(GL_CULL_FACE);
		glDisable(GL_DEPTH_TEST);

		// Blit FBO
		{
			int width = lastStretchedCanvasWidth;
			int height = lastStretchedCanvasHeight;

			final GraphicsConfiguration graphicsConfiguration = clientUI.getGraphicsConfiguration();
			final AffineTransform transform = graphicsConfiguration.getDefaultTransform();

			width = getScaledValue(transform.getScaleX(), width);
			height = getScaledValue(transform.getScaleY(), height);

			glBindFramebuffer(GL_READ_FRAMEBUFFER, fboScene);
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, awtContext.getFramebuffer(false));
			glBlitFramebuffer(0, 0, width, height, 0, 0, width, height,
				GL_COLOR_BUFFER_BIT, GL_NEAREST);

			// Reset
			glBindFramebuffer(GL_READ_FRAMEBUFFER, awtContext.getFramebuffer(false));
		}

	}

	private void prepareInterfaceTexture(int canvasWidth, int canvasHeight)
	{
		if (canvasWidth != lastCanvasWidth || canvasHeight != lastCanvasHeight)
		{
			lastCanvasWidth = canvasWidth;
			lastCanvasHeight = canvasHeight;

			glBindBuffer(GL_PIXEL_UNPACK_BUFFER, interfacePbo);
			glBufferData(GL_PIXEL_UNPACK_BUFFER, canvasWidth * canvasHeight * 4L, GL_STREAM_DRAW);
			glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);

			glBindTexture(GL_TEXTURE_2D, interfaceTexture);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, canvasWidth, canvasHeight, 0, GL_BGRA, GL_UNSIGNED_BYTE, 0);
			glBindTexture(GL_TEXTURE_2D, 0);
		}

		final BufferProvider bufferProvider = client.getBufferProvider();
		final int[] pixels = bufferProvider.getPixels();
		final int width = bufferProvider.getWidth();
		final int height = bufferProvider.getHeight();

		glBindBuffer(GL_PIXEL_UNPACK_BUFFER, interfacePbo);
		glMapBuffer(GL_PIXEL_UNPACK_BUFFER, GL_WRITE_ONLY)
			.asIntBuffer()
			.put(pixels, 0, width * height);
		glUnmapBuffer(GL_PIXEL_UNPACK_BUFFER);
		glBindTexture(GL_TEXTURE_2D, interfaceTexture);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, 0);
		glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	@Override
	public void draw(int overlayColor)
	{
		final GameState gameState = client.getGameState();
		if (gameState == GameState.STARTING)
		{
			return;
		}

		final TextureProvider textureProvider = client.getTextureProvider();
		if (textureArrayId == -1)
		{
			// lazy init textures as they may not be loaded at plugin start.
			// this will return -1 and retry if not all textures are loaded yet, too.
			textureArrayId = textureManager.initTextureArray(textureProvider);
			if (textureArrayId > -1)
			{
				// if texture upload is successful, compute and set texture animations
				float[] texAnims = textureManager.computeTextureAnimations(textureProvider);
				glProgramUniform2fv(glProgram, uniTextureAnimations, texAnims);
			}
		}

		final int canvasHeight = client.getCanvasHeight();
		final int canvasWidth = client.getCanvasWidth();

		prepareInterfaceTexture(canvasWidth, canvasHeight);

		if (gameState.getState() <= GameState.LOADING.getState())
		{
			glClearColor(0, 0, 0, 1);
			glClear(GL_COLOR_BUFFER_BIT);
		}

		// Texture on UI
		drawUi(overlayColor, canvasHeight, canvasWidth);

		try
		{
			awtContext.swapBuffers();
		}
		catch (RuntimeException ex)
		{
			// this is always fatal
			if (!canvas.isValid())
			{
				// this might be AWT shutting down on VM shutdown, ignore it
				return;
			}

			throw ex;
		}

		drawManager.processDrawComplete(this::screenshot);

		glBindFramebuffer(GL_FRAMEBUFFER, awtContext.getFramebuffer(false));

		checkGLErrors();
	}

	private void drawUi(final int overlayColor, final int canvasHeight, final int canvasWidth)
	{
		glEnable(GL_BLEND);
		glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
		glBindTexture(GL_TEXTURE_2D, interfaceTexture);

		// Use the texture bound in the first pass
		final UIScalingMode uiScalingMode = config.uiScalingMode();
		glUseProgram(glUiProgram);
		glUniform1i(uniTex, 0);
		glUniform1i(uniTexSamplingMode, uiScalingMode.getMode());
		glUniform2i(uniTexSourceDimensions, canvasWidth, canvasHeight);
		glUniform1i(uniUiColorBlindMode, config.colorBlindMode().ordinal());
		glUniform4f(uniUiAlphaOverlay,
			(overlayColor >> 16 & 0xFF) / 255f,
			(overlayColor >> 8 & 0xFF) / 255f,
			(overlayColor & 0xFF) / 255f,
			(overlayColor >>> 24) / 255f
		);

		if (client.isStretchedEnabled())
		{
			Dimension dim = client.getStretchedDimensions();
			glDpiAwareViewport(0, 0, dim.width, dim.height);
			glUniform2i(uniTexTargetDimensions, dim.width, dim.height);
		}
		else
		{
			glDpiAwareViewport(0, 0, canvasWidth, canvasHeight);
			glUniform2i(uniTexTargetDimensions, canvasWidth, canvasHeight);
		}

		// Set the sampling function used when stretching the UI.
		// This is probably better done with sampler objects instead of texture parameters, but this is easier and likely more portable.
		// See https://www.khronos.org/opengl/wiki/Sampler_Object for details.
		// GL_NEAREST makes sampling for bicubic/xBR simpler, so it should be used whenever linear isn't
		final int function = uiScalingMode == UIScalingMode.LINEAR ? GL_LINEAR : GL_NEAREST;
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, function);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, function);

		// Texture on UI
		glBindVertexArray(vaoUiHandle);
		glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

		// Reset
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindVertexArray(0);
		glUseProgram(0);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glDisable(GL_BLEND);
	}

	/**
	 * Convert the front framebuffer to an Image
	 *
	 * @return
	 */
	private Image screenshot()
	{
		int width = client.getCanvasWidth();
		int height = client.getCanvasHeight();

		if (client.isStretchedEnabled())
		{
			Dimension dim = client.getStretchedDimensions();
			width = dim.width;
			height = dim.height;
		}

		final GraphicsConfiguration graphicsConfiguration = clientUI.getGraphicsConfiguration();
		final AffineTransform t = graphicsConfiguration.getDefaultTransform();
		width = getScaledValue(t.getScaleX(), width);
		height = getScaledValue(t.getScaleY(), height);

		ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4)
			.order(ByteOrder.nativeOrder());

		glReadBuffer(awtContext.getBufferMode());
		glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		for (int y = 0; y < height; ++y)
		{
			for (int x = 0; x < width; ++x)
			{
				int r = buffer.get() & 0xff;
				int g = buffer.get() & 0xff;
				int b = buffer.get() & 0xff;
				buffer.get(); // alpha

				pixels[(height - y - 1) * width + x] = (r << 16) | (g << 8) | b;
			}
		}

		return image;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
//		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
//		{
//			zones = new Zone[NUM_ZONES][NUM_ZONES];
//			for (int x = 0; x < NUM_ZONES; ++x)
//				for (int y = 0; y < NUM_ZONES; ++y)
//				{
//					zones[x][y] = new Zone();
//				}
//		}
	}

	@Override
	public void loadScene(WorldView worldView, Scene scene)
	{
		if (scene.getWorldViewId() > -1) {
			loadSubScene(worldView, scene);
			return;
		}

		assert scene.getWorldViewId() == -1;
		if (nextZones != null) {
			log.error("Double zone-load");
			// this needs to free on the client thread
//			for (int x = 0; x < nextZones.length; ++x) {
//				for (int z = 0; z < nextZones[0].length; ++z) {
//					Zone zone = zones[x][z];
//
//					if (zone.state == Zone.ZONE_STATE_NEW) {
//						zone.free();
//					}
//				}
//			}
		}

		SceneContext ctx = root;
		Scene prev = client.getTopLevelWorldView().getScene();

		sceneUploader.prepare(scene);

		int dx = scene.getBaseX() - prev.getBaseX() >> 3;
		int dy = scene.getBaseY() - prev.getBaseY() >> 3;

		final int SCENE_ZONES = NUM_ZONES;

		// initially mark every zone as needing culled
		for (int x = 0; x < SCENE_ZONES; ++x)
		{
			for (int z = 0; z < SCENE_ZONES; ++z)
			{
				ctx.zones[x][z].cull = true;
			}
		}

		// find zones which overlap and copy them
		Zone[][] newZones = new Zone[SCENE_ZONES][SCENE_ZONES];
		if (prev.isInstance() == scene.isInstance()
			&& prev.getRoofRemovalMode() == scene.getRoofRemovalMode())
		{
			int[][][] prevTemplates = prev.getInstanceTemplateChunks();
			int[][][] curTemplates = scene.getInstanceTemplateChunks();

			for (int x = 0; x < SCENE_ZONES; ++x)
			{
				next:
				for (int z = 0; z < SCENE_ZONES; ++z)
				{
					int ox = x + dx;
					int oz = z + dy;

					// Reused the old zone if it is also in the new scene, except for the edges to work around
					// tile blending, edge shadows, sharelight, etc.
					//XXX this is wrong when extended map loading is <5 or when extended map area can't be loaded due to xteas missing!
					if (ox >= 1 && oz >= 1 && ox < SCENE_ZONES - 1 && oz < SCENE_ZONES - 1)
					{
						if (scene.isInstance())
						{
							// Convert from modified chunk coordinates to Jagex chunk coordinates
							int jx = x - (SCENE_OFFSET / 8);
							int jz = z - (SCENE_OFFSET / 8);
							int jox = ox - (SCENE_OFFSET / 8);
							int joz = oz - (SCENE_OFFSET / 8);
							// Check Jagex chunk coordinates are within the Jagex scene
							if (jx >= 0 && jx < Constants.SCENE_SIZE / 8 && jz >= 0 && jz < Constants.SCENE_SIZE / 8)
							{
								if (jox >= 0 && jox < Constants.SCENE_SIZE / 8 && joz >= 0 && joz < Constants.SCENE_SIZE / 8)
								{
									for (int level = 0; level < 4; ++level)
									{
										int prevTemplate = prevTemplates[level][jox][joz];
										int curTemplate = curTemplates[level][jx][jz];
										if (prevTemplate != curTemplate)
										{
											// Does this ever happen?
											log.warn("Instance template reuse mismatch! prev={} cur={}", prevTemplate, curTemplate);
											continue next;
										}
									}
								}
							}
						}

						Zone old = ctx.zones[ox][oz];
						if (!old.initialized) continue; // this happens from turning the plugin on when logged in?
//						assert old.initialized;

						if (old.dirty) continue;
						// without extended map loading these other zones will be loaded but empty
						if (old.sizeO ==0 && old.sizeA ==0) continue;

						assert old.cull;
						old.cull = false;

						newZones[x][z] = old;
					}
				}
			}
		}

		// Fill out any zones that weren't copied
		for (int x = 0; x < SCENE_ZONES; ++x)
		{
			for (int z = 0; z < SCENE_ZONES; ++z)
			{
				if (newZones[x][z] == null)
				{
					newZones[x][z] = new Zone();
				}
			}
		}

		// size the zones which require upload
		Stopwatch sw = Stopwatch.createStarted();
		int len = 0, lena = 0;
		int reused = 0, newzones = 0;
		for (int x = 0; x < NUM_ZONES; ++x)
		{
			for (int z = 0; z < NUM_ZONES; ++z)
			{
				Zone zone = newZones[x][z];
				if (!zone.initialized)
				{
					assert zone.glVao == 0;
					assert zone.glVaoA == 0;
					sceneUploader.zoneSize(scene, zone, x, z);
					len += zone.sizeO;
					lena += zone.sizeA;
					newzones++;
				}
				else
				{
					reused++;
				}
			}
		}
		log.debug("Scene size time {} reused {} new {} len opaque {} size opaque {}kb len alpha {} size alpha {}kb",
			sw, reused, newzones,
			len, ((long) len * Zone.VERT_SIZE * 3) / 1024,
			lena, ((long) lena * Zone.VERT_SIZE * 3) / 1024);

		// allocate buffers for zones which require upload
		CountDownLatch latch = new CountDownLatch(1);
		clientThread.invoke(() -> {
			for (int x = 0; x < Constants.EXTENDED_SCENE_SIZE >> 3; ++x)
			{
				for (int z = 0; z < Constants.EXTENDED_SCENE_SIZE >> 3; ++z)
				{
					Zone zone = newZones[x][z];

					if (zone.initialized) continue;

					int sz = zone.sizeO * Zone.VERT_SIZE * 3;
					if (sz > 0)
					{
						zone.vboO = new VBO(sz);
						zone.vboO.init();
						zone.vboO.map();
					}

					sz = zone.sizeA * Zone.VERT_SIZE * 3;
					if (sz > 0)
					{
						zone.vboA = new VBO(sz);
						zone.vboA.init();
						zone.vboA.map();
					}
				}
			}

			latch.countDown();
		});
		try
		{
			latch.await();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}

		// upload zones
		sw = Stopwatch.createStarted();
		for (int x = 0; x < Constants.EXTENDED_SCENE_SIZE>>3; ++x) {
			for (int z =0; z < Constants.EXTENDED_SCENE_SIZE>>3; ++z) {
				Zone zone = newZones[x][z];

				if (!zone.initialized)
				{
					sceneUploader.uploadZone(scene, zone, x, z);
				}
			}
		}
		log.debug("Scene upload time {}", sw);

		// Roof ids aren't consistent between scenes, so build a mapping of old -> new roof ids
		Map<Integer, Integer> roofChanges;
		{
			int[][][] prids = prev.getRoofs();
			int[][][] nrids = scene.getRoofs();
			dx <<= 3;
			dy <<= 3;
			roofChanges = new HashMap<>();

			sw = Stopwatch.createStarted();
			for (int level = 0; level < 4; ++level)
			{
				for (int x = 0; x < Constants.EXTENDED_SCENE_SIZE; ++x)
				{
					for (int z = 0; z < Constants.EXTENDED_SCENE_SIZE; ++z)
					{
						int ox = x + dx;
						int oz = z + dy;

						// old zone still in scene?
						if (ox >= 0 && oz >= 0 && ox < Constants.EXTENDED_SCENE_SIZE && oz < Constants.EXTENDED_SCENE_SIZE)
						{
							int prid = prids[level][ox][oz];
							int nrid = nrids[level][x][z];
							if (prid > 0 && nrid > 0 && prid != nrid)
							{
								roofChanges.putIfAbsent(prid, nrid);
							}
						}
					}
				}
			}
			sw.stop();

			log.debug("Roof remapping time {}", sw);
		}

		nextZones = newZones;
		nextRoofChanges = roofChanges;
	}

	private void loadSubScene(WorldView worldView, Scene scene)
	{
		int worldViewId = scene.getWorldViewId();
		assert worldViewId != -1;

		log.debug("Loading world view {}", worldViewId);

		SceneContext ctx0 = subs[worldViewId];
		if (ctx0 != null)
		{
			log.error("reload of an already loaded boat zone?");
		}

//		if (ctx == null) {
		final SceneContext ctx = new SceneContext(worldView.getSizeX() >> 3, worldView.getSizeY() >> 3);
		subs[worldViewId] = ctx;
//		}

//		SceneContext ctx = context(scene);
		// Does reloading of sub scenes ever happen?
		for (int x = 0; x < ctx.sizeX; ++x)
		{
			for (int z = 0; z < ctx.sizeZ; ++z)
			{
				if (ctx.zones[x][z].initialized)
				{
					log.error("reload of an already loaded boat zone?");
				}
			}
		}

		Zone[][] newZones = new Zone[ctx.sizeX][ctx.sizeZ];
		for (int x = 0; x < ctx.sizeX; ++x)
		{
			for (int z = 0; z < ctx.sizeZ; ++z)
			{
				Zone zone = newZones[x][z] = new Zone();

				sceneUploader.zoneSize(scene, zone, x, z);
			}
		}

		// allocate buffers for zones which require upload
		CountDownLatch latch = new CountDownLatch(1);
		clientThread.invoke(() -> {
			for (int x = 0; x < ctx.sizeX; ++x)
			{
				for (int z = 0; z < ctx.sizeZ; ++z)
				{
					Zone zone = newZones[x][z];// = new Zone();

					int sz = zone.sizeO * Zone.VERT_SIZE * 3;
					if (sz > 0)
					{
						zone.vboO = new VBO(sz);
						zone.vboO.init();
						zone.vboO.map();
					}

					sz = zone.sizeA * Zone.VERT_SIZE * 3;
					if (sz > 0)
					{
						zone.vboA = new VBO(sz);
						zone.vboA.init();
						zone.vboA.map();
					}
				}
			}

			ctx.zones = newZones;

			latch.countDown();
		});
		try
		{
			latch.await();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}

		for (int x = 0; x < ctx.sizeX; ++x)
		{
			for (int z = 0; z < ctx.sizeZ; ++z)
			{
				Zone zone = newZones[x][z];

				sceneUploader.uploadZone(scene, zone, x, z);

//				zone.prepare();
//				zone.initialized = true;
			}
		}
	}

	@Override
	public void despawnWorldView(WorldView worldView)
	{
		int worldViewId = worldView.getId();
		subs[worldViewId] = null; //XXX leaking gl buffers
	}

	@Override
	public void swapScene(Scene scene)
	{
		if (scene.getWorldViewId() > -1) {
			swapSub(scene); return;
		}

		SceneContext ctx = root;
		for (int x = 0; x < ctx.zones.length; ++x) {
			for (int z = 0; z < ctx.zones[0].length; ++z) {
				Zone zone = ctx.zones[x][z];

				if (zone.cull) {
					zone.free();
				}
			}
		}

		ctx.zones = nextZones;
		nextZones = null;

		// setup vaos
		for (int x = 0; x < ctx.zones.length; ++x)
		{
			for (int z = 0; z < ctx.zones[0].length; ++z)
			{
				Zone zone = ctx.zones[x][z];

				if (!zone.initialized)
				{
					zone.prepare();
					zone.initialized = true;
				}
			}
		}

		// Update roofs
		for (int x = 0; x < ctx.zones.length; ++x)
		{
			for (int z = 0; z < ctx.zones[0].length; ++z)
			{
				Zone zone = ctx.zones[x][z];
				zone.updateRoofs(nextRoofChanges);
			}
		}
		nextRoofChanges = null;

		checkGLErrors();
	}

	private void swapSub(Scene scene) {
		SceneContext ctx = context(scene);
		// setup vaos
		for (int x = 0; x < ctx.zones.length; ++x)
		{
			for (int z = 0; z < ctx.zones[0].length; ++z)
			{
				Zone zone = ctx.zones[x][z];

				if (!zone.initialized)
				{
					zone.prepare();
					zone.initialized = true;
				}
			}
		}
	}

	private int getScaledValue(final double scale, final int value)
	{
		return (int) (value * scale);
	}

	private void glDpiAwareViewport(final int x, final int y, final int width, final int height)
	{
		final GraphicsConfiguration graphicsConfiguration = clientUI.getGraphicsConfiguration();
		final AffineTransform t = graphicsConfiguration.getDefaultTransform();
		glViewport(
			getScaledValue(t.getScaleX(), x),
			getScaledValue(t.getScaleY(), y),
			getScaledValue(t.getScaleX(), width),
			getScaledValue(t.getScaleY(), height));
	}

	private int getDrawDistance()
	{
		return Ints.constrainToRange(config.drawDistance(), 0, MAX_DISTANCE);
	}

	private void updateBuffer(@Nonnull GLBuffer glBuffer, int target, @Nonnull FloatBuffer data, int usage)
	{
		int size = data.remaining() << 2;
		updateBuffer(glBuffer, target, size, usage);
		glBufferSubData(target, 0, data);
	}

	private void updateBuffer(@Nonnull GLBuffer glBuffer, int target, @Nonnull IntBuffer data, int usage)
	{
		int size = data.remaining() << 2;
		updateBuffer(glBuffer, target, size, usage);
		glBufferSubData(target, 0, data);
	}

	private void updateBuffer(@Nonnull GLBuffer glBuffer, int target, int size, int usage)
	{
		glBindBuffer(target, glBuffer.glBufferId);
		if (glCapabilities.glInvalidateBufferData != 0L)
		{
			// https://www.khronos.org/opengl/wiki/Buffer_Object_Streaming suggests buffer re-specification is useful
			// to avoid implicit syncing. We always need to trash the whole buffer anyway so this can't hurt.
			glInvalidateBufferData(glBuffer.glBufferId);
		}
		if (size > glBuffer.size)
		{
			int newSize = Math.max(1024, nextPowerOfTwo(size));
			log.debug("Buffer resize: {} {} -> {}", glBuffer.name, glBuffer.size, newSize);

			glBuffer.size = newSize;
			glBufferData(target, newSize, usage);
		}
	}

	private static int nextPowerOfTwo(int v)
	{
		v--;
		v |= v >> 1;
		v |= v >> 2;
		v |= v >> 4;
		v |= v >> 8;
		v |= v >> 16;
		v++;
		return v;
	}

	private void checkGLErrors()
	{
		if (!log.isDebugEnabled())
		{
			return;
		}

		for (; ; )
		{
			int err = glGetError();
			if (err == GL_NO_ERROR)
			{
				return;
			}

			String errStr;
			switch (err)
			{
				case GL_INVALID_ENUM:
					errStr = "INVALID_ENUM";
					break;
				case GL_INVALID_VALUE:
					errStr = "INVALID_VALUE";
					break;
				case GL_INVALID_OPERATION:
					errStr = "INVALID_OPERATION";
					break;
				case GL_INVALID_FRAMEBUFFER_OPERATION:
					errStr = "INVALID_FRAMEBUFFER_OPERATION";
					break;
				default:
					errStr = "" + err;
					break;
			}

			log.debug("glGetError:", new Exception(errStr));
		}
	}
}
