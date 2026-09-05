/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.message.reply.MessageReply
 *  com.netease.mc.mod.network.socket.NetworkHandler
 *  net.minecraftforge.common.MinecraftForge
 */
package com.netease.mc.mod.screenshot;

import com.netease.mc.mod.network.message.reply.MessageReply;
import com.netease.mc.mod.network.socket.NetworkHandler;
import com.netease.mc.mod.screenshot.ScreenShotHandler;
import com.netease.mc.mod.screenshot.reply.ReplyScreenshot;
import net.minecraftforge.common.MinecraftForge;

public class ScreenShotMod {
    public static void init() {
        MinecraftForge.EVENT_BUS.register((Object)new ScreenShotHandler());
        NetworkHandler.networkHandler.registerAsync(4612, (MessageReply)new ReplyScreenshot());
    }
}

