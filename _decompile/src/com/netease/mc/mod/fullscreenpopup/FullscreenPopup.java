/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.common.MinecraftForge
 */
package com.netease.mc.mod.fullscreenpopup;

import com.netease.mc.mod.fullscreenpopup.ToggleFullscreenTransformer;
import com.netease.mc.mod.fullscreenpopup.handler.EnterWorldEventHandler;
import com.netease.mc.mod.fullscreenpopup.handler.KeyBindings;
import com.netease.mc.mod.fullscreenpopup.handler.KeyInputEventHandler;
import net.minecraftforge.common.MinecraftForge;

public class FullscreenPopup {
    public static void init() {
        MinecraftForge.EVENT_BUS.register((Object)new KeyInputEventHandler());
        MinecraftForge.EVENT_BUS.register((Object)new EnterWorldEventHandler());
        KeyBindings.init();
        ToggleFullscreenTransformer.disable = false;
    }
}

