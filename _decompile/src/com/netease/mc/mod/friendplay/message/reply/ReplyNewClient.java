/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.common.GameState
 *  com.netease.mc.mod.network.message.reply.Reply
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.friendplay.message.reply;

import com.netease.mc.mod.network.common.GameState;
import com.netease.mc.mod.network.message.reply.Reply;
import com.netease.mc.mod.network.message.request.MessageRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReplyNewClient
extends Reply {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int SMID = 1793;

    public void handler(int port) {
        LOGGER.info(String.format("ReplyNewClient add accept port: %d", port));
        byte result = 0;
        if (!GameState.acceptList.contains(port)) {
            GameState.acceptList.add(port);
        }
        MessageRequest mrq = new MessageRequest();
        mrq.send(1793, new Object[]{GameState.gameid, result});
    }
}

