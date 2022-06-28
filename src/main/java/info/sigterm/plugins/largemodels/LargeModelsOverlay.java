/*
 * Copyright (c) 2017, Kronos <https://github.com/KronosDesign>
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
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
package info.sigterm.plugins.largemodels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.Model;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.Renderable;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.WallObject;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
class LargeModelsOverlay extends Overlay
{
	private static final Font FONT = FontManager.getRunescapeFont().deriveFont(Font.BOLD, 16);
	private static final Color GREEN = new Color(0, 200, 83);
	private static final Color ORANGE = new Color(255, 109, 0);
	private static final Color YELLOW = new Color(255, 214, 0);
	private static final Color PURPLE = new Color(170, 0, 255);
	private static final Color GRAY = new Color(158, 158, 158);

	private final Client client;
	private final LargeModelsConfig config;

	private int faceCount;

	@Inject
	private LargeModelsOverlay(Client client, LargeModelsConfig config)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(OverlayPriority.HIGHEST);
		this.client = client;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		graphics.setFont(FONT);

		faceCount = config.numFaces();

		renderNpcs(graphics);
		renderTileObjects(graphics);

		return null;
	}

	private void renderNpcs(Graphics2D graphics)
	{
		List<NPC> npcs = client.getNpcs();
		for (NPC npc : npcs)
		{
			NPCComposition composition = npc.getComposition();
			Color color = composition.getCombatLevel() > 1 ? YELLOW : ORANGE;
			if (composition.getConfigs() != null)
			{
				NPCComposition transformedComposition = composition.transform();
				if (transformedComposition == null)
				{
					color = GRAY;
				}
				else
				{
					composition = transformedComposition;
				}
			}

			Model m = npc.getModel();
			if (m == null || m.getFaceCount() <= faceCount)
			{
				continue;
			}

			String text = composition.getName() + " " + m.getFaceCount();
			OverlayUtil.renderActorOverlay(graphics, npc, text, color);
		}
	}

	private void renderTileObjects(Graphics2D graphics)
	{
		Scene scene = client.getScene();
		Tile[][][] tiles = scene.getTiles();

		int z = client.getPlane();
		boolean ground = config.groundObjects();
		boolean game = config.gameObjects();
		boolean walls = config.walls();
		boolean deco = config.decorations();

		for (int x = 0; x < Constants.SCENE_SIZE; ++x)
		{
			for (int y = 0; y < Constants.SCENE_SIZE; ++y)
			{
				Tile tile = tiles[z][x][y];

				if (tile == null)
				{
					continue;
				}

				Player player = client.getLocalPlayer();
				if (player == null)
				{
					continue;
				}

				if (ground)
				{
					renderGroundObject(graphics, tile.getGroundObject(), PURPLE);
				}

				if (game)
				{
					renderGameObjects(graphics, tile);
				}

				if (walls)
				{
					renderWallObject(graphics, tile.getWallObject(), GRAY);
				}

				if (deco)
				{
					renderDecorObject(graphics, tile);
				}
			}
		}
	}

	private void renderGameObjects(Graphics2D graphics, Tile tile)
	{
		GameObject[] gameObjects = tile.getGameObjects();
		if (gameObjects != null)
		{
			for (GameObject gameObject : gameObjects)
			{
				if (gameObject != null && gameObject.getSceneMinLocation().equals(tile.getSceneLocation()))
				{
					StringBuilder stringBuilder = new StringBuilder();
					Model m = renderableToModel(gameObject.getRenderable());

					if (m == null || m.getFaceCount() <= faceCount)
					{
						continue;
					}

					stringBuilder.append(m.getFaceCount());

					OverlayUtil.renderTileOverlay(graphics, gameObject, stringBuilder.toString(), GREEN);
				}
			}
		}
	}

	private void renderGroundObject(Graphics2D graphics, GroundObject groundObject, Color color)
	{
		if (groundObject != null)
		{
			Model m = renderableToModel(groundObject.getRenderable());
			if (m != null && m.getFaceCount() > faceCount)
			{
				OverlayUtil.renderTileOverlay(graphics, groundObject, Integer.toString(m.getFaceCount()), color);
			}
		}
	}

	private void renderWallObject(Graphics2D graphics, WallObject wallObject, Color color)
	{
		if (wallObject != null)
		{
			Model m = renderableToModel(wallObject.getRenderable1());
			if (m != null && m.getFaceCount() > faceCount)
			{
				OverlayUtil.renderTileOverlay(graphics, wallObject, Integer.toString(m.getFaceCount()), color);
			}
		}
	}

	private void renderDecorObject(Graphics2D graphics, Tile tile)
	{
		DecorativeObject decorObject = tile.getDecorativeObject();
		if (decorObject != null)
		{
			Model m = renderableToModel(decorObject.getRenderable());
			if (m != null && m.getFaceCount() > faceCount)
			{
				Shape p = decorObject.getConvexHull();
				if (p != null)
				{
					graphics.draw(p);
				}
			}

			m = renderableToModel(decorObject.getRenderable2());
			if (m != null && m.getFaceCount() > faceCount)
			{
				Shape p = decorObject.getConvexHull2();
				if (p != null)
				{
					graphics.draw(p);
				}
			}
		}
	}

	private static Model renderableToModel(Renderable r)
	{
		if (r == null)
		{
			return null;
		}
		else if (r instanceof Model)
		{
			return (Model) r;
		}
		else
		{
			return r.getModel();
		}
	}
}
