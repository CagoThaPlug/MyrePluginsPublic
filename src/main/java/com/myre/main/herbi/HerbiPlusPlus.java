package com.myre.main.herbi;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.Collections.Widgets;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.NPCPackets;
import com.example.Packets.WidgetPackets;
import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.npcoverlay.HighlightedNpc;
import net.runelite.client.game.npcoverlay.NpcOverlayService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.herbiboars.HerbiboarPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@PluginDescriptor(
        name = "Herbi++",
        description = "Herbiboar plugin with extra features - NO BANKING",
        tags = {"herbiboar", "hunter", "herbi", "herb", "boar",  "Cago", "Myre" , "EthanAPI"},
        enabledByDefault = false
)
@PluginDependency(HerbiboarPlugin.class)
public class HerbiPlusPlus extends Plugin {

    @Inject
    @Getter
    private Client client;

    @Inject
    private HerbiAfkConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private Notifier notifier;

    @Inject
    private HerbiAfkOverlay overlay;

    @Inject
    private HerbiAfkMinimapOverlay minimapOverlay;

    @Inject
    private HerbiboarPlugin herbiboarPlugin;

    @Inject
    private NpcOverlayService npcOverlayService;

    @Getter
    private List<WorldPoint> pathLinePoints = new ArrayList<>();
    @Getter
    private WorldPoint nextSearchSpot;

    private List<Widget> herbWidgets;
    private List<Widget> cleanHerbWidgets;

    private WorldPoint startLocation, endLocation;

    private enum HerbiState {
        IDLE,
        FINDING_START,
        INTERACTING_START,
        HUNTING,
        STUNNED,
        NOTING,
        CLEANING,
        STORING,
        DROPPING,
    }

    private static boolean varbitChanged = false;
    private HerbiState herbiState;

    private int finishedId = -1;
    private int closestIndex = 0;

    private List<Widget> herbsToNote;

    private int timeOut = 0;

    private static final List<WorldPoint> END_LOCATIONS = ImmutableList.of(
            new WorldPoint(3693, 3798, 0),
            new WorldPoint(3702, 3808, 0),
            new WorldPoint(3703, 3826, 0),
            new WorldPoint(3710, 3881, 0),
            new WorldPoint(3700, 3877, 0),
            new WorldPoint(3715, 3840, 0),
            new WorldPoint(3751, 3849, 0),
            new WorldPoint(3685, 3869, 0),
            new WorldPoint(3681, 3863, 0)
    );

    private static final List<WorldPoint> START_LOCATIONS = ImmutableList.of(
            new WorldPoint(3686, 3870, 0),
            new WorldPoint(3751, 3850, 0),
            new WorldPoint(3695, 3800, 0),
            new WorldPoint(3704, 3810, 0),
            new WorldPoint(3705, 3830, 0)
    );

    private static final String HERBI_STUN = "You stun the creature";
    private static final String HERBI_KC = "Your herbiboar harvest count is:";
    private static final String HERBIBOAR_NAME = "Herbiboar";
    private static final String HERBI_CIRCLES = "The creature has successfully confused you with its tracks, leading you round in circles";
    private static final Integer PATH_LINE_DIVISION = 10;

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(overlay);
        overlayManager.add(minimapOverlay);

        npcOverlayService.registerHighlighter(isHerbiboar);

        pathLinePoints = new ArrayList<>();
        min = /* random number between 1 and 15 */ (int) (Math.random() * 15) + 1;
        max = /* random number between 65 and 90 */ (int) (Math.random() * 25) + 65;

        herbiState = HerbiState.IDLE;
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        overlayManager.remove(minimapOverlay);
        npcOverlayService.unregisterHighlighter(isHerbiboar);

        resetTrailData();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        switch (event.getGameState()) {
            case HOPPING:
            case LOGGING_IN:
                resetTrailData();
                herbiState = HerbiState.IDLE;
                break;
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        varbitChanged = true;
    }


    public boolean isRunEnabled() {
        return client.getVarpValue(173) == 1;
    }

    private int min, max;

