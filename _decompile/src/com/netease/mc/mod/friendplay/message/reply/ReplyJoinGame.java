/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.common.GameState
 *  com.netease.mc.mod.network.common.GameState$GameS
 *  com.netease.mc.mod.network.message.reply.Reply
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.ConnectScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.TitleScreen
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.client.multiplayer.resolver.ServerAddress
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.friendplay.message.reply;

import com.netease.mc.mod.network.common.GameState;
import com.netease.mc.mod.network.message.reply.Reply;
import com.netease.mc.mod.network.message.request.MessageRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReplyJoinGame
extends Reply {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int SMID = 1799;
    public static long timeStamp = 0L;
    public String lastIp;
    public int lastPort;
    public String lastRoom;
    public static ReplyJoinGame lastReply = null;
    private static int reconnectNum = 0;
    public static final int MAX_RECOONECT = 3;

    public void handler(String ip, int port, String room) {
        lastReply = this;
        this.lastIp = ip;
        this.lastPort = port;
        this.lastRoom = room;
        byte result = 0;
        if (GameState.gameState == GameState.GameS.INIT) {
            result = 1;
        } else if (GameState.gameState == GameState.GameS.LOAD) {
            try {
                Minecraft mc = Minecraft.m_91087_();
                LOGGER.info(String.format("ReplyJoinGame, room: %s, port: %d", room, port));
                ServerData serverData = new ServerData(room, ip + ":" + port, true);
                ConnectScreen.m_278792_((Screen)new TitleScreen(), (Minecraft)mc, (ServerAddress)ServerAddress.m_171864_((String)serverData.f_105363_), (ServerData)serverData, (boolean)false);
                timeStamp = System.currentTimeMillis();
                result = 0;
            }
            catch (Exception e) {
                LOGGER.error("ReplyJoinGame", (Throwable)e);
                GameState.gameState = GameState.GameS.LOAD;
                result = 3;
            }
        } else {
            result = 2;
        }
        MessageRequest mrq = new MessageRequest();
        mrq.send(1799, new Object[]{result, GameState.gameid});
    }

    public static void reconnect() {
        if (lastReply == null) {
            return;
        }
        if (++reconnectNum <= 3) {
            LOGGER.info("reconnect join room:" + reconnectNum);
            lastReply.handler(ReplyJoinGame.lastReply.lastIp, ReplyJoinGame.lastReply.lastPort, ReplyJoinGame.lastReply.lastRoom);
        }
    }
}

