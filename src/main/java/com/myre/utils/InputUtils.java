package com.myre.utils;

import net.runelite.api.Client;

import java.awt.event.KeyEvent;

public class InputUtils {

    private Client client;

    public static void typeString(String string) {
        for (char c : string.toCharArray()) {
            pressKey(c);
        }
    }
    public void pressKey(char key) {
        keyEvent(401, key);
        keyEvent(402, key);
        keyEvent(400, key);
    }

    private void keyEvent(int id, char key) {
        KeyEvent e = new KeyEvent(client.getCanvas(), id, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, key);
        client.getCanvas().dispatchEvent(e);
    }
}