    @Subscribe
    public void onGameTick(GameTick event) {
        if (!isInHerbiboarArea()) {
            herbiState = HerbiState.IDLE;
            return;
        }

        if (herbiState != HerbiState.STUNNED) {
            if (isRunEnabled() && client.getEnergy() / 100 < min) {
                toggleRun();
            }
            if (!isRunEnabled() && client.getEnergy() / 100 > max) {
                toggleRun();
            }
        }

        if (timeOut > 0) {
            timeOut--;
            return;
        }

        Inventory.search().matchesWildCardNoCase("*fossil").first().ifPresent(fossil -> {
            herbiState = HerbiState.STORING;
        });

        //System.out.println("HerbiState: " + herbiState);

        Optional<NPC> herbiboar = NPCs.search().withId(NpcID.HERBIBOAR).first();

        switch (herbiState) {
            case FINDING_START:
             //   System.out.println("Finding Start");
                if (Inventory.getEmptySlots() <= 4) {
                    herbiState = HerbiState.CLEANING;
                    break;
                }

                if (client.getLocalPlayer() != null) {
                    startLocation = client.getLocalPlayer().getWorldLocation();
                }
                endLocation = getNearestStartLocation();
                if (endLocation != null) {
                    updatePathLinePoints(startLocation, endLocation);
                }

                if (varbitChanged) {
                    updateTrailData();
                    varbitChanged = false;
                }

                if (client.getLocalPlayer().getAnimation() == -1 && !EthanApiPlugin.isMoving()) {
                    Optional<TileObject> nearestStart = TileObjects.search().atLocation(endLocation).first();
                    if (nearestStart.isPresent() && client.getLocalPlayer().getAnimation() == -1) {
                        notifier.notify("Found Start", TrayIcon.MessageType.INFO);
                        herbiState = HerbiState.IDLE;
                    }

                }

                break;
            case HUNTING:
              //  System.out.println("Hunting");
                if (varbitChanged) {
                    updateTrailData();
                    varbitChanged = false;
                }

                if (Inventory.getEmptySlots() <= 4) {
                    herbiState = HerbiState.NOTING;
                    break;
                }

                if (herbiboar.isPresent()) {
                    herbiState = HerbiState.STUNNED;
                } else {
                    updateTrailData();
                }

                if (config.pathRelativeToPlayer()) {
                    if (client.getLocalPlayer() != null && pathLinePoints != null) {
                        startLocation = client.getLocalPlayer().getWorldLocation();
                        try {
                            updatePathLinePoints(startLocation, endLocation);
                        } catch (Exception e) {
                            herbiState = HerbiState.FINDING_START;
                        }
                    }
                }
                try {
                    if (client.getLocalPlayer().getWorldLocation().distanceTo(endLocation) <= 1) {
                        TileObjects.search().atLocation(endLocation).first().ifPresent(tileObject -> {
                            TileObjectInteraction.interact(tileObject, "Inspect", "Attack");
                        });
                    } else {
                        if (!EthanApiPlugin.isMoving()) {
                            MousePackets.queueClickPacket();
                            MovementPackets.queueMovement(endLocation.getX(), endLocation.getY(), false);
                        }
                    }
                } catch (Exception e) {
                    herbiState = HerbiState.FINDING_START;
                }
                break;

            case STUNNED:
              //  System.out.println("Stunned");
                if (isRunEnabled()) {
                    toggleRun();
                }
                if (herbiboar.isPresent()) {
                    min = /* random number between 1 and 15 */ (int) (Math.random() * 15) + 1;
                    max = /* random number between 65 and 90 */ (int) (Math.random() * 25) + 65;
                    updateTrailData();
                    npcOverlayService.rebuild();
                    MousePackets.queueClickPacket();
                    NPCPackets.queueNPCAction(getHerbiboarNpc(), "Harvest");
                } else {
                 //   System.out.println("We have harvested Herbi, now we clean.");
                    herbiState = HerbiState.CLEANING;
                }
                break;
            case CLEANING:
                Inventory.search().withIds(ItemID.GRIMY_GUAM_LEAF, ItemID.GRIMY_MARRENTILL, ItemID.GRIMY_TARROMIN).first().ifPresent(herb -> {
                    InventoryInteraction.useItem(herb, "Clean");
                  //  sendGameMessage("Cleaning " + herb.getName());
                });

                if (Inventory.search().withIds(ItemID.GRIMY_GUAM_LEAF, ItemID.GRIMY_MARRENTILL, ItemID.GRIMY_TARROMIN).empty())
                    herbiState = HerbiState.DROPPING;

                break;
            case DROPPING:
            //    System.out.println("Dropping");
                Inventory.search().withIds(ItemID.GUAM_LEAF, ItemID.MARRENTILL, ItemID.TARROMIN).first().ifPresent(guam -> {
                    InventoryInteraction.useItem(guam, "Drop");
                 //   sendGameMessage("Dropping " + guam.getName());
                });

                if (Inventory.search().withIds(ItemID.GUAM_LEAF, ItemID.TARROMIN, ItemID.MARRENTILL).empty()) {
                    if (Inventory.getEmptySlots() <= 4) {
                        herbiState = HerbiState.NOTING;
                    } else {
                        herbiState = HerbiState.HUNTING;
                    }
                }
                break;
            case IDLE:
               // System.out.println("Idle");
                if (varbitChanged) {
                    updateTrailData();
                    varbitChanged = false;
                }
                break;
            case STORING:
               // System.out.println("Storing");
                if (Inventory.search().matchesWildCardNoCase("*fossil").first().isEmpty()) {
                    herbiState = HerbiState.HUNTING;
                } else {
                    if (Widgets.search().withText("Fossil Storage").first().isEmpty()) {
                        if(TileObjects.search().withId(ObjectID.FOSSIL_STORAGE).first().isPresent()) {
                            TileObjectInteraction.interact(TileObjects.search().withId(ObjectID.FOSSIL_STORAGE).first().get(), "Open");
                        } else {
                            if(!EthanApiPlugin.isMoving()) {
                                MousePackets.queueClickPacket();
                                MovementPackets.queueMovement(3689, 3878, false);
                            }
                        }
                    } else {
                        Inventory.search().matchesWildCardNoCase("*fossil").first().ifPresent(fossil -> {
                          //  sendGameMessage("Storing " + fossil.getName());
                            // MenuOptionClicked(getParam0=9, getParam1=39976960, getMenuOption=Deposit All, getMenuTarget=<col=ff9040>Unidentified small fossil</col>, getMenuAction=CC_OP, getId=4)
                            EthanApiPlugin.invoke(fossil.getIndex(), 39976960, MenuAction.CC_OP.getId(), 4, -1, "Deposit All", "<col=ff9040>" + fossil.getName() + "</col>", 0, 0);
                        });
                    }
                }
                break;
            case NOTING:
               // System.out.println("Noting");
                Optional<NPC> leprechaun = NPCs.search().nameContains("Leprechaun").first();

                if (leprechaun.isPresent()) {
                    herbsToNote = Inventory.search().matchesWildCardNoCase("Grimy*").result();

                    // remove all herbs that are already noted
                    for (int i = 0; i < herbsToNote.size(); i++) {
                        if (Inventory.search().isNoted(herbsToNote.get(i))) {
                            herbsToNote.remove(i);
                            i--;
                        }
                    }

                    //remove duplicates from list
                    for (int i = 0; i < herbsToNote.size(); i++) {
                        for (int j = i + 1; j < herbsToNote.size(); j++) {
                            if (herbsToNote.get(i).getName().equals(herbsToNote.get(j).getName())) {
                                herbsToNote.remove(j);
                                j--;
                            }
                        }
                    }

                    for (Widget herb : herbsToNote) {
                      //  System.out.println("Herb: " + herb.getName());
                        if (!herbsToNote.isEmpty()) {
                            if (!Inventory.search().isNoted(herb)) {
                               // System.out.println("Noting " + herb.getName());
                              //  System.out.println("Herbs to note Size: " + herbsToNote.size());
                                NPCPackets.queueWidgetOnNPC(leprechaun.get(), herb);
                            }
                        }

                     //   System.out.println("Herbs to note Size: " + herbsToNote.size());
                    //    System.out.println("Removing Herb: " + herb.getName());
                        herbsToNote.remove(herb);
                    }
                } else {
                    if (!EthanApiPlugin.isMoving()) {
                        MousePackets.queueClickPacket();
                        MovementPackets.queueMovement(3710, 3836, false);
                        break;
                    }
                }

                if (herbsToNote.isEmpty()) {
                    herbiState = HerbiState.FINDING_START;
                }


                break;
        }

    }

