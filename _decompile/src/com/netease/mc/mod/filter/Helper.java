/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.filter;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Helper {
    protected static final Logger logger = LogManager.getLogger();
    private static final boolean enableLog = false;

    public static void debugChatLog(String msg) {
    }

    public static void debugLog(String msg) {
    }

    public static void printAtChannelChat(String msg) {
        logger.info("printAtChannelChat:" + msg);
        Minecraft mc = Minecraft.m_91087_();
        MutableComponent msgComponentTranslation = Component.m_237113_((String)msg);
        mc.f_91065_.m_93076_().m_93785_((Component)msgComponentTranslation);
    }
}

