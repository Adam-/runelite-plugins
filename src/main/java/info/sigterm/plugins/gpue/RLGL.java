package info.sigterm.plugins.gpue;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.util.OSType;
import net.runelite.opengl.RLGLContext;

@Slf4j
public class RLGL implements RuneLiteGL
{
	private final RLGLContext gl = new RLGLContext();

	private static void loadLibrary(String prefix, String suffix, InputStream in) throws IOException
	{
		Path tempFile = Files.createTempFile(prefix, suffix);
		Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
		log.debug("Loading library {}", tempFile.toAbsolutePath().toString());
		System.load(tempFile.toAbsolutePath().toString());
	}

	@Override
	public void init(Canvas canvas) throws Exception
	{
		final String arch = System.getProperty("os.arch");

		log.debug("Loading library for {} {}", arch, OSType.getOSType());

		if (OSType.getOSType() == OSType.Linux)
		{
			if (!"amd64".equals(arch))
			{
				throw new RuntimeException("Arch is unsupported: Linux " + arch);
			}

			System.loadLibrary("jawt");

			try (InputStream in = getClass().getResourceAsStream("linux-amd64/librlgl.so"))
			{
				loadLibrary("librlgl", ".so", in);
			}
		}
		else if (OSType.getOSType() == OSType.MacOS)
		{
			if (!"amd64".equals(arch))
			{
				throw new RuntimeException("Arch is unsupported: MacOS " + arch);
			}

			try (InputStream in = getClass().getResourceAsStream("macos-amd64/librlgl.dylib"))
			{
				loadLibrary("librlgl", ".dylib", in);
			}
		}
		else
		{
			if (!"amd64".equals(arch) && "!x86".equals(arch))
			{
				throw new RuntimeException("Arch is unsupported: Windows " + arch);
			}

			System.loadLibrary("jawt");

			try (InputStream in = getClass().getResourceAsStream("windows-" + arch + "/rlgl.dll"))
			{
				loadLibrary("rlgl", ".dll", in);
			}
		}

		int inset_x = 0, inset_y = 0;
		if (OSType.getOSType() == OSType.MacOS)
		{
			// This logic copied from JOGL MacOSXJAWTWindow.
			Point p0 = new Point();
			Component outer = AWTUtil.getLocationOnScreenNonBlocking(p0, canvas);
			Point p1 = (Point) p0.clone();
			p1.translate(-outer.getX(), -outer.getY());
			inset_x = -p1.x;
			inset_y = -p1.y;
		}

		if (!RLGLContext.choosePixelFormat(canvas, 0, 0, inset_x, inset_y))
		{
			throw new RuntimeException("choosePixelFormat failed");
		}

		if (!RLGLContext.createContext())
		{
			throw new RuntimeException("createContext failed");
		}

		if (!RLGLContext.makeCurrent1())
		{
			throw new RuntimeException("makeCurrent failed");
		}

		RLGLContext.setSwapInterval(0);
	}

	@Override
	public void destroy()
	{
		RLGLContext.releaseContext();
		RLGLContext.destroy();
	}

	@Override
	public void swapBuffers()
	{
		RLGLContext.swapBuffers();
	}

	@Override
	public void glEnable(int cap)
	{
		gl.glEnable(cap);
	}

	@Override
	public void glDisable(int cap)
	{
		gl.glDisable(cap);
	}

	@Override
	public int glGetIntegerv(int pname)
	{
		int[] p = new int[1];
		gl.glGetIntegerv1(pname, p, 0);
		return p[0];
	}

	@Override
	public String glGetProgramInfoLog(int program)
	{
		return gl.glGetProgramInfoLog(program);
	}

	@Override
	public int glGetProgram(int program, int pname)
	{
		return gl.glGetProgramiv(program, pname);
	}

	@Override
	public void glClearColor(float red, float green, float blue, float alpha)
	{
		gl.glClearColor(red, green, blue, alpha);
	}

	@Override
	public void glClear(int mask)
	{
		gl.glClear(mask);
	}

	@Override
	public void glBlendFunc(int sfactor, int dfactor)
	{
		gl.glBlendFunc(sfactor, dfactor);
	}

