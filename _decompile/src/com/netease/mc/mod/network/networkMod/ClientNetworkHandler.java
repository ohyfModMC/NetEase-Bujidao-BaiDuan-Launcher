/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.message.reply.MessageReply
 *  com.netease.mc.mod.network.socket.NetworkHandler
 *  com.netease.mc.mod.network.socket.NetworkSocket
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.network.networkMod;

import com.netease.mc.mod.network.message.reply.MessageReply;
import com.netease.mc.mod.network.socket.NetworkHandler;
import com.netease.mc.mod.network.socket.NetworkSocket;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientNetworkHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int SMIDLEN = 2;

    @SubscribeEvent
    public void onServerTick(TickEvent.ClientTickEvent event) {
        while (NetworkSocket.mRecvMsgQueue.count() > 0) {
            byte[] msg = (byte[])NetworkSocket.mRecvMsgQueue.pop();
            if (msg.length == 0) continue;
            int smid = this.getSidMid(msg);
            if (!NetworkHandler.replyHashMap.containsKey(smid)) {
                LOGGER.error("the msg is wrong " + smid + " " + msg);
                continue;
            }
            LOGGER.info("ClientNetworkHandler receive message: " + smid);
            ((MessageReply)NetworkHandler.replyHashMap.get(smid)).handMessage(msg);
        }
    }

    private int getSidMid(byte[] msg) {
        if (msg.length < 2) {
            return -1;
        }
        return msg[0] << 8 | msg[1];
    }
}

