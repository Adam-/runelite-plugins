package com.theatrepoints;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.util.Map;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import static com.theatrepoints.TheatrePointsConstant.*;
import net.runelite.client.util.Text;

public class TheatrePointsOverlay extends OverlayPanel {

    private Client client;
    private TheatrePointsPlugin plugin;
    private TheatrePointsConfig config;

    @Inject
    private TheatrePointsOverlay(TheatrePointsPlugin plugin, Client client, TheatrePointsConfig config) {
    	super(plugin);
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.TOP_RIGHT);

        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Theatre Points Overlay"));
    }


    @Override
    public Dimension render(Graphics2D graphics) {
        if (plugin.inRaid && plugin.loadedPlayers)
		{
			Player player = client.getLocalPlayer();
			if (player != null && player.getName() != null)
			{
				String name = Text.sanitize(player.getName());

				List<LayoutableRenderableEntity> elems = panelComponent.getChildren();
				elems.clear();

				Map<String, Integer> deaths = plugin.getDeathCounter();

				// These are the maximum number of points that a team could have.
				double maxPoints = (deaths.size() * POINTS_ENCOUNTER) + POINTS_MVP;

				// This is the total amount of deaths that the team has.
				double totalDeaths = 0;
				for (String pName : deaths.keySet())
					totalDeaths += (double) deaths.get(pName);

				// This is the amount of points left.
				double totalLosses = totalDeaths * POINTS_DEATH;
				double pointsRemaining = Math.max(0.0, maxPoints - totalLosses);

				// This is the factor multiplier.
				double multiplier = pointsRemaining / maxPoints;

				// This is the drop chance.
				double dropChance = BASE_RATE * multiplier;

				if (config.showTeamChance())
					elems.add(LineComponent.builder()
						.left("Team:")
						.right(String.format("%.2f%%", dropChance))
						.build());

				// This is your chance at the loot.
				double maxPersonal = POINTS_ENCOUNTER + (POINTS_MVP / deaths.size()); // Let's assume that MVP points are distributed evenly...

				// This is your own personal contribution out of the team's total contribution.
				double personalLosses = POINTS_DEATH * deaths.get(name);
				double personalDeaths = deaths.get(name);
				double personalRemaining = Math.max(0.0, maxPersonal - personalLosses);
				double personalChance = personalRemaining / pointsRemaining;

				if (config.showPersonalChance()) // P(X|Y), X = purple is in your name, Y = team sees a purple
					elems.add(LineComponent.builder()
						.left("Personal:")
						.right(String.format("%.2f%%", 100.0 * personalChance))
						.build());

				if (config.showYoinkChance()) // P(X&Y), team sees a purple in your name
					elems.add(LineComponent.builder()
						.left("Team & Personal:")
						.right(String.format("%.2f%%", dropChance * personalChance))
						.build());

				if (config.showPersonalDeathCount())
					elems.add(LineComponent.builder()
						.left("Personal Deaths:")
						.right(String.format("%d", (int) personalDeaths))
						.build());

				if (config.showDeathCount())
					elems.add(LineComponent.builder()
						.left("Total Deaths:")
						.right(String.format("%d", (int) totalDeaths))
						.build());
			}
		}

        return super.render(graphics);
    }
}
