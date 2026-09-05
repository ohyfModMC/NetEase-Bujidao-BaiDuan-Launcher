/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.message.reply.Reply
 *  net.minecraft.client.Minecraft
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.opencommand.reply;

import com.netease.mc.mod.network.message.reply.Reply;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OpenCommandHandler
extends Reply {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int SMID = 4099;

    public void handler(String message) {
        LOGGER.info("sendChatMessage:" + message);
        Minecraft mc = Minecraft.m_91087_();
        mc.f_91074_.f_108617_.m_246623_(message.substring(1));
    }
}

