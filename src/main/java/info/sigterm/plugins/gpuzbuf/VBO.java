package info.sigterm.plugins.gpuzbuf;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL15.glMapBuffer;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL15C.glDeleteBuffers;
import static org.lwjgl.opengl.GL15C.glGenBuffers;
import static org.lwjgl.opengl.GL15C.glUnmapBuffer;

class VBO
{
	final int size;
	int bufId;
	private ByteBuffer buffer;
	IntBuffer vb;
	int len;
	boolean mapped;

	VBO(int size)
	{
		this.size = size;
	}

	void init(int usage)
	{
		bufId = glGenBuffers();

		glBindBuffer(GL_ARRAY_BUFFER, bufId);
		glBufferData(GL_ARRAY_BUFFER, size, usage);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	void destroy()
	{
		if (mapped)
		{
			glBindBuffer(GL_ARRAY_BUFFER, bufId);
			glUnmapBuffer(GL_ARRAY_BUFFER);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			mapped = false;
		}
		glDeleteBuffers(bufId);
		bufId = 0;
	}

	void map()
	{
		assert !mapped;
		glBindBuffer(GL_ARRAY_BUFFER, bufId);
		buffer = glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY, buffer);
		if (buffer == null)
		{
			throw new RuntimeException("unable to map GL buffer " + bufId + " size " + size);
		}
		this.vb = buffer.asIntBuffer();
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		mapped = true;
	}

	void unmap()
	{
		assert mapped;
		len = vb.position();
		vb = null;

		glBindBuffer(GL_ARRAY_BUFFER, bufId);
		glUnmapBuffer(GL_ARRAY_BUFFER);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		mapped = false;
	}
}
