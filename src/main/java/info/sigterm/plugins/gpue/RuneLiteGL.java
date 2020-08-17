package info.sigterm.plugins.gpue;

import java.awt.Canvas;
import java.nio.Buffer;
import java.nio.ByteBuffer;

interface RuneLiteGL
{
	void init(Canvas canvas) throws Exception;

	void destroy();

	void swapBuffers();

	void glEnable(int cap);

	void glDisable(int cap);

	int glGetIntegerv(int pname);

	String glGetProgramInfoLog(int program);

	int glGetProgram(int program, int pname);

	void glClearColor(float red, float green, float blue, float alpha);

	void glClear(int mask);

	void glBlendFunc(int sfactor, int dfactor);

	void glViewport(int x, int y, int width, int height);

	int glCreateProgram();

	void glDeleteProgram(int program);

	void glUseProgram(int program);

	int glCreateShader(int type);

	void glDeleteShader(int shader);

	void glShaderSource(int shader, String source);

	void glCompileShader(int shader);

	int glGetShaderiv(int shader, int pname);

	void glAttachShader(int program, int shader);

	void glDetachShader(int program, int shader);

	void glLinkProgram(int program);

	void glValidateProgram(int program);

	int glGenVertexArrays();

	void glDeleteVertexArrays(int array);

	void glBindVertexArray(int array);

	void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long ptr);

	void glVertexAttribIPointer(int index, int size, int type, int stride, long ptr);

	void glEnableVertexAttribArray(int index);

	int glGenBuffers();

	void glDeleteBuffer(int buffer);

	void glBindBuffer(int target, int buffer);

	void glBindBufferBase(int target, int index, int buffer);

	void glBufferData(int target, int size, Buffer data, int usage);

	void glBufferSubData(int target, int offset, int size, Buffer data);

	int glGetUniformLocation(int program, String name);

	int glGetUniformBlockIndex(int program, String name);

	void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding);

	void glUniform1i(int uniform, int v0);

	void glUniform1f(int uniform, float v0);

	void glUniform2i(int uniform, int v0, int v1);

	void glUniform2fv(int uniform, int count, float[] value, int offset);

	void glUniform4f(int uniform, float v0, float v1, float v2, float v3);

	void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset);

	int glGenTexture();

	void glDeleteTexture(int texture);

	void glBindTexture(int target, int texture);

	void glActiveTexture(int texture);

	void glTexParameteri(int target, int pname, int param);

	void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer data);

	void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels);

	void glTexImage2DMultisample(int target, int samples, int internalformat, int width, int height, boolean fixedsamplelocations);

	void glTexStorage3D(int target, int levels, int internalformat, int width, int height, int depth);

	void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, Buffer buffer);

	int glGenFrameBuffer();

	void glDeleteFrameBuffer(int frameBuffer);

	void glBindFramebuffer(int target, int framebuffer);

	void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer);

	void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level);

	int glGenRenderbuffers();

	void glDeleteRenderbuffers(int renderBuffer);

	void glBindRenderbuffer(int target, int renderBuffer);

	void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height);

	void glDispatchCompute(int num_groups_x, int num_groups_y, int num_groups_z);

	void glMemoryBarrier(int barriers);

	void glDrawArrays(int mode, int first, int count);

	void glReadBuffer(int mode);

	void glReadPixels(int x, int y, int width, int height, int format, int type, ByteBuffer data);

	void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter);

	String glGetShaderInfoLog(int shader);
}
