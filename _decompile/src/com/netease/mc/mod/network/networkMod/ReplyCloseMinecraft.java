/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.message.reply.Reply
 *  org.apache.logging.log4j.LogManager
 */
package com.netease.mc.mod.network.networkMod;

import com.netease.mc.mod.network.message.reply.Reply;
import org.apache.logging.log4j.LogManager;

public class ReplyCloseMinecraft
extends Reply {
    public static final int SMID = 1;

    public void handler() {
        LogManager.getLogger().info("CloseGame!!!!");
    }
}

