package com.myre.main.LS;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;

@PluginDescriptor(
        name = "Live Debug",
        description = "Debugs live",
        tags = {"debug", "live", "myre", "cago"}
)
public class LiveDebugPlugin extends Plugin {
    
    @Inject
    private Client client;
    
    @Inject
    private ChatMessageManager  chatMessageManager;
    
    @Override
    protected void startUp() throws Exception {
        sendGameMessage("Live Debug started.");
    }
    
    @Override
    protected void shutDown() throws Exception {
        sendGameMessage("Live Debug stopped.");
    }

    @Subscribe
    public void onGameTick() {
        if (client.getGameState() != GameState.LOGGED_IN)
            return;


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
