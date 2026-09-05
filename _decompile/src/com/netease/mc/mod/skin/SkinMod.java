/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.message.reply.MessageReply
 *  com.netease.mc.mod.network.socket.NetworkHandler
 */
package com.netease.mc.mod.skin;

import com.netease.mc.mod.network.message.reply.MessageReply;
import com.netease.mc.mod.network.socket.NetworkHandler;
import com.netease.mc.mod.skin.message.reply.LoadSkinReplyV2;

public class SkinMod {
    public static void init() {
        NetworkHandler.networkHandler.registerAsync(2050, (MessageReply)new LoadSkinReplyV2());
    }
}

