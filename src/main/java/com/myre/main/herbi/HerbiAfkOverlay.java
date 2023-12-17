package com.myre.main.herbi;

import com.myre.main.herbi.QuestHelperTools.WorldLines;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;

public class HerbiAfkOverlay extends Overlay {

    private final HerbiPlusPlus plugin;
    private final HerbiAfkConfig config;

    @Inject
    public HerbiAfkOverlay(HerbiPlusPlus plugin, HerbiAfkConfig config) {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!plugin.isInHerbiboarArea())
        {
            return null;
        }

        if (config.showPathLine() && plugin.getPathLinePoints() != null) {
            WorldLines.drawLinesOnWorld(graphics, plugin.getClient(), plugin.getPathLinePoints(), config.getLineColor());
        }
        return null;
    }
}
