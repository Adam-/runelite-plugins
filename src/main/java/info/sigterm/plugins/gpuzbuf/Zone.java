package info.sigterm.plugins.gpuzbuf;

import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Model;
import net.runelite.api.Perspective;
import net.runelite.api.Scene;
import static info.sigterm.plugins.gpuzbuf.FacePrioritySorter.distanceFaceCount;
import static info.sigterm.plugins.gpuzbuf.FacePrioritySorter.distanceToFaces;
import static info.sigterm.plugins.gpuzbuf.GpuPlugin.glProgram;
import static info.sigterm.plugins.gpuzbuf.GpuPlugin.uniBase;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL14.glMultiDrawArrays;
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
import static org.lwjgl.opengl.GL41C.glProgramUniform3i;

@Slf4j
@RequiredArgsConstructor
class Zone
{
	// Zone vertex format
	// index 0: -FLOAT.MAX_VALUE (non-array)
	// index 1: short vec3(x, y, z)
	// index 2: int abhsl
	// index 3: short vec4(id, x, y, z)
	static final int VERT_SIZE = 20;

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

	int[][] rids;
	int[][] roofStart;
	int[][] roofEnd;

	final List<AlphaModel> alphaModels = new ArrayList<>(0);

	void init(VBO o, VBO a)
	{
		assert glVao == 0;
		assert glVaoA == 0;

		if (o != null)
		{
			vboO = o;
			glVao = glGenVertexArrays();
			setupVao(glVao, o.bufId);
		}

		if (a != null)
		{
			vboA = a;
			glVaoA = glGenVertexArrays();
			setupVao(glVaoA, a.bufId);
		}
	}

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

