package info.sigterm.plugins.smevent;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.JagexColor;
import net.runelite.api.Model;
import net.runelite.api.ModelData;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.Text;

public class PlayerOverlay extends Overlay
{
	private final Client client;
	private final SMEvent plugin;

	@Inject
	private PlayerOverlay(Client client, SMEvent plugin)
	{
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(PRIORITY_MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		for (var entry : plugin.players.values())
		{
			render(graphics, entry);
		}
		return null;
	}

	private void render(Graphics2D graphics, SMPlayer p)
	{
		Player player = p.player;
		PlayerConfig config = p.config;

		updateRlo(p);
		updateFont(graphics, p);

		String name = player.getName();
		if (name == null)
		{
			return;
		}

		name = Text.sanitize(player.getName());
		int zOffset = player.getLogicalHeight() + 40;
		graphics.setFont(p.font);
		Point textLocation = player.getCanvasTextLocation(graphics, name, zOffset);
		if (textLocation != null)
		{
			OverlayUtil.renderTextLocation(graphics, textLocation, name, config.color);
		}
	}

	private void updateRlo(SMPlayer p)
	{
		Player player = p.player;
		PlayerConfig config = p.config;

		if (p.rlo != null)
		{
			p.rlo.setLocation(player.getLocalLocation(), client.getPlane());
			return;
		}

		ModelData md = client.loadModelData(33196);
		if (md == null)
		{
			return;
		}

		var rlo = p.rlo = client.createRuneLiteObject();

		md.cloneVertices();
		md.translate(0, -25, 0);

		md.cloneColors();
		md.recolor((short) 939, JagexColor.rgbToHSL(config.color.getRGB(), 1.0d));

		Model m = md.light();
		rlo.setModel(m);
		rlo.setLocation(player.getLocalLocation(), client.getPlane());
		rlo.setActive(true);
	}

	private void updateFont(Graphics2D graphics, SMPlayer player)
	{
		if (player.font != null)
		{
			return;
		}

		player.font = graphics.getFont().deriveFont((float) player.config.textSize);
	}
}
