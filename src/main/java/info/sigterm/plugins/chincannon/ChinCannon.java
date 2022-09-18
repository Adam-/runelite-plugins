package info.sigterm.plugins.chincannon;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Projectile;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Chinbompa"
)
public class ChinCannon extends Plugin
{
	@Inject
	private Client client;

	@Subscribe
	public void onProjectileMoved(ProjectileMoved projectileMoved)
	{
		Projectile projectile = projectileMoved.getProjectile();
		if (projectile.getId() == 53) //cball
		{
			Projectile p = client.createProjectile(1272, //black chin
				projectile.getFloor(),
				projectile.getX1(), projectile.getY1(),
				projectile.getHeight(), // start height
				projectile.getStartCycle(), projectile.getEndCycle(),
				projectile.getSlope(),
				projectile.getStartHeight(), projectile.getEndHeight(),
				projectile.getInteracting(),
				projectile.getTarget().getX(), projectile.getTarget().getY());
			client.getProjectiles()
				.addLast(p);
			projectile.setEndCycle(0);
		}
		if (projectile.getId() == 1272 && projectile.getEndCycle() == client.getGameCycle())
		{
			Actor interacting = projectile.getInteracting();
			if (interacting != null && interacting.getGraphic() == -1)
			{
				interacting.setGraphic(157);
				interacting.setSpotAnimFrame(0);
			}
		}
	}
}
