package tictac7x.tithe;

import tictac7x.Overlay;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
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
        if (plugin.inTitheFarm() && config.highlightPatchesOnHover()) {
            final MenuEntry[] menu_entries = client.getMenuEntries();
            if (menu_entries.length != 0) {
                final MenuEntry entry = menu_entries[menu_entries.length - 1];
                final TileObject object = findTileObject(client, entry.getParam0(), entry.getParam1(), entry.getIdentifier());

                if (object != null && TithePlant.isPatch(object)) {
                    renderTile(graphics, object, config.getPatchesColor());
                }
            }
        }

        return null;
    }
}
