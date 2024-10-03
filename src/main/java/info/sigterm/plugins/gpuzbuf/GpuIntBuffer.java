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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class GpuIntBuffer
{
	private IntBuffer buffer;

	GpuIntBuffer() {
		 buffer = allocateDirect(65536);
	}

	GpuIntBuffer(int size) {
		buffer = allocateDirect(size);
	}

	GpuIntBuffer(IntBuffer ib) { buffer = ib; }

	void put(float x, float y, float z, int w)
	{
		buffer.put(Float.floatToIntBits(x))
			.put(Float.floatToIntBits(y))
			.put(Float.floatToIntBits(z))
			.put(w);
	}

	void put(float x, float y, float z, float w)
	{
		buffer.put(Float.floatToIntBits(x))
			.put(Float.floatToIntBits(y))
			.put(Float.floatToIntBits(z))
			.put(Float.floatToIntBits(w));
	}

	void put22224(int x, int y, int z, int w) {
			buffer.put(((y & 0xffff) << 16) | (x & 0xffff));
			buffer.put(((0 & 0xffff) << 16) | (z & 0xffff));
			buffer.put(w);
	}

	void put(int i) {
		buffer.put(i);
	}

	void put2(int x, int y, int z, int w) {
		buffer.put(((y & 0xffff) << 16) | (x&0xffff));
		buffer.put(((w & 0xffff) << 16) | (z&0xffff));
	}

	void flip()
	{
		buffer.flip();
	}

	void clear()
	{
		buffer.clear();
	}

	void ensureCapacity(int size)
	{
		int capacity = buffer.capacity();
		final int position = buffer.position();
		if ((capacity - position) < size)
		{
			int old = capacity;
			do
			{
				capacity *= 2;
			}
			while ((capacity - position) < size);

			log.debug("resizing from {}kb to {}kb", old/1024, capacity/1024);
			IntBuffer newB = allocateDirect(capacity);
			buffer.flip();
			newB.put(buffer);
			buffer = newB;
		}
	}

	IntBuffer getBuffer()
	{
		return buffer;
	}

	static IntBuffer allocateDirect(int size)
	{
		return ByteBuffer.allocateDirect(size * Integer.BYTES)
			.order(ByteOrder.nativeOrder())
			.asIntBuffer();
	}
}
