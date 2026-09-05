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

import com.netease.mc.mod.friendplay.FriendPlayMod;
import com.netease.mc.mod.friendplay.message.reply.ReplyJoinGame;
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

public class ReplyReconnect
extends Reply {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int SMID = 1796;

    public void handler(String ip, int port, String room, boolean isLanGame) {
        byte result = 0;
        if (!FriendPlayMod.IsDisconnect) {
            result = 1;
        } else {
            try {
                LOGGER.info(String.format("ReplyReconnect, room: %s, port: %d", room, port));
                Minecraft mc = Minecraft.m_91087_();
                ServerData serverData = new ServerData(room, ip + ":" + port, isLanGame);
                ConnectScreen.m_278792_((Screen)new TitleScreen(), (Minecraft)mc, (ServerAddress)ServerAddress.m_171864_((String)serverData.f_105363_), (ServerData)serverData, (boolean)false);
                if (isLanGame) {
                    ReplyJoinGame.lastReply = null;
                }
                result = 0;
                FriendPlayMod.IsDisconnect = false;
            }
            catch (Exception e) {
                LOGGER.error("ReplyReconnect", (Throwable)e);
                GameState.gameState = GameState.GameS.LOAD;
                result = 3;
            }
        }
        MessageRequest mrq = new MessageRequest();
        mrq.send(1796, new Object[]{result, GameState.gameid});
    }
}

