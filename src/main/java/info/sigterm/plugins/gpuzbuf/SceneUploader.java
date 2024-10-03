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

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Constants;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.Model;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Renderable;
import net.runelite.api.Scene;
import net.runelite.api.SceneTileModel;
import net.runelite.api.SceneTilePaint;
import net.runelite.api.Tile;
import net.runelite.api.WallObject;
import info.sigterm.plugins.gpuzbuf.regions.Regions;

@Singleton
@Slf4j
class SceneUploader
{
	private final GpuPluginConfig gpuConfig;

	private final Regions regions;

	@Inject
	SceneUploader(
		GpuPluginConfig config
	)
	{
		this.gpuConfig = config;

		try (var in = SceneUploader.class.getResourceAsStream("regions/regions.txt"))
		{
			regions = new Regions(in, "regions.txt");
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	void zoneSize(Scene scene, Zone zone, int mzx, int mzz) {
		Tile[][][] tiles = scene.getExtendedTiles();

		int basex = mzx << 10, basez = mzz << 10;
		for (int z = 3; z >= 0; --z)
		{
			for (int xoff = 0; xoff < 8; ++xoff)
			{
				for (int zoff = 0; zoff < 8; ++zoff)
				{
					Tile t = tiles[z][(mzx << 3) + xoff][(mzz << 3) + zoff];
					if (t != null)
					{
						zoneSize(scene, zone, t, basex, basez);
					}
				}
			}
		}
	}

	void uploadZone(Scene scene, Zone zone, int mzx, int mzz)
	{
		int[][][] roofs = scene.getRoofs();
		Set<Integer> roofIds = new HashSet<>();

		var vb = zone.vboO != null ? new GpuIntBuffer(zone.vboO.vb) : null ;
		var ab = zone.vboA != null? new GpuIntBuffer(zone.vboA.vb) : null ;

		for (int level =0 ; level <= 3; ++level)
		{
			for (int xoff = 0; xoff < 8; ++xoff)
			{
				for (int zoff = 0; zoff < 8; ++zoff)
				{
					int rid = roofs[level][(mzx << 3) + xoff][(mzz << 3) + zoff];
					if(rid>0)
						roofIds.add(rid);
				}
			}
		}

		zone.rids = new int[4][roofIds.size()];
		zone.roofStart = new int[4][roofIds.size()];
		zone.roofEnd = new int[4][roofIds.size()];
		zone.roofStartA = new int[4][roofIds.size()];
		zone.roofEndA = new int[4][roofIds.size()];

		for (int z = 0; z <= 3; ++z)
		{
			if (z == 0)
			{
				uploadZoneLevel(scene, zone, mzx, mzz, z, false, roofIds, vb, ab);
				uploadZoneLevel(scene, zone, mzx, mzz, z, true, roofIds, vb, ab);
				uploadZoneLevel(scene, zone, mzx, mzz, 1, true, roofIds, vb, ab);
				uploadZoneLevel(scene, zone, mzx, mzz, 2, true, roofIds, vb, ab);
				uploadZoneLevel(scene, zone, mzx, mzz, 3, true, roofIds, vb, ab);
			}
			else
			{
				uploadZoneLevel(scene, zone, mzx, mzz, z, false, roofIds, vb, ab);
			}

			if (zone.vboO != null)
			{
				int pos = zone.vboO.vb.position();
				zone.levelOffsets[z] = pos;
			}

			if (zone.vboA != null)
			{
				int pos = zone.vboA.vb.position();
				zone.levelOffsetsA[z] = pos;
			}
		}
	}

	private void uploadZoneLevel(Scene scene, Zone zone, int mzx, int mzz, int level, boolean visbelow, Set<Integer> roofIds, GpuIntBuffer vb, GpuIntBuffer ab) {
		int ridx = 0;

		// upload the roofs and save their positions
		for (int id : roofIds) {
			int pos = zone.vboO != null ? zone.vboO.vb.position() : 0;
			int posa = zone.vboA != null ? zone.vboA.vb.position() : 0;

			uploadZoneLevelRoof(scene, mzx, mzz, level, id, visbelow, vb, ab);

			int endpos = zone.vboO != null ? zone.vboO.vb.position() : 0;
			int endposa = zone.vboA != null?  zone.vboA.vb.position() : 0;

			if (endpos>pos || endposa>posa)
			{
				zone.rids[level][ridx] = id;
				zone.roofStart[level][ridx] = pos;
				zone.roofEnd[level][ridx] = endpos;
				zone.roofStartA[level][ridx] = posa;
				zone.roofEndA[level][ridx]=endposa;
				++ridx;
			}
		}

		// upload everything else
		uploadZoneLevelRoof(scene, mzx, mzz, level, 0, visbelow, vb, ab);
	}

	private void uploadZoneLevelRoof(Scene scene, int mzx, int mzz, int level, int roofId, boolean visbelow, GpuIntBuffer vb, GpuIntBuffer ab) {
		byte[][][] settings = scene.getExtendedTileSettings();
		int[][][] roofs = scene.getRoofs();
		Tile[][][] tiles = scene.getExtendedTiles();
		int basex = mzx << 10, basez = mzz << 10;

		for (int xoff = 0; xoff < 8; ++xoff)
		{
			for (int zoff = 0; zoff < 8; ++zoff)
			{
				int msx = (mzx << 3) + xoff;
				int msz = (mzz << 3) + zoff;

				boolean isbridge = (settings[1][msx][msz] & Constants.TILE_FLAG_BRIDGE) != 0;
				int maplevel = level;
				if (isbridge)
				{
					++maplevel;
				}

				boolean isvisbelow = maplevel <= 3 && (settings[maplevel][msx][msz] & Constants.TILE_FLAG_VIS_BELOW) != 0;
				int rid;
				if (isvisbelow || maplevel == 0)
				{
					rid = 0;
				}
				else
				{
					rid = roofs[maplevel - 1][msx][msz];
				}

				if (isvisbelow != visbelow) continue;

				if (rid == roofId)
				{
					Tile t = tiles[level][msx][msz];
					if (t != null)
					{
						uploadZoneTile(scene, t, vb, ab, basex, basez);
					}
				}
			}
		}
	}

	private void zoneSize(Scene scene, Zone z, Tile t,  int basex, int basez) {

		SceneTilePaint paint = t.getSceneTilePaint();
		if (paint != null)
		{
			z.sizeO += 2;
		}

		SceneTileModel model = t.getSceneTileModel();
		if (model != null) {
			z.sizeO +=  model.getFaceX().length;
		}

		WallObject wallObject = t.getWallObject();
		if (wallObject != null)
		{
			Renderable renderable1 = wallObject.getRenderable1();
			if (renderable1 instanceof Model)
			{
				zoneModelSize(z, (Model) renderable1);
			}

			Renderable renderable2 = wallObject.getRenderable2();
			if (renderable2 instanceof Model)
			{
				zoneModelSize(z, (Model) renderable2);
			}
		}

		DecorativeObject decorativeObject = t.getDecorativeObject();
		if (decorativeObject != null)
		{
			Renderable renderable = decorativeObject.getRenderable();
			if (renderable instanceof Model)
			{
				zoneModelSize(z, (Model) renderable);
			}

			Renderable renderable2 = decorativeObject.getRenderable2();
			if (renderable2 instanceof Model)
			{
				zoneModelSize(z, (Model) renderable2);
			}
		}

		GroundObject groundObject = t.getGroundObject();
		if (groundObject != null)
		{
			Renderable renderable = groundObject.getRenderable();
			if (renderable instanceof Model)
			{
				zoneModelSize(z, (Model) renderable);
			}
		}

		GameObject[] gameObjects = t.getGameObjects();
		for (GameObject gameObject : gameObjects)
		{
			if (gameObject == null)
			{
				continue;
			}

			if (!gameObject.getSceneMinLocation().equals(t.getSceneLocation())) continue;

			Renderable renderable = gameObject.getRenderable();
			if (renderable instanceof Model)
			{
					zoneModelSize(z, (Model) renderable);
			}
		}

		Tile bridge = t.getBridge();
		if (bridge != null) {
			 zoneSize(scene, z, bridge, basex, basez);
		}
	}

	private int uploadZoneTile(Scene scene, Tile t, GpuIntBuffer vertexBuffer, GpuIntBuffer ab, int basex, int basez) {
		int len = 0;

		SceneTilePaint paint = t.getSceneTilePaint();
		if (paint != null)
		{
			Point tilePoint = t.getSceneLocation();
			len = upload(scene, paint,
				t.getRenderLevel(), tilePoint.getX(), tilePoint.getY(),
				vertexBuffer, ab,
				tilePoint.getX() * 128 - basex, tilePoint.getY() * 128 - basez
				);
		}

		SceneTileModel model = t.getSceneTileModel();
		if (model != null)
		{
			int len_ = upload(model,
				basex, basez,
				vertexBuffer, ab, true);
			len += len_;
		}

		WallObject wallObject = t.getWallObject();
		if (wallObject != null)
		{
			Renderable renderable1 = wallObject.getRenderable1();
			if (renderable1 instanceof Model)
			{
				pushZoneModel0((Model) renderable1, wallObject.getX()-basex, wallObject.getZ(), wallObject.getY()-basez, vertexBuffer, ab);
			}

			Renderable renderable2 = wallObject.getRenderable2();
			if (renderable2 instanceof Model)
			{
				pushZoneModel0((Model) renderable2, wallObject.getX()-basex, wallObject.getZ(), wallObject.getY()-basez, vertexBuffer, ab);
			}
		}

		DecorativeObject decorativeObject = t.getDecorativeObject();
		if (decorativeObject != null)
		{
			Renderable renderable = decorativeObject.getRenderable();
			if (renderable instanceof Model)
			{
				pushZoneModel0((Model) renderable, decorativeObject.getX()-basex, decorativeObject.getZ(), decorativeObject.getY()-basez, vertexBuffer, ab);
			}

			Renderable renderable2 = decorativeObject.getRenderable2();
			if (renderable2 instanceof Model)
			{
				pushZoneModel0((Model) renderable2, decorativeObject.getX()-basex, decorativeObject.getZ(), decorativeObject.getY()-basez, vertexBuffer, ab);
			}
		}

		GroundObject groundObject = t.getGroundObject();
		if (groundObject != null)
		{
			Renderable renderable = groundObject.getRenderable();
			if (renderable instanceof Model)
			{
				pushZoneModel0((Model) renderable, groundObject.getX()-basex, groundObject.getZ(), groundObject.getY()-basez, vertexBuffer, ab);
			}
		}

		GameObject[] gameObjects = t.getGameObjects();
		for (GameObject gameObject : gameObjects)
		{
			if (gameObject == null)
			{
				continue;
			}

			if (!gameObject.getSceneMinLocation().equals(t.getSceneLocation())) continue;

			Renderable renderable = gameObject.getRenderable();
			if (renderable instanceof Model)
			{
				pushZoneModel0((Model) renderable, gameObject.getX()-basex, gameObject.getZ(), gameObject.getY()-basez, vertexBuffer, ab);
			}
		}

		Tile bridge = t.getBridge();
		if (bridge != null) {
			len += uploadZoneTile(scene, bridge, vertexBuffer, ab, basex, basez);
		}

		return len;
	}

	private void zoneModelSize(Zone z, Model m) {
		byte[] transparencies = m.getFaceTransparencies();
		short[] faceTextures = m.getFaceTextures();
		int faceCount = m.getFaceCount();
		if (transparencies != null || faceTextures != null) {
			for (int face = 0; face < faceCount; ++face) {
				boolean atex = (transparencies != null && transparencies[face] != 0);
				if (atex) z.sizeA++; else z.sizeO++;
			}
			return;
		}
		z.sizeO += faceCount;
	}

	private void pushZoneModel0(Model model, int x, int y, int z, GpuIntBuffer vertexBuffer, GpuIntBuffer ab)
	{
		uploadModelScene(model, x, y, z, vertexBuffer, ab);
	}

	private int upload(Scene scene, SceneTilePaint tile, int tileZ, int tileX, int tileY, GpuIntBuffer vertexBuffer, GpuIntBuffer ab,
		int lx, int lz)
	{
		tileX += scene.getWorldViewId() ==-1? GpuPlugin.SCENE_OFFSET:0;
		tileY += scene.getWorldViewId() == -1 ?GpuPlugin.SCENE_OFFSET:0;

		final int[][][] tileHeights = scene.getTileHeights();
		final int swHeight = tileHeights[tileZ][tileX][tileY];
		final int seHeight = tileHeights[tileZ][tileX + 1][tileY];
		final int neHeight = tileHeights[tileZ][tileX + 1][tileY + 1];
		final int nwHeight = tileHeights[tileZ][tileX][tileY + 1];

		final int swColor = tile.getSwColor();
		final int seColor = tile.getSeColor();
		final int neColor = tile.getNeColor();
		final int nwColor = tile.getNwColor();

		if (neColor == 12345678)
		{
			return 0;
		}

		// 0,0
		final int lx0 = lx;
		final int ly0 = swHeight;
		final int lz0 = lz;
		final int hsl0 = swColor;

		// 1,0
		final int lx1 = lx + Perspective.LOCAL_TILE_SIZE;
		final int ly1 = seHeight;
		final int lz1 = lz;
		final int hsl1 = seColor;

		// 1,1
		final int lx2 = lx + Perspective.LOCAL_TILE_SIZE;
		final int ly2 = neHeight;
		final int lz2 = lz + Perspective.LOCAL_TILE_SIZE;
		final int hsl2 = neColor;

		// 0,1
		final int lx3 = lx;
		final int ly3 = nwHeight;
		final int lz3 = lz + Perspective.LOCAL_TILE_SIZE;
		final int hsl3 = nwColor;

		int tex = tile.getTexture() + 1;

		vertexBuffer.put22224(lx2, ly2, lz2, hsl2);
		if (tile.isFlat()) vertexBuffer.put2(tex, lx0-lx2, ly0-ly2, lz0-lz2); else vertexBuffer.put2(tex, lx2-lx2, ly2-ly2, lz2-lz2);

		vertexBuffer.put22224(lx3, ly3, lz3, hsl3);
		if (tile.isFlat()) vertexBuffer.put2(tex, lx1-lx3, ly1-ly3, lz1-lz3); else vertexBuffer.put2(tex, lx3-lx3, ly3-ly3, lz3-lz3);


		vertexBuffer.put22224( lx1, ly1, lz1, hsl1);
		if (tile.isFlat()) vertexBuffer.put2(tex, lx3-lx1, ly3-ly1, lz3-lz1); else vertexBuffer.put2(tex, lx1-lx1, ly1-ly1, lz1-lz1);

		vertexBuffer.put22224( lx0, ly0, lz0, hsl0);
		vertexBuffer.put2(tex, lx0-lx0, ly0-ly0, lz0-lz0);

		vertexBuffer.put22224( lx1, ly1, lz1, hsl1);
		vertexBuffer.put2(tex, lx1-lx1, ly1-ly1, lz1-lz1);

		vertexBuffer.put22224( lx3, ly3, lz3, hsl3);
		vertexBuffer.put2(tex, lx3-lx3, ly3-ly3, lz3-lz3);

		return 6;
	}

	private int upload(SceneTileModel sceneTileModel, int lx, int lz,
		GpuIntBuffer vertexBuffer, GpuIntBuffer uvBuffer, boolean stream)
	{
		final int[] faceX = sceneTileModel.getFaceX();
		final int[] faceY = sceneTileModel.getFaceY();
		final int[] faceZ = sceneTileModel.getFaceZ();

		final int[] vertexX = sceneTileModel.getVertexX();
		final int[] vertexY = sceneTileModel.getVertexY();
		final int[] vertexZ = sceneTileModel.getVertexZ();

		final int[] triangleColorA = sceneTileModel.getTriangleColorA();
		final int[] triangleColorB = sceneTileModel.getTriangleColorB();
		final int[] triangleColorC = sceneTileModel.getTriangleColorC();

		final int[] triangleTextures = sceneTileModel.getTriangleTextureId();

		final int faceCount = faceX.length;

//		vertexBuffer.ensureCapacity(faceCount * 12*2);
//		uvBuffer.ensureCapacity(faceCount * 12*2);

		int cnt = 0;
		for (int i = 0; i < faceCount; ++i)
		{
			final int vertex0 = faceX[i];
			final int vertex1 = faceY[i];
			final int vertex2 = faceZ[i];

			final int hsl0 = triangleColorA[i];
			final int hsl1 = triangleColorB[i];
			final int hsl2 = triangleColorC[i];

			if (hsl0 == 12345678)
			{
				continue;
			}

			cnt += 3;

			// vertexes are stored in scene local, convert to tile local
			int lx0 = vertexX[vertex0] - lx;
			int ly0 = vertexY[vertex0];
			int lz0 = vertexZ[vertex0] - lz;

			int lx1 = vertexX[vertex1] - lx;
			int ly1 = vertexY[vertex1];
			int lz1 = vertexZ[vertex1] - lz;

			int lx2 = vertexX[vertex2] - lx;
			int ly2 = vertexY[vertex2];
			int lz2 = vertexZ[vertex2] - lz;

			int tex = triangleTextures != null ? triangleTextures[i] + 1 : 0;
			vertexBuffer.put22224( lx0, ly0, lz0, hsl0);
//			if (sceneTileModel.isFlat()) vertexBuffer.put(tex, vertexX[0] - lx, vertexY[0], vertexZ[0] - lz); else vertexBuffer.put(tex, vertexX[vertex0] - lx, vertexY[vertex0], vertexZ[vertex0] - lz);
//			vertexBuffer.put(0); vertexBuffer.put(0);
			if (sceneTileModel.isFlat()) vertexBuffer.put2(tex, vertexX[0] - lx - lx0, vertexY[0] - ly0, vertexZ[0] - lz - lz0); else vertexBuffer.put2(tex, vertexX[vertex0] - lx - lx0, vertexY[vertex0] - ly0, vertexZ[vertex0] - lz - lz0);

			vertexBuffer.put22224( lx1, ly1, lz1, hsl1);
//			if (sceneTileModel.isFlat()) vertexBuffer.put(tex, vertexX[1] - lx, vertexY[1], vertexZ[1] - lz);else vertexBuffer.put(tex, vertexX[vertex1] - lx, vertexY[vertex1], vertexZ[vertex1] - lz);
//			vertexBuffer.put(0); vertexBuffer.put(0);
			if (sceneTileModel.isFlat()) vertexBuffer.put2(tex, vertexX[1] -lx - lx1, vertexY[1]-ly1, vertexZ[1] -lz - lz1);else vertexBuffer.put2(tex, vertexX[vertex1]-lx - lx1, vertexY[vertex1]-ly1, vertexZ[vertex1] -lz - lz1);

			vertexBuffer.put22224( lx2, ly2, lz2, hsl2);
//			if (sceneTileModel.isFlat()) vertexBuffer.put(tex, vertexX[3] - lx, vertexY[3], vertexZ[3] - lz); else vertexBuffer.put(tex, vertexX[vertex2] - lx, vertexY[vertex2], vertexZ[vertex2] - lz);
//			vertexBuffer.put(0); vertexBuffer.put(0);
			if (sceneTileModel.isFlat()) vertexBuffer.put2(tex, vertexX[3] -lx - lx2, vertexY[3]-ly2, vertexZ[3] -lz - lz2); else vertexBuffer.put2(tex, vertexX[vertex2] -lx - lx2, vertexY[vertex2]-ly2, vertexZ[vertex2] -lz - lz2);
		}

		return cnt;
	}

	// scene upload
	private int uploadModelScene(Model model, int x, int y, int z, GpuIntBuffer vertexBuffer, GpuIntBuffer ab)
	{
		final int triangleCount = model.getFaceCount();

		final float[] vertexX = model.getVerticesX();
		final float[] vertexY = model.getVerticesY();
		final float[] vertexZ = model.getVerticesZ();

		final int[] indices1 = model.getFaceIndices1();
		final int[] indices2 = model.getFaceIndices2();
		final int[] indices3 = model.getFaceIndices3();

		final int[] color1s = model.getFaceColors1();
		final int[] color2s = model.getFaceColors2();
		final int[] color3s = model.getFaceColors3();

		final short[] faceTextures = model.getFaceTextures();
		final byte[] textureFaces = model.getTextureFaces();
		final int[] texIndices1 = model.getTexIndices1();
		final int[] texIndices2 = model.getTexIndices2();
		final int[] texIndices3 = model.getTexIndices3();

		final byte[] transparencies = model.getFaceTransparencies();
//		final byte[] facePriorities = model.getFaceRenderPriorities();

		int len = 0;
		for (int face = 0; face < triangleCount; ++face)
		{
			int color1 = color1s[face];
			int color2 = color2s[face];
			int color3 = color3s[face];

			boolean atex = (transparencies != null && transparencies[face] != 0);
			GpuIntBuffer vb = atex?ab:vertexBuffer;

			if (color3 == -1)
			{
				color2 = color3 = color1;
			}
			else if (color3 == -2)
			{
				continue;
			}

			int triangleA = indices1[face];
			int triangleB = indices2[face];
			int triangleC = indices3[face];

			int vx1 = (int) vertexX[triangleA];
			int vy1 = (int) vertexY[triangleA];
			int vz1 = (int) vertexZ[triangleA];

			int vx2 = (int) vertexX[triangleB];
			int vy2 = (int) vertexY[triangleB];
			int vz2 = (int) vertexZ[triangleB];

			int vx3 = (int) vertexX[triangleC];
			int vy3 = (int) vertexY[triangleC];
			int vz3 = (int) vertexZ[triangleC];

			vx1 += x;
			vx2 += x;
			vx3 += x;

			vy1 += y;
			vy2 += y;
			vy3 += y;

			vz1 += z;
			vz2 += z;
			vz3 += z;

			int texA, texB, texC;

			if (textureFaces != null && textureFaces[face] != -1)
			{
				int tface = textureFaces[face] & 0xff;
				texA = texIndices1[tface];
				texB = texIndices2[tface];
				texC = texIndices3[tface];
			}
			else
			{
				texA = triangleA;
				texB = triangleB;
				texC = triangleC;
			}

			int packedAlpha = faceAlpha(faceTextures, transparencies, face) << 24;
			int texture = faceTextures != null ? faceTextures[face] + 1 : 0;

			vb.put22224(vx1, vy1, vz1, packedAlpha | color1);
			vb.put2(texture, (int) vertexX[texA] + x - vx1, (int) vertexY[texA] + y - vy1, (int) vertexZ[texA] + z - vz1);

			vb.put22224(vx2, vy2, vz2, packedAlpha | color2);
			vb.put2(texture, (int) vertexX[texB] + x - vx2, (int) vertexY[texB] + y - vy2, (int) vertexZ[texB] + z - vz2);

			vb.put22224(vx3, vy3, vz3, packedAlpha | color3);
			vb.put2(texture, (int) vertexX[texC] + x - vx3, (int) vertexY[texC] + y - vy3, (int) vertexZ[texC] + z - vz3);

			len += 3;
		}

		return len;
	}

	// temp draw
	public int uploadModelTemp(Model model, int orientation, int x, int y, int z, IntBuffer opaqueBuffer, IntBuffer alphaBuffer)
	{
		final int triangleCount = model.getFaceCount();
		final int vertexCount = model.getVerticesCount();

		final float[] verticesX = model.getVerticesX();
		final float[] verticesY = model.getVerticesY();
		final float[] verticesZ = model.getVerticesZ();

		final int[] indices1 = model.getFaceIndices1();
		final int[] indices2 = model.getFaceIndices2();
		final int[] indices3 = model.getFaceIndices3();

		final int[] color1s = model.getFaceColors1();
		final int[] color2s = model.getFaceColors2();
		final int[] color3s = model.getFaceColors3();

		final short[] faceTextures = model.getFaceTextures();
		final byte[] textureFaces = model.getTextureFaces();
		final int[] texIndices1 = model.getTexIndices1();
		final int[] texIndices2 = model.getTexIndices2();
		final int[] texIndices3 = model.getTexIndices3();

		final byte[] transparencies = model.getFaceTransparencies();
//		final byte[] facePriorities = model.getFaceRenderPriorities();

		final byte overrideAmount = model.getOverrideAmount();
		final byte overrideHue = model.getOverrideHue();
		final byte overrideSat = model.getOverrideSaturation();
		final byte overrideLum = model.getOverrideLuminance();

		float orientSine = 0;
		float orientCosine = 0;
		if (orientation != 0)
		{
			orientSine = Perspective.SINE[orientation] / 65536f;
			orientCosine = Perspective.COSINE[orientation] / 65536f;
		}

		for (int v = 0; v < vertexCount; ++v) {
			float vertexX = verticesX[v];
			float vertexY = verticesY[v];
			float vertexZ = verticesZ[v];

			if (orientation != 0)
			{
				float x0 = vertexX;
				vertexX = vertexZ * orientSine + x0 * orientCosine;
				vertexZ = vertexZ * orientCosine - x0 * orientSine;
			}

			vertexX += x; vertexY += y; vertexZ += z;

			modelLocalX[v] = vertexX;
			modelLocalY[v] = vertexY;
			modelLocalZ[v] = vertexZ;
		}

		int len = 0;
		for (int face = 0; face < triangleCount; ++face)
		{
			int color1 = color1s[face];
			int color2 = color2s[face];
			int color3 = color3s[face];

			boolean atex = (transparencies != null && transparencies[face] != 0);

			if (color3 == -1)
			{
				color2 = color3 = color1;
			}
			else if (color3 == -2)
			{
				continue;
			}

			// HSL override is not applied to textured faces
			if (faceTextures == null || faceTextures[face] == -1)
			{
				if (overrideAmount > 0)
				{
					color1 = interpolateHSL(color1, overrideHue, overrideSat, overrideLum, overrideAmount);
					color2 = interpolateHSL(color2, overrideHue, overrideSat, overrideLum, overrideAmount);
					color3 = interpolateHSL(color3, overrideHue, overrideSat, overrideLum, overrideAmount);
				}
			}

			int triangleA = indices1[face];
			int triangleB = indices2[face];
			int triangleC = indices3[face];

			float vx1 = modelLocalX[triangleA];
			float vy1 = modelLocalY[triangleA];
			float vz1 = modelLocalZ[triangleA];

			float vx2 = modelLocalX[triangleB];
			float vy2 = modelLocalY[triangleB];
			float vz2 = modelLocalZ[triangleB];

			float vx3 = modelLocalX[triangleC];
			float vy3 = modelLocalY[triangleC];
			float vz3 = modelLocalZ[triangleC];

			int texA, texB, texC;

			if (textureFaces != null && textureFaces[face] != -1)
			{
				int tface = textureFaces[face] & 0xff;
				texA = texIndices1[tface];
				texB = texIndices2[tface];
				texC = texIndices3[tface];
			}
			else
			{
				texA = triangleA;
				texB = triangleB;
				texC = triangleC;
			}

			int packedAlpha = faceAlpha(faceTextures, transparencies, face) << 24;
			int texture = faceTextures != null ? faceTextures[face] + 1 : 0;

			var vb = atex?alphaBuffer:opaqueBuffer;

			put(vb,vx1,vy1,vz1, packedAlpha | color1);
			put2222(vb,texture, (int)modelLocalX[texA]-(int)vx1, (int)modelLocalY[texA]-(int)vy1, (int)modelLocalZ[texA]-(int)vz1);

			put(vb,vx2,vy2,vz2, packedAlpha | color2);
			put2222(vb,texture, (int)modelLocalX[texB]-(int)vx2, (int)modelLocalY[texB]-(int)vy2, (int)modelLocalZ[texB]-(int)vz2);

			put(vb,vx3,vy3,vz3, packedAlpha | color3);
			put2222(vb,texture, (int)modelLocalX[texC]-(int)vx3, (int)modelLocalY[texC]-(int)vy3, (int)modelLocalZ[texC]-(int)vz3);

			len += 3;
		}

		return len;
	}

	private static void put2222(IntBuffer vb, int x, int y, int z, int w) {
		vb.put(((y & 0xffff) << 16) | (x&0xffff));
		vb.put(((w & 0xffff) << 16) | (z&0xffff));
	}

	private static void put(IntBuffer vb, int x, int y, int z, int w){
		vb.put(x); vb.put(y); vb.put(z);
		vb.put(w);
	}

	private static void put(IntBuffer vb, float x, float y, float z, int w)
	{
		vb.put(Float.floatToIntBits(x));
		vb.put(Float.floatToIntBits(y));
		vb.put(Float.floatToIntBits(z));
		vb.put(w);
	}

	private static float[] modelLocalX;
	private static float[] modelLocalY;
	private static float[] modelLocalZ;

	static final int MAX_VERTEX_COUNT = 6500;

	static {
		modelLocalX = new float[MAX_VERTEX_COUNT];
		modelLocalY = new float[MAX_VERTEX_COUNT];
		modelLocalZ = new float[MAX_VERTEX_COUNT];
	}

	private static int faceAlpha(short[] faceTextures, byte[] faceTransparencies, int face)
	{
		if (faceTransparencies != null && (faceTextures == null || faceTextures[face] == -1))
		{
			return faceTransparencies[face] & 0xFF;
		}
		return 0;
	}

	private static int interpolateHSL(int hsl, byte hue2, byte sat2, byte lum2, byte lerp)
	{
		int hue = hsl >> 10 & 63;
		int sat = hsl >> 7 & 7;
		int lum = hsl & 127;
		int var9 = lerp & 255;
		if (hue2 != -1)
		{
			hue += var9 * (hue2 - hue) >> 7;
		}

		if (sat2 != -1)
		{
			sat += var9 * (sat2 - sat) >> 7;
		}

		if (lum2 != -1)
		{
			lum += var9 * (lum2 - lum) >> 7;
		}

		return (hue << 10 | sat << 7 | lum) & 65535;
	}

	// remove tiles from the scene that are outside the current region
	void prepare(Scene scene)
	{
		if (scene.isInstance() || !gpuConfig.hideUnrelatedMaps())
		{
			return;
		}

		int baseX = scene.getBaseX() / 8;
		int baseY = scene.getBaseY() / 8;
		int centerX = baseX + 6;
		int centerY = baseY + 6;
		int centerId = regions.getRegionId(centerX, centerY);

		int r = Constants.EXTENDED_SCENE_SIZE / 16;
		for (int offx = -r; offx <= r; ++offx)
		{
			for (int offy = -r; offy <= r; ++offy)
			{
				int cx = centerX + offx;
				int cy = centerY + offy;
				int id = regions.getRegionId(cx, cy);
				if (id != centerId)
				{
					removeChunk(scene, cx, cy);
				}
			}
		}
	}

	private static void removeChunk(Scene scene, int cx, int cy)
	{
		int wx = cx * 8;
		int wy = cy * 8;
		int sx = wx - scene.getBaseX();
		int sy = wy - scene.getBaseY();
		int cmsx = sx + GpuPlugin.SCENE_OFFSET;
		int cmsy = sy + GpuPlugin.SCENE_OFFSET;
		Tile[][][] tiles = scene.getExtendedTiles();
		for (int x = 0; x < 8; ++x)
		{
			for (int y = 0; y < 8; ++y)
			{
				int msx = cmsx + x;
				int msy = cmsy + y;
				if (msx >= 0 && msx < Constants.EXTENDED_SCENE_SIZE && msy >= 0 && msy < Constants.EXTENDED_SCENE_SIZE)
				{
					for (int z = 0; z < Constants.MAX_Z; ++z)
					{
						Tile tile = tiles[z][msx][msy];
						if (tile != null)
						{
							scene.removeTile(tile);
						}
					}
				}
			}
		}
	}
}
