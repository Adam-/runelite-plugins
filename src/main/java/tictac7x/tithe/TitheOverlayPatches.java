package tictac7x.tithe;

import tictac7x.Overlay;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.TileObject;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class TitheOverlayPatches extends Overlay {
    private final Client client;
    private final TithePlugin plugin;
    private final TitheConfig config;

    public TitheOverlayPatches(final TithePlugin plugin, final TitheConfig config, final Client client) {
        this.plugin = plugin;
        this.config = config;
        this.client = client;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
    }

    @Override
    public Dimension render(final Graphics2D graphics) {
        if (plugin.inTitheFarm()) {
            final MenuEntry[] menu_entries = client.getMenuEntries();

            for (final MenuEntry menu_entry : menu_entries) {
                final MenuAction menu_option = menu_entry.getType();
                if (menu_option == MenuAction.CANCEL || menu_option == MenuAction.WALK) continue;

                final TileObject object = findTileObject(client, menu_entry.getParam0(), menu_entry.getParam1(), menu_entry.getIdentifier());

                if (object != null && TithePlant.isPatch(object)) {
                    renderTile(graphics, object, config.getPatchesHighlightOnHoverColor());
                    break;
                }
            }
        }

        return null;
    }
}
