package com.myre.main.herbi;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("herbi++")
public interface HerbiAfkConfig extends Config
{

	@ConfigSection(
			position = 0,
			name = "Trail",
			description = "Trail settings"
	)
	String trailSection = "trailSection";
	@ConfigItem(
			position = 1,
			keyName = "showPathLine",
			name = "Show path lines",
			description = "Show trail path lines on the world.",
			section = trailSection
	)
	default boolean showPathLine()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
			position = 2,
			keyName = "lineColor",
			name = "Path line color",
			description = "Color of the trail path lines.",
			section = trailSection
	)
	default Color getLineColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
			position = 3,
			keyName = "showMiniMapArrow",
			name = "Show arrow on the minimap",
			description = "Show an arrow on the minimap to the next search spot.",
			section = trailSection
	)
	default boolean showMiniMapArrow()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
			position = 4,
			keyName = "arrowColor",
			name = "Minimap arrow color",
			description = "Color of the arrow on the minimap.",
			section = trailSection
	)
	default Color getArrowColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
			position = 5,
			keyName = "showMiniMaplines",
			name = "Show path lines on the minimap",
			description = "Show the trail path lines on the minimap.",
			section = trailSection
	)
	default boolean showMiniMaplines()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
			position = 6,
			keyName = "minimapPathColor",
			name = "Minimap path lines color",
			description = "Color of the trail path lines on the minimap.",
			section = trailSection
	)
	default Color getMinimapPathColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
			position = 7,
			keyName = "highlightHerbiTile",
			name = "Highlight herbiboar tile",
			description = "Highlights herbiboar tile at the end of the trail.",
			section = trailSection
	)
	default boolean highlightHerbiTile()
	{
		return false;
	}

	@ConfigItem(
			position = 8,
			keyName = "highlightHerbiHull",
			name = "Highlight herbiboar hull",
			description = "Highlights herbiboar hull at the end of the trail.",
			section = trailSection
	)
	default boolean highlightHerbiHull()
	{
		return true;
	}

	@ConfigItem(
			position = 9,
			keyName = "highlightHerbiOutline",
			name = "Highlight herbiboar outline",
			description = "Highlights herbiboar outline at the end of the trail.",
			section = trailSection
	)
	default boolean highlightHerbiOutline()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
			position = 10,
			keyName = "herbiboarColor",
			name = "Herbiboar highlight color",
			description = "Color of the herbiboar highlight.",
			section = trailSection
	)
	default Color getHerbiboarColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
			position = 11,
			keyName = "pathRelativeToPlayer",
			name = "Path relative to player",
			description = "Make the trail path line relative to the player.",
			section = trailSection
	)
	default boolean pathRelativeToPlayer()
	{
		return true;
	}

}