    private void updateTrailData() {
        updateStartAndEndLocation();
        if (startLocation != null && endLocation != null) {
            updatePathLinePoints(startLocation, endLocation);
        }
    }

    private void updateStartAndEndLocation() {
        List<? extends Enum<?>> currentPath = herbiboarPlugin.getCurrentPath();
        int currentPathSize = currentPath.size();

        WorldPoint newStartLocation = null;
        WorldPoint newEndLocation = null;

        if (herbiState == HerbiState.STUNNED) {
            newStartLocation = END_LOCATIONS.get(finishedId - 1);
            NPC herbi = getHerbiboarNpc();
            if (herbi != null) {
                newEndLocation = herbi.getWorldLocation();
            }
        } else if (currentPathSize >= 1) {
            if (herbiboarPlugin.getFinishId() > 0) {
                newStartLocation = HerbiboarSearchSpot.valueOf(currentPath.get(currentPathSize - 1).toString()).getLocation();
                finishedId = herbiboarPlugin.getFinishId();
                newEndLocation = END_LOCATIONS.get(finishedId - 1);
            } else if (currentPathSize == 1) {
                newStartLocation = herbiboarPlugin.getStartPoint();
                newEndLocation = HerbiboarSearchSpot.valueOf(currentPath.get(0).toString()).getLocation();
            } else {
                newStartLocation = HerbiboarSearchSpot.valueOf(currentPath.get(currentPathSize - 2).toString()).getLocation();
                newEndLocation = HerbiboarSearchSpot.valueOf(currentPath.get(currentPathSize - 1).toString()).getLocation();
            }
        }

        if (newStartLocation != null && newEndLocation != null) {
            if (config.pathRelativeToPlayer()) {
                if (client.getLocalPlayer() != null) {
                    newStartLocation = client.getLocalPlayer().getWorldLocation();
                }
            }

            nextSearchSpot = newEndLocation;

            startLocation = newStartLocation;
            endLocation = newEndLocation;

            herbiState = HerbiState.HUNTING;
        }
    }

