/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.common.GameState
 *  com.netease.mc.mod.network.common.GameState$GameS
 *  com.netease.mc.mod.network.message.reply.MessageReply
 *  com.netease.mc.mod.network.socket.NetworkHandler
 *  net.minecraftforge.common.MinecraftForge
 */
package com.netease.mc.mod.network.networkMod;

import com.netease.mc.mod.network.common.GameState;
import com.netease.mc.mod.network.message.reply.MessageReply;
import com.netease.mc.mod.network.networkMod.ClientNetworkHandler;
import com.netease.mc.mod.network.networkMod.ReplyCloseMinecraft;
import com.netease.mc.mod.network.socket.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;

public class NetworkSocketMod {
    public static void init() {
        GameState.gameState = GameState.GameS.INIT;
        NetworkHandler.networkHandler.register(1, (MessageReply)new ReplyCloseMinecraft());
        MinecraftForge.EVENT_BUS.register((Object)NetworkHandler.networkHandler);
        MinecraftForge.EVENT_BUS.register((Object)new ClientNetworkHandler());
    }
}

