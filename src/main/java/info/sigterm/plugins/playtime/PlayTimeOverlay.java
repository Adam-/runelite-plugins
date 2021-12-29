package info.sigterm.plugins.playtime;

import com.google.common.base.Strings;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class PlayTimeOverlay extends OverlayPanel
{
	private final PlayTimePlugin plugin;

	@Inject
	private PlayTimeOverlay(PlayTimePlugin plugin)
	{
		this.plugin = plugin;
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		String text = plugin.overlayText;
		if (Strings.isNullOrEmpty(text))
		{
			return null;
		}

		final FontMetrics fontMetrics = graphics.getFontMetrics();
		int panelWidth = Math.max(ComponentConstants.STANDARD_WIDTH, fontMetrics.stringWidth(text) +
			ComponentConstants.STANDARD_BORDER + ComponentConstants.STANDARD_BORDER);

		panelComponent.setPreferredSize(new Dimension(panelWidth, 0));
		panelComponent.getChildren().add(TitleComponent.builder()
			.text(text)
			.build());
		return super.render(graphics);
	}
}
