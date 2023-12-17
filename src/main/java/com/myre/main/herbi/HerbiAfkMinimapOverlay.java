package com.myre.main.herbi;

import com.myre.main.herbi.QuestHelperTools.DirectionArrow;
import com.myre.main.herbi.QuestHelperTools.WorldLines;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;

public class HerbiAfkMinimapOverlay extends Overlay {

    private final HerbiPlusPlus plugin;
    private final HerbiAfkConfig config;

    @Inject
    public HerbiAfkMinimapOverlay(HerbiPlusPlus plugin, HerbiAfkConfig config) {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!plugin.isInHerbiboarArea())
        {
            return null;
        }

        if (config.showMiniMapArrow() && plugin.getNextSearchSpot() != null) {
            DirectionArrow.renderMinimapArrow(graphics, plugin.getClient(), plugin.getNextSearchSpot(), config.getArrowColor());
        }

        if (config.showMiniMaplines() && plugin.getPathLinePoints() != null) {
            WorldLines.createMinimapLines(graphics, plugin.getClient(), plugin.getPathLinePoints(), config.getMinimapPathColor());
        }
        return null;
    }
}