    private WorldPoint getNearestStartLocation() {
        WorldPoint neartestPoint = null;
        WorldPoint player = null;

        if (client.getLocalPlayer() != null) {
            player = client.getLocalPlayer().getWorldLocation();
        }
        if (player == null) {
            return null;
        }

        double shortestDistance = Double.MAX_VALUE;
        for (WorldPoint startPoint : START_LOCATIONS) {
            double distance = player.distanceTo2D(startPoint);
            if (distance < shortestDistance) {
                neartestPoint = startPoint;
                shortestDistance = distance;
            }
        }

        return neartestPoint;
    }

    private void updatePathLinePoints(WorldPoint start, WorldPoint end) {
        double distance = start.distanceTo2D(end);
        int divisions = (int) Math.ceil(distance / PATH_LINE_DIVISION);

        pathLinePoints.clear();
        pathLinePoints.add(start);

        if (divisions == 1) {
            pathLinePoints.add(end);
            return;
        }

        double angle = Math.atan2((end.getY() - start.getY()), (end.getX() - start.getX()));
        double deltaH = distance / divisions;
        int deltaX = (int) (deltaH * Math.cos(angle));
        int deltaY = (int) (deltaH * Math.sin(angle));

        int currentX = start.getX();
        int currentY = start.getY();

        for (int i = 1; i < divisions; i++) {
            currentX += deltaX;
            currentY += deltaY;
            pathLinePoints.add(new WorldPoint(currentX, currentY, 0));
        }

        pathLinePoints.add(end);
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() == ChatMessageType.GAMEMESSAGE) {
            String message = Text.sanitize(Text.removeTags(event.getMessage()));
            if (message.contains(HERBI_STUN)) {
                herbiState = HerbiState.STUNNED;
            } else if (message.contains(HERBI_KC) || message.contains(HERBI_CIRCLES)) {
                resetTrailData();
            }
        }
    }

    private NPC getHerbiboarNpc() {
        final NPC[] cachedNPCs = client.getCachedNPCs();
        for (NPC npc : cachedNPCs) {
            if (npc != null) {
                if (npc.getName() != null && npc.getName().equals(HERBIBOAR_NAME)) {
                    return npc;
                }
            }
        }
        return null;
    }

    public final Function<NPC, HighlightedNpc> isHerbiboar = (n) -> {
        boolean isHighlight = config.highlightHerbiHull() || config.highlightHerbiTile() || config.highlightHerbiOutline();
        if (isHighlight && n.getName() != null && n.getName().equals(HERBIBOAR_NAME)) {
            Color color = config.getHerbiboarColor();
            return HighlightedNpc.builder()
                    .npc(n)
                    .highlightColor(color)
                    .fillColor(ColorUtil.colorWithAlpha(color, color.getAlpha() / 12))
                    .hull(config.highlightHerbiHull())
                    .tile(config.highlightHerbiTile())
                    .outline(config.highlightHerbiOutline())
                    .build();
        }
        return null;
    };

    private void resetTrailData() {
        pathLinePoints.clear();

        nextSearchSpot = null;
        startLocation = null;
        endLocation = null;

        finishedId = -1;

        herbiState = HerbiState.FINDING_START;
    }

    public boolean isInHerbiboarArea() {
        return herbiboarPlugin.isInHerbiboarArea();
    }

    public void sendGameMessage(String message) {
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "{DEBUG}", message, null);
    }

    public void toggleRun() {
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetAction(client.getWidget(WidgetInfo.MINIMAP_TOGGLE_RUN_ORB), "Toggle Run");
    }

    @Provides
    HerbiAfkConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(HerbiAfkConfig.class);
    }
}