		// don't add permanent alphamodels to the cache as permanent alphamodels are always allocated
		// to avoid having to synchronize the cache
		alphaModels.clear();
	}

	void unmap()
	{
		if (vboO != null)
		{
			vboO.unmap();
		}
		if (vboA != null)
		{
			vboA.unmap();
		}

		if (vboO != null)
		{
			this.bufLen = vboO.len / (VERT_SIZE / 4);
		}

		if (vboA != null)
		{
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

		for (AlphaModel m : alphaModels)
		{
			m.rid = (short) (int) updates.getOrDefault((int) m.rid, (int) m.rid);
		}
	}

	private static final int NUM_DRAW_RANGES = 512;
	private static final int[] drawOff = new int[NUM_DRAW_RANGES];
	private static final int[] drawEnd = new int[NUM_DRAW_RANGES];
	private static int drawIdx = 0;
	private static int[] glDrawOffset, glDrawLength;

	private void convertForDraw(int vertSize)
	{
		for (int i = 0; i < drawIdx; ++i)
		{
			assert drawEnd[i] >= drawOff[i];

			// convert from bytes to verts
			drawOff[i] /= vertSize >> 2;
			drawEnd[i] /= vertSize >> 2;

			drawEnd[i] -= drawOff[i]; // convert from end pos to length
		}

		glDrawOffset = Arrays.copyOfRange(drawOff, 0, drawIdx);
		glDrawLength = Arrays.copyOfRange(drawEnd, 0, drawIdx);
	}

	void renderOpaque(int zx, int zz, int minLevel, int currentLevel, int maxLevel, Set<Integer> hiddenRoofIds)
	{
		drawIdx = 0;

		for (int level = minLevel; level <= maxLevel; ++level)
		{
			int[] rids = this.rids[level];
			int[] roofStart = this.roofStart[level];
			int[] roofEnd = this.roofEnd[level];

			if (rids.length == 0 || hiddenRoofIds.isEmpty() || level <= currentLevel)
			{
				// draw the whole level
				int start = level == 0 ? 0 : this.levelOffsets[level - 1];
				int end = this.levelOffsets[level];
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

			// push from the end of the last roof to the end of the level
			int endpos = level == 0 ? 0 : this.levelOffsets[level - 1];
			for (int roofIdx = rids.length - 1; roofIdx >= 0; --roofIdx)
			{
				int rid = rids[roofIdx];
				if (rid > 0)
				{
					endpos = roofEnd[roofIdx];
					break;
				}
			}
			// draw the non roofs
			pushRange(endpos, this.levelOffsets[level]);
		}

		convertForDraw(VERT_SIZE);

		glProgramUniform3i(glProgram, uniBase, zx << 10, 0, zz << 10);
		glBindVertexArray(glVao);
		glMultiDrawArrays(GL_TRIANGLES, glDrawOffset, glDrawLength);
	}

	private static void pushRange(int start, int end)
	{
		assert end >= start;

		if (drawIdx > 0 && drawEnd[drawIdx - 1] == start)
		{
			drawEnd[drawIdx - 1] = end;
		}
		else if (drawIdx >= NUM_DRAW_RANGES)
		{
			log.debug("draw ranges exhausted");
		}
		else
		{
			drawOff[drawIdx] = start;
			drawEnd[drawIdx] = end;
			drawIdx++;
		}
	}

	static class AlphaModel
	{
		int id;
		// only set for static geometry as they require sorting
		Model model;
		int startpos, endpos;
		short x, y, z; // local position
		short rid;
		int vao;
		byte level;
		byte lx, lz, ux, uz; // lower/upper zone coords
		byte zofx, zofz; // for temp alpha models, offset of source zone from target zone
		byte flags;

		static final int SKIP = 1; // temporary model is in a closer zone
		static final int TEMP = 2; // temporary model added to a closer zone

		boolean isTemp()
		{
			return model == null;
		}
	}

	static final Queue<AlphaModel> modelCache = new ArrayDeque<>();

	void addAlphaModel(int vao, Model model, int startpos, int endpos, int x, int y, int z, int lx, int lz, int ux, int uz, int rid, int level, int id)
	{
		AlphaModel m = new AlphaModel();
		m.id = id;
		m.model = model;
		m.startpos = startpos;
		m.endpos = endpos;
		m.x = (short) x;
		m.y = (short) y;
		m.z = (short) z;
		m.vao = vao;
		m.rid = (short) rid;
		m.level = (byte) level;
		if (lx > -1)
		{
			m.lx = (byte) lx;
			m.lz = (byte) lz;
			m.ux = (byte) ux;
			m.uz = (byte) uz;
		}
		else
		{
			m.lx = m.lz = m.ux = m.uz = -1;
		}
		alphaModels.add(m);
	}

	void addTempAlphaModel(int vao, int startpos, int endpos, int x, int y, int z)
	{
		AlphaModel m = modelCache.poll();
		if (m == null)
		{
			m = new AlphaModel();
		}
		m.id = -1;
		m.model = null;
		m.startpos = startpos;
		m.endpos = endpos;
		m.x = (short) x;
		m.y = (short) y;
		m.z = (short) z;
		m.vao = vao;
		m.rid = -1;
		m.level = -1;
		m.lx = m.lz = m.ux = m.uz = -1;
		m.flags = 0;
		m.zofx = m.zofz = 0;
		alphaModels.add(m);
	}

	void removeTemp()
	{
		for (int i = alphaModels.size() - 1; i >= 0; --i)
		{
			AlphaModel m = alphaModels.get(i);
			if (m.isTemp() || (m.flags & AlphaModel.TEMP) != 0)
			{
				alphaModels.remove(i);
				m.model = null;
				modelCache.add(m);
			}
			m.flags &= ~AlphaModel.SKIP;
		}
	}

	// this needs to be larger than the max model alpha face count * 3
	private static final IntBuffer alphaElements = BufferUtils.createIntBuffer(16384);

	private static final int STATIC = 1;
	private static final int TEMP = 2;

	private static int lastDrawMode;
	private static int lastVao;
	private static int lastzx, lastzz;

	void renderAlpha(int zx, int zz, int cx, int cy, int cz, int cyaw, int cpitch, int minLevel, int currentLevel, int maxLevel, Set<Integer> hiddenRoofIds)
	{
		drawIdx = 0;
		alphaElements.clear();
		lastDrawMode = lastVao = 0;
		lastzx = zx;
		lastzz = zz;

		alphaModels.sort(Comparator.comparingInt((AlphaModel m) ->
					{
						final int mx = (m.x + ((zx-m.zofx)<<10));
						final int mz = (m.z + ((zz-m.zofz)<<10));
						return (mx - cx) * (mx - cx) +
							(m.y - cy) * (m.y - cy) +
							(mz - cz) * (mz - cz);
					}
				)
				.reversed()
		);

		int yawsin = Perspective.SINE[cyaw];
		int yawcos = Perspective.COSINE[cyaw];
		int pitchsin = Perspective.SINE[cpitch];
		int pitchcos = Perspective.COSINE[cpitch];
		for (AlphaModel m : alphaModels)
		{
			if ((m.flags & AlphaModel.SKIP) != 0) continue;

			boolean ok = true;
			if (m.level != -1)
			{
				ok = false;
				int level = m.level;
				if (level >= minLevel && level <= maxLevel)
				{
					if (level <= currentLevel || !hiddenRoofIds.contains((int)m.rid))
					{
						ok = true;
					}
				}
			}
			if (!ok)
			{
				continue;
			}

			if (lastVao != m.vao
				|| lastzx != (zx - m.zofx) || lastzz != (zz - m.zofz)
			)
			{
				flush();
			}

			lastDrawMode = m.isTemp() ? TEMP : STATIC;
			lastVao = m.vao;
			lastzx = zx - m.zofx;
			lastzz = zz - m.zofz;

			if (m.isTemp())
			{
				// these are already sorted and so just requires a glMultiDrawArrays() from the active vao
				pushRange(m.startpos, m.endpos);
				continue;
			}

			Model model = m.model;
			model.calculateBoundsCylinder();

			final int diameter = model.getDiameter();
			final int radius = model.getRadius();
			if (diameter >= 6000)
			{
				continue;
			}

			Arrays.fill(distanceFaceCount, 0, diameter, (char) 0);

			byte[] transparencies = model.getFaceTransparencies();
			int faceCount = model.getFaceCount();
			final float[] vertexX = model.getVerticesX();
			final float[] vertexY = model.getVerticesY();
			final float[] vertexZ = model.getVerticesZ();
			final int[] indices1 = model.getFaceIndices1();
			final int[] indices2 = model.getFaceIndices2();
			final int[] indices3 = model.getFaceIndices3();
			final int[] color3 = model.getFaceColors3();

			char bufferIdx = 0;
			for (int faceIdx = 0; faceIdx < faceCount; ++faceIdx)
			{
				if (color3[faceIdx] == -2)
				{
					continue;
				}
				if (transparencies[faceIdx] == 0)
				{
					continue;
				}

				int x, y, z, t;
				int z0, z1, z2;

				x = (int) vertexX[indices1[faceIdx]];
				y = (int) vertexY[indices1[faceIdx]];
				z = (int) vertexZ[indices1[faceIdx]];

				t = z * yawcos - x * yawsin >> 16;
				z0 = y * pitchsin + t * pitchcos >> 16;

				x = (int) vertexX[indices2[faceIdx]];
				y = (int) vertexY[indices2[faceIdx]];
				z = (int) vertexZ[indices2[faceIdx]];

				t = z * yawcos - x * yawsin >> 16;
				z1 = y * pitchsin + t * pitchcos >> 16;

				x = (int) vertexX[indices3[faceIdx]];
				y = (int) vertexY[indices3[faceIdx]];
				z = (int) vertexZ[indices3[faceIdx]];

				t = z * yawcos - x * yawsin >> 16;
				z2 = y * pitchsin + t * pitchcos >> 16;

				// TODO fix boats
				int fz = radius + (z0 + z1 + z2) / 3;
				assert fz >= 0 && fz < diameter;
				distanceToFaces[fz][distanceFaceCount[fz]++] = bufferIdx++;
			}

			if (bufferIdx * 3 > alphaElements.remaining())
			{
				flush();
			}

			int start = m.startpos / (VERT_SIZE >> 2); // ints to verts
			for (int i = diameter - 1; i >= 0; --i)
			{
				final int cnt = distanceFaceCount[i];
				if (cnt > 0)
				{
					final char[] faces = distanceToFaces[i];

					for (int faceIdx = 0; faceIdx < cnt; ++faceIdx)
					{
						int face = faces[faceIdx];
						face *= 3;
						face += start;
						alphaElements.put(face++);
						alphaElements.put(face++);
						alphaElements.put(face++);
					}
				}
			}
		}

		flush();
	}

	private void flush()
	{
		if (lastDrawMode == TEMP)
		{
			convertForDraw(VAO.VERT_SIZE);
			glProgramUniform3i(glProgram, uniBase,0,0,0);
			glBindVertexArray(lastVao);
			glMultiDrawArrays(GL_TRIANGLES, glDrawOffset, glDrawLength);
			drawIdx = 0;
		}
		else if (lastDrawMode == STATIC)
		{
			alphaElements.flip();
			glProgramUniform3i(glProgram, uniBase, lastzx << 10, 0, lastzz << 10);
			glBindVertexArray(lastVao);
			glDrawElements(GL_TRIANGLES, alphaElements);
			alphaElements.clear();
		}
	}

	void multizoneLocs(Scene scene, int zx, int zz, int cx, int cz, Zone[][] zones)
	{
		int offset = scene.getWorldViewId() == -1 ? GpuPlugin.SCENE_OFFSET >> 3 : 0;
		for (AlphaModel m : alphaModels)
		{
			if (m.lx == -1)
			{
				continue;
			}

			// calculate which zone this model should be drawn from
			// TODO fix for boats
			int max = Integer.MAX_VALUE;
			int closestZoneX = -50, closestZoneZ = -50;
			for (int x = m.lx >> 3; x <= m.ux >> 3; ++x)
			{
				for (int z = m.lz >> 3; z <= m.uz >> 3; ++z)
				{
					int centerX = (zx - m.zofx + x) * 8 + 4 << 7;
					int centerZ = (zz - m.zofz + z) * 8 + 4 << 7;
					int distance = (centerX - cx) * (centerX - cx) +
						(centerZ - cz) * (centerZ - cz);
					if (distance < max)
					{
						max = distance;
						closestZoneX = centerX >> 10;
						closestZoneZ = centerZ >> 10;
					}
				}
			}
			assert closestZoneX != -50;
			if (closestZoneX != zx || closestZoneZ != zz)
			{
				assert (m.flags & AlphaModel.TEMP) == 0;

				assert closestZoneX + offset >= 0 : closestZoneX;
				assert closestZoneX + offset < zones.length : closestZoneX;
				assert closestZoneZ + offset >= 0 : closestZoneZ;
				assert closestZoneZ + offset < zones[0].length : closestZoneZ;

				Zone z = zones[closestZoneX + offset][closestZoneZ + offset];
				assert z != null;
				assert z != this;

				AlphaModel m2 = modelCache.poll();
				if (m2 == null)
				{
					m2 = new AlphaModel();
				}
				m2.id = m.id;
				m2.model = m.model;
				m2.startpos = m.startpos;
				m2.endpos = m.endpos;
				m2.x = m.x;
				m2.y = m.y;
				m2.z = m.z;
				m2.vao = m.vao;
				m2.rid = m.rid;
				m2.level = m.level;
				m2.lx = m.lx;
				m2.lz = m.lz;
				m2.ux = m.ux;
				m2.uz = m.uz;
				m2.zofx = (byte) (closestZoneX - zx);
				m2.zofz = (byte) (closestZoneZ - zz);
				m2.flags = AlphaModel.TEMP;
				m.flags |= AlphaModel.SKIP;
				z.alphaModels.add(m2);
			}
		}
	}
}
