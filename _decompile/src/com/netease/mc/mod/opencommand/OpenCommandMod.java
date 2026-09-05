/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.message.reply.MessageReply
 *  com.netease.mc.mod.network.socket.NetworkHandler
 *  net.minecraftforge.common.MinecraftForge
 */
package com.netease.mc.mod.opencommand;

import com.netease.mc.mod.network.message.reply.MessageReply;
import com.netease.mc.mod.network.socket.NetworkHandler;
import com.netease.mc.mod.opencommand.GameRuleEventHandler;
import com.netease.mc.mod.opencommand.OpenCommandKeyBindings;
import com.netease.mc.mod.opencommand.reply.OpenCommandHandler;
import com.netease.mc.mod.opencommand.reply.PermissionHandler;
import com.netease.mc.mod.opencommand.reply.ReplyAllPlayersList;
import com.netease.mc.mod.opencommand.reply.ReplyGameRules;
import net.minecraftforge.common.MinecraftForge;

public class OpenCommandMod {
    public static void init() {
        OpenCommandKeyBindings.init();
        NetworkHandler.networkHandler.register(4097, (MessageReply)new ReplyAllPlayersList());
        NetworkHandler.networkHandler.register(4099, (MessageReply)new OpenCommandHandler());
        NetworkHandler.networkHandler.register(4100, (MessageReply)new ReplyGameRules());
        NetworkHandler.networkHandler.register(4102, (MessageReply)new PermissionHandler());
        MinecraftForge.EVENT_BUS.register((Object)new GameRuleEventHandler());
    }
}

