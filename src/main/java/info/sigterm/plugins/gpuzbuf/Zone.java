package info.sigterm.plugins.gpuzbuf;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import static org.lwjgl.opengl.GL30C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL30C.GL_INT;
import static org.lwjgl.opengl.GL30C.GL_SHORT;
import static org.lwjgl.opengl.GL30C.glBindBuffer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;
import static org.lwjgl.opengl.GL30C.glVertexAttrib3f;
import static org.lwjgl.opengl.GL30C.glVertexAttribIPointer;

@Slf4j
class Zone
{
	static final int VERT_SIZE = 20;

//	static final int ZONE_STATE_CULL = 0;
//	static final int ZONE_STATE_LIVE = 1;
//	static final int ZONE_STATE_NEW = 2;

	int glVao;
	int bufLen;

	int glVaoA;
	int bufLenA;

	int sizeO, sizeA;
	VBO vboO, vboA;

	boolean initialized; // whether the zone vao and vbos are ready
	boolean cull; // whether the zone is queued for deletion
	boolean dirty; // whether the zone has temporary modifications
	boolean invalidate; // whether the zone needs rebuilding

	int[] levelOffsets = new int[4]; // buffer pos in ints for the end of the level
	int[] levelOffsetsA = new int[4];

	int[][] rids;
	int[][] roofStart;
	int[][] roofEnd;
	int[][] roofStartA;
	int[][] roofEndA;

	void free()
	{
		if (vboO != null)
		{
			vboO.destroy();
			vboO = null;
		}

		if (vboA != null)
		{
			vboA.destroy();
			vboA = null;
		}

		if (glVao != 0)
		{
			glDeleteVertexArrays(glVao);
			glVao = 0;
		}

		if (glVaoA != 0)
		{
			glDeleteVertexArrays(glVaoA);
			glVaoA = 0;
		}
	}

	void prepare()
	{
		if (vboO != null)
		{
			vboO.unmap();
		}
		if (vboA != null)
		{
			vboA.unmap();
		}

		// VAO
		int vaoO = 0;
		if (vboO != null)
		{
			vaoO = glGenVertexArrays();
			setupVao(vaoO, vboO.bufId);
		}

		// ALPHA VAO
		int vaoA = 0;
		if (vboA != null)
		{
			vaoA = glGenVertexArrays();
			setupVao(vaoA, vboA.bufId);
		}

		assert glVao == 0;
		assert glVaoA == 0;

		if (vboO != null)
		{
			this.glVao = vaoO;
			this.bufLen = vboO.len / (VERT_SIZE / 4);
		}

		if (vboA != null)
		{
			this.glVaoA = vaoA;
			this.bufLenA = vboA.len / (VERT_SIZE / 4);
		}
	}

	private void setupVao(int vao, int buffer)
	{
		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, buffer);

		glVertexAttrib3f(0, -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);

		glEnableVertexAttribArray(1);
		glVertexAttribIPointer(1, 3, GL_SHORT, VERT_SIZE, 0);

		glEnableVertexAttribArray(2);
		glVertexAttribIPointer(2, 1, GL_INT, VERT_SIZE, 8);

		glEnableVertexAttribArray(3);
		glVertexAttribIPointer(3, 4, GL_SHORT, VERT_SIZE, 12);

		glBindVertexArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	void updateRoofs(Map<Integer, Integer> updates)
	{
		for (int level = 0; level < 4; ++level)
		{
			for (int i = 0; i < rids[level].length; ++i)
			{
				rids[level][i] = updates.getOrDefault(rids[level][i], rids[level][i]);
			}
		}
	}

	private static final int[] drawOff = new int[512];
	private static final int[] drawEnd = new int[512];
	private static int drawIdx = 0;
	static int[] glDrawOffset, glDrawLength;

	private void convertForDraw()
	{
		for (int i = 0; i < drawIdx; ++i)
		{
			assert drawEnd[i] >= drawOff[i];

			drawOff[i] /= VERT_SIZE / 4;
			drawEnd[i] /= VERT_SIZE / 4;

			assert drawEnd[i] >= drawOff[i];
			drawEnd[i] = drawEnd[i] - drawOff[i];
		}

		glDrawOffset = Arrays.copyOfRange(drawOff, 0, drawIdx);
		glDrawLength = Arrays.copyOfRange(drawEnd, 0, drawIdx);
	}

	void computeDrawRanges(boolean alpha, int lowerLevel, int currentLevel, int upperLevel, Set<Integer> hiddenRoofIds)
	{
		drawIdx = 0;

		int[] _levelOffsets = alpha ? levelOffsetsA : levelOffsets;
		int[][] _roofStart = alpha ? roofStartA : roofStart;
		int[][] _roofEnd = alpha ? roofEndA : roofEnd;

		for (int level = lowerLevel; level <= upperLevel; ++level)
		{
			int[] rids = this.rids[level];
			int[] roofStart = _roofStart[level];
			int[] roofEnd = _roofEnd[level];

			if (rids.length == 0 || hiddenRoofIds.isEmpty() || level <= currentLevel)
			{
				// draw the whole level
				int start = level == 0 ? 0 : _levelOffsets[level - 1];
				int end = _levelOffsets[level];
				pushRange(start, end);
				continue;
			}

			for (int roofIdx = 0; roofIdx < rids.length; ++roofIdx)
			{
				int rid = rids[roofIdx];
				if (rid > 0 && !hiddenRoofIds.contains(rid))
				{
					// draw the roof
					assert roofEnd[roofIdx] >= roofStart[roofIdx];
					if (roofEnd[roofIdx] > roofStart[roofIdx])
					{
						pushRange(roofStart[roofIdx], roofEnd[roofIdx]);
					}
				}
			}

			int endpos = level == 0 ? 0 : _levelOffsets[level - 1];
			for (int roofIdx = 0; roofIdx < rids.length; ++roofIdx)
			{
				int rid = rids[roofIdx];
				if (rid > 0)
				{
					endpos = roofEnd[roofIdx];
				}
			}
			// draw the non roofs
			pushRange(endpos, _levelOffsets[level]);
		}

		convertForDraw();
	}

	private static void pushRange(int start, int end)
	{
		assert end >= start;

		if (drawIdx > 0 && drawEnd[drawIdx - 1] == start)
		{
			drawEnd[drawIdx - 1] = end;
		}
		else
		{
			drawOff[drawIdx] = start;
			drawEnd[drawIdx] = end;
			drawIdx++;
		}
	}
}