	@Override
	public void glViewport(int x, int y, int width, int height)
	{
		gl.glViewport(x, y, width, height);
	}

	@Override
	public int glCreateProgram()
	{
		return gl.glCreateProgram();
	}

	@Override
	public void glDeleteProgram(int program)
	{
		gl.glDeleteProgram(program);
	}

	@Override
	public void glUseProgram(int program)
	{
		gl.glUseProgram(program);
	}

	@Override
	public int glCreateShader(int type)
	{
		return gl.glCreateShader(type);
	}

	@Override
	public void glDeleteShader(int shader)
	{
		gl.glDeleteShader(shader);
	}

	@Override
	public void glShaderSource(int shader, String source)
	{
		gl.glShaderSource(shader, 1, new String[]{source}, new int[]{source.length()}, 0);
	}

	@Override
	public void glCompileShader(int shader)
	{
		gl.glCompileShader(shader);
	}

	@Override
	public int glGetShaderiv(int shader, int pname)
	{
		return gl.glGetShaderiv(shader, pname);
	}

	@Override
	public void glAttachShader(int program, int shader)
	{
		gl.glAttachShader(program, shader);
	}

	@Override
	public void glDetachShader(int program, int shader)
	{
		gl.glDetachShader(program, shader);
	}

	@Override
	public void glLinkProgram(int program)
	{
		gl.glLinkProgram(program);
	}

	@Override
	public void glValidateProgram(int program)
	{
		gl.glValidateProgram(program);
	}

	@Override
	public int glGenVertexArrays()
	{
		return gl.glGenVertexArrays();
	}

	@Override
	public void glDeleteVertexArrays(int array)
	{
		gl.glDeleteVertexArrays(array);
	}

	@Override
	public void glBindVertexArray(int array)
	{
		gl.glBindVertexArray(array);
	}

	@Override
	public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long ptr)
	{
		gl.glVertexAttribPointer(index, size, type, normalized, stride, ptr);
	}

	@Override
	public void glVertexAttribIPointer(int index, int size, int type, int stride, long ptr)
	{
		gl.glVertexAttribIPointer(index, size, type, stride, ptr);
	}

	@Override
	public void glEnableVertexAttribArray(int index)
	{
		gl.glEnableVertexAttribArray(index);
	}

	@Override
	public int glGenBuffers()
	{
		return gl.glGenBuffers();
	}

	@Override
	public void glDeleteBuffer(int buffer)
	{
		gl.glDeleteBuffers(buffer);
	}

	@Override
	public void glBindBuffer(int target, int buffer)
	{
		gl.glBindBuffer(target, buffer);
	}

	@Override
	public void glBindBufferBase(int target, int index, int buffer)
	{
		gl.glBindBufferBase(target, index, buffer);
	}

	@Override
	public void glBufferData(int target, int size, Buffer data, int usage)
	{
		if (data != null)
		{
			assert data.isDirect();
			assert data.position() == 0;
			gl.glBufferData(target, size, data, usage, 0);
		}
		else
		{
			gl.glBufferData(target, size, null, usage, 0);
		}
	}

	@Override
	public void glBufferSubData(int target, int offset, int size, Buffer data)
	{
		if (data != null)
		{
			assert data.isDirect();
			assert data.position() == 0;
			gl.glBufferSubData(target, offset, size, data, 0);
		}
		else
		{
			gl.glBufferSubData(target, offset, size, null, 0);
		}
	}

	@Override
	public int glGetUniformLocation(int program, String name)
	{
		return gl.glGetUniformLocation(program, name);
	}

	@Override
	public int glGetUniformBlockIndex(int program, String name)
	{
		return gl.glGetUniformBlockIndex(program, name);
	}

	@Override
	public void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding)
	{
		gl.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
	}

	@Override
	public void glUniform1i(int uniform, int v0)
	{
		gl.glUniform1i(uniform, v0);
	}

	@Override
	public void glUniform1f(int uniform, float v0)
	{
		gl.glUniform1f(uniform, v0);
	}

	@Override
	public void glUniform2i(int uniform, int v0, int v1)
	{
		gl.glUniform2i(uniform, v0, v1);
	}

	@Override
	public void glUniform2fv(int uniform, int count, float[] value, int offset)
	{
		assert offset == 0;
		gl.glUniform2fv(uniform, count, value);
	}

	@Override
	public void glUniform4f(int uniform, float v0, float v1, float v2, float v3)
	{
		gl.glUniform4f(uniform, v0, v1, v2, v3);
	}

	@Override
	public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset)
	{
		assert offset == 0;
		gl.glUniformMatrix4fv(location, count, transpose, value);
	}

	@Override
	public int glGenTexture()
	{
		return gl.glGenTextures();
	}

	@Override
	public void glDeleteTexture(int texture)
	{
		gl.glDeleteTextures(texture);
	}

	@Override
	public void glBindTexture(int target, int texture)
	{
		gl.glBindTexture(target, texture);
	}

	@Override
	public void glActiveTexture(int texture)
	{
		gl.glActiveTexture(texture);
	}

	@Override
	public void glTexParameteri(int target, int pname, int param)
	{
		gl.glTexParameteri(target, pname, param);
	}

	@Override
	public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer data)
	{
		assert data == null || data.isDirect();
		gl.glTexImage2D0(target, level, internalformat, width, height, border, format, type, data, 0);
	}

	@Override
	public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels)
	{
		assert pixels == null || pixels.isDirect();
		gl.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
	}

	@Override
	public void glTexImage2DMultisample(int target, int samples, int internalformat, int width, int height, boolean fixedsamplelocations)
	{
		gl.glTexImage2DMultisample(target, samples, internalformat, width, height, fixedsamplelocations);
	}

	@Override
	public void glTexStorage3D(int target, int levels, int internalformat, int width, int height, int depth)
	{
		gl.glTexStorage3D(target, levels, internalformat, width, height, depth);
	}

	@Override
	public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, Buffer buffer)
	{
		assert buffer == null || buffer.isDirect();
		gl.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, buffer);
	}

	@Override
	public int glGenFrameBuffer()
	{
		return gl.glGenFramebuffers();
	}

	@Override
	public void glDeleteFrameBuffer(int frameBuffer)
	{
		gl.glDeleteFramebuffers(frameBuffer);
	}

	@Override
	public void glBindFramebuffer(int target, int framebuffer)
	{
		gl.glBindFramebuffer(target, framebuffer);
	}

	@Override
	public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer)
	{
		gl.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
	}

	@Override
	public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level)
	{
		gl.glFramebufferTexture2D(target, attachment, textarget, texture, level);
	}

	@Override
	public int glGenRenderbuffers()
	{
		return gl.glGenRenderbuffers();
	}

	@Override
	public void glDeleteRenderbuffers(int renderBuffer)
	{
		gl.glDeleteRenderbuffers(renderBuffer);
	}

	@Override
	public void glBindRenderbuffer(int target, int renderBuffer)
	{
		gl.glBindRenderbuffer(target, renderBuffer);
	}

	@Override
	public void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height)
	{
		gl.glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
	}

	@Override
	public void glDispatchCompute(int num_groups_x, int num_groups_y, int num_groups_z)
	{
		gl.glDispatchCompute(num_groups_x, num_groups_y, num_groups_z);
	}

	@Override
	public void glMemoryBarrier(int barriers)
	{
		gl.glMemoryBarrier(barriers);
	}

	@Override
	public void glDrawArrays(int mode, int first, int count)
	{
		gl.glDrawArrays(mode, first, count);
	}

	@Override
	public void glReadBuffer(int mode)
	{
		gl.glReadBuffer(mode);
	}

	@Override
	public void glReadPixels(int x, int y, int width, int height, int format, int type, ByteBuffer data)
	{
		gl.glReadPixels(x, y, width, height, format, type, data);
	}

	@Override
	public void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter)
	{
		gl.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
	}

	@Override
	public String glGetShaderInfoLog(int shader)
	{
		return gl.glGetShaderInfoLog(shader);
	}
}
