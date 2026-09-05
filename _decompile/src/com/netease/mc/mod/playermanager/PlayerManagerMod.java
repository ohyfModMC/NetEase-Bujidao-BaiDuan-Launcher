/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.message.reply.MessageReply
 *  com.netease.mc.mod.network.socket.NetworkHandler
 *  net.minecraftforge.common.MinecraftForge
 */
package com.netease.mc.mod.playermanager;

import com.netease.mc.mod.network.message.reply.MessageReply;
import com.netease.mc.mod.network.socket.NetworkHandler;
import com.netease.mc.mod.playermanager.EventHandlerClient;
import com.netease.mc.mod.playermanager.reply.ReplySearchPlayerListByName;
import net.minecraftforge.common.MinecraftForge;

public class PlayerManagerMod {
    public static void init() {
        NetworkHandler.networkHandler.register(1041, (MessageReply)new ReplySearchPlayerListByName());
        MinecraftForge.EVENT_BUS.register((Object)new EventHandlerClient());
    }
}

