package info.sigterm.plugins.gpuzbuf;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;
import static org.lwjgl.opengl.GL30C.glVertexAttribI3i;
import static org.lwjgl.opengl.GL30C.glVertexAttribIPointer;

class VAO
{
	static final int VERT_SIZE = 24;

	final VBO vbo;
	int vao;

	VAO(int size)
	{
		vbo = new VBO(size);
	}

	void init()
	{
		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		vbo.init();
		glBindBuffer(GL_ARRAY_BUFFER, vbo.bufId);
//		glBufferData(GL_ARRAY_BUFFER, vbo.size, GL_DYNAMIC_DRAW);

		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, VERT_SIZE, 0);

		glVertexAttribI3i(1, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

		glEnableVertexAttribArray(2);
		glVertexAttribIPointer(2, 1, GL_INT, VERT_SIZE, 12);

		glEnableVertexAttribArray(3);
		glVertexAttribIPointer(3, 4, GL_SHORT, VERT_SIZE, 16);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}

	void destroy()
	{
		vbo.destroy();
		glDeleteVertexArrays(vao);
		vao = 0;
	}
}

@Slf4j
class VAOList {
	// this needs to be larger than the largest single model
	//	private static final int VAO_SIZE = 16 * 1024 * 1024;
	private static final int VAO_SIZE = 1024 * 1024;

	private int curIdx;
	private final List<VAO> vaos = new ArrayList<>();

	 VAO get(int size)
	{
		assert size <= VAO_SIZE;

		while(curIdx < vaos.size()) {
			VAO vao = vaos.get(curIdx);
			if (!vao.vbo.mapped) {
				vao.vbo.map();
			}

			int rem = vao.vbo.vb.remaining() * Integer.BYTES;
			if (size <= rem) {
				return vao;
			}

			curIdx++;
		}

		VAO vao = new VAO(VAO_SIZE);
		vao.init();
		vao.vbo.map();
		vaos.add(vao);
		log.debug("Allocated VAO {}", vao.vao);
		return vao;
	}

	List<VAO> unmap()
	{
		int sz=0;
		for (VAO vao : vaos)
		{
			if (vao.vbo.mapped)
			{
				++sz;
				vao.vbo.unmap();
			}
		}
		curIdx = 0;
		return vaos.subList(0, sz);
	}

	void destroy()
	{
		for (VAO vao : vaos)
		{
			vao.destroy();
		}
		vaos.clear();
		curIdx = 0;
	}
}