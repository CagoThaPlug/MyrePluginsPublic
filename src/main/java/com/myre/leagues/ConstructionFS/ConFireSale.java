package com.myre.leagues.ConstructionFS;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.Collections.Widgets;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.TileObjectInteraction;
import com.myre.utils.InputUtils;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemID;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;

import javax.inject.Inject;

public class ConFireSale extends Plugin {

    @Inject
    private Client client;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Override
    protected void startUp() throws Exception {
        System.out.println("Construction Fire Sale started.");
    }

    @Override
    protected void shutDown() throws Exception {
        System.out.println("Construction Fire Sale stopped.");
    }

    private int timeOut = 0;

    @Subscribe
    public void onGameTick() {
        if (client.getGameState() != GameState.LOGGED_IN)
            return;

        if (timeOut > 0) {
            timeOut--;
            return;
        }

        if(Inventory.search().withId(ItemID.FIRE_RUNE).first().isEmpty()) {
            sendGameMessage("No fire runes found.");
            EthanApiPlugin.stopPlugin(this);
        }

        if(Inventory.search().withId(ItemID.AIR_RUNE).first().isEmpty()) {
            sendGameMessage("No air runes found.");
            EthanApiPlugin.stopPlugin(this);
        }

        if(Inventory.search().withId(ItemID.EARTH_RUNE).first().isEmpty()) {
            sendGameMessage("No earth runes found.");
            EthanApiPlugin.stopPlugin(this);;
        }

        if(Inventory.search().withId(ItemID.WATER_RUNE).first().isEmpty()) {
            sendGameMessage("No water runes found.");
            EthanApiPlugin.stopPlugin(this);;
        }

        if (TileObjects.search().withName("Elemental balance space").first().isPresent() && Widgets.search().withTextContains("Furniture Creation Menu").first().isEmpty()) {
            TileObjectInteraction.interact(TileObjects.search().withName("Elemental balance space").first().get(), "Build");
        } else {
            if (Widgets.search().withTextContains("Really remove it?").first().isEmpty()) {
                InputUtils.typeString("3");
            }
        }

        if (Widgets.search().withTextContains("Really remove it?").first().isPresent()) {
            InputUtils.typeString("1");
            timeOut = 1;
        } else {
            if (TileObjects.search().withName("Elemental balance").first().isPresent() && TileObjects.search().withName("Elemental balance").withAction("remove").first().isPresent()) {
                TileObjectInteraction.interact(TileObjects.search().withName("Elemental balance").first().get(), "Remove");
            }
        }
    }

    private void sendGameMessage(String message) {

        String chatMessage = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(message)
                .build();

        chatMessageManager
                .queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(chatMessage)
                        .build());
    }
}
