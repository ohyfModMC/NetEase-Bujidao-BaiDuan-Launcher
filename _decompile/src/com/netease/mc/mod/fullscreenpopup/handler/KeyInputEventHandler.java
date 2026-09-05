/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyMapping
 *  net.minecraftforge.client.event.InputEvent$Key
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package com.netease.mc.mod.fullscreenpopup.handler;

import com.netease.mc.mod.fullscreenpopup.ToggleFullscreenTransformer;
import com.netease.mc.mod.fullscreenpopup.handler.KeyBindings;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KeyInputEventHandler {
    @SubscribeEvent
    public void handleKeyInputEvent(InputEvent.Key event) {
        if (KeyInputEventHandler.isKeyPressed(event, KeyBindings.showGameStore) && !KeyInputEventHandler.isAltDown(event)) {
            ToggleFullscreenTransformer.showGameStorePopup();
        }
    }

    public static boolean isKeyPressed(InputEvent.Key event, KeyMapping keyBinding) {
        if (event.getAction() != 1) {
            return false;
        }
        int keyCode = keyBinding.getKey().m_84873_();
        return keyCode == event.getKey() || keyCode == event.getScanCode();
    }

    public static boolean isKeyPressed(InputEvent.Key event, int keyCode) {
        if (event.getAction() != 1) {
            return false;
        }
        return keyCode == event.getKey() || keyCode == event.getScanCode();
    }

    public static boolean isAltDown(InputEvent.Key event) {
        int code = event.getModifiers() & 4;
        return code == 4;
    }
}

