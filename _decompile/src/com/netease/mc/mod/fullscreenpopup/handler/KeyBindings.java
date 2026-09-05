/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraftforge.client.event.RegisterKeyMappingsEvent
 */
package com.netease.mc.mod.fullscreenpopup.handler;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;

public class KeyBindings {
    public static KeyMapping showGameStore;

    public static void init() {
        showGameStore = new KeyMapping("key.gamestore", 293, "key.categories.gameplay");
        RegisterKeyMappingsEvent event = new RegisterKeyMappingsEvent(Minecraft.m_91087_().f_91066_);
        event.register(showGameStore);
    }
}

