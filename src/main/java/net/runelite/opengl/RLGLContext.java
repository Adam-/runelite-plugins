/*
 * Copyright (c) 2020, Adam <Adam@sigterm.info>
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
package net.runelite.opengl;

import java.awt.Component;
import java.nio.Buffer;

public class RLGLContext
{
	public static native boolean createContext();

	public static native boolean releaseContext();

	public static native boolean destroy();

	public static native boolean swapBuffers();

	public static native int getlastError();

	public static native void setSwapInterval(int interval);

	public static native String getExtensionsString();

	public static native boolean choosePixelFormat(Component component, int num_samples, int alpha_bits, int inset_x, int inset_y) throws Exception;

	public static native boolean makeCurrent1();

	public native void glActiveTexture(int texture);

	public native void glAlphaFunc(int func, float ref);

	public native void glAttachShader(int program, int shader);

	public native void glBindBuffer(int target, int buffer);

	public native void glBindBufferBase(int target, int index, int buffer);

	public native void glBindFramebuffer(int target, int framebuffer);

	public native void glBindRenderbuffer(int target, int renderBuffer);

	public native void glBindTexture(int target, int texture);

	public native void glBindVertexArray(int array);

	public native int glBlendFunc(int sfactor, int dfactor);

	public native void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter);

	public native void glBufferData(int target, int size, Buffer data, int usage, int data_off);

	public native void glBufferSubData(int target, int offset, int size, Buffer data, int data_off);

	public native void glClear(int mask);

	public native void glClearColor(float red, float green, float blue, float alpha);

	public native void glCompileShader(int shader);

	public native int glCreateProgram();

	public native int glCreateShader(int type);

	public native void glDeleteBuffers(int buffer);

	public native void glDeleteFramebuffers(int framebuffer);

	public native void glDeleteProgram(int program);

	public native void glDeleteRenderbuffers(int renderBuffer);

	public native void glDeleteShader(int shader);

	public native void glDeleteTextures(int texture);

	public native void glDeleteVertexArrays(int array);

	public native void glDetachShader(int program, int shader);

	public native void glDisable(int cap);

	public native void glDispatchCompute(int num_groups_x, int num_groups_y, int num_groups_z);

	public native void glDrawArrays(int mode, int first, int count);

	public native void glDrawElements(int mode, int count, int type, long indices);

	public native void glDrawElements0(int mode, int count, int type, Buffer indices, int indices_off);

	public native void glEnable(int cap);

	public native void glEnableVertexAttribArray(int index);

	public native void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer);

	public native void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level);

	public native int glGenBuffers();

	public native int glGenFramebuffers();

	public native int glGenRenderbuffers();

	public native int glGenTextures();

	public native int glGenVertexArrays();

	public native void glGenerateMipmap(int target);

	public native boolean glGetBooleanv(int pname);

	public native double glGetDoublev(int pname);

	public native float glGetFloatv(int pname);

	public native int glGetIntegerv(int pname);

	public native String glGetProgramInfoLog(int program);

	public native int glGetProgramiv(int program, int pname);

	public native String glGetShaderInfoLog(int shader);

	public native int glGetShaderiv(int shader, int pname);

	public native String glGetString(int name);

	public native int glGetUniformBlockIndex(int program, String uniformBlockName);

	public native int glGetUniformLocation(int program, String name);

	public native void glLinkProgram(int program);

	public native void glMemoryBarrier(int barriers);

	public native void glReadBuffer(int mode);

	public native void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer data);

	public native void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height);

	public native void glScissor(int x, int y, int width, int height);

	public native void glShaderSource(int shader, int count, String[] string, int[] len, int length_off);

	public native void glTexImage2D0(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels, int pixels_off);

	public native void glTexImage2DMultisample(int target, int samples, int internalformat, int width, int height, boolean fixedsamplelocations);

	public native void glTexParameterf(int target, int pname, float param);

	public native void glTexParameteri(int target, int pname, int param);

	public native void glTexStorage3D(int target, int levels, int internalformat, int width, int height, int depth);

	public native void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer data);

	public native void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, Buffer data);

	public native void glUniform1f(int uniform, float v0);

	public native void glUniform1i(int uniform, int v0);

	public native void glUniform2fv(int uniform, int count, float[] values);

	public native void glUniform2i(int uniform, int v0, int v1);

	public native void glUniform4f(int uniform, float v0, float v1, float v2, float v3);

	public native void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding);

	public native void glUniformMatrix4fv(int uniform, int count, boolean transpose, float[] value);

	public native void glUseProgram(int program);

	public native void glValidateProgram(int program);

	public native void glVertexAttribIPointer(int index, int size, int type, int stride, long ptr);

	public native void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long ptr);

	public native int glViewport(int x, int y, int width, int height);

	public native boolean isExtensionAvailable(String name);
}
