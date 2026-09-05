/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.common.GameState
 *  com.netease.mc.mod.network.common.GameState$GameS
 *  com.netease.mc.mod.network.message.reply.Reply
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.level.GameType
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.friendplay.message.reply;

import com.netease.mc.mod.friendplay.FriendPlayMod;
import com.netease.mc.mod.network.common.GameState;
import com.netease.mc.mod.network.message.reply.Reply;
import com.netease.mc.mod.network.message.request.MessageRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.GameType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReplyStartExistSingle
extends Reply {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int SMID = 1538;

    public void handler(byte gameType, byte isCheat, byte isOnline, String save, String name) {
        int port;
        int result;
        block10: {
            result = 0;
            port = 0;
            if (GameState.gameState == GameState.GameS.INIT) {
                result = 1;
            } else if (GameState.gameState == GameState.GameS.LOAD) {
                try {
                    String type = FriendPlayMod.getGameType(gameType);
                    boolean cheat = isCheat == 1;
                    boolean online = isOnline == 1;
                    Minecraft mc = Minecraft.m_91087_();
                    Path p = mc.m_91392_().m_78257_().resolve(save);
                    List list = mc.m_91392_().m_289863_().m_289885_(p, true);
                    if (!list.isEmpty()) {
                        throw new IOException("Path " + save + " is not a directory");
                    }
                    if (mc.m_91392_().m_78255_(save)) {
                        mc.m_231466_().m_233133_(mc.f_91080_, save);
                        if (online) {
                            FriendPlayMod.setLanGameStates(online, GameType.m_46400_((String)type), cheat);
                            return;
                        }
                        GameState.gameState = GameState.GameS.SINGLE;
                        result = 0;
                        break block10;
                    }
                    result = 4;
                    GameState.gameState = GameState.GameS.LOAD;
                }
                catch (IOException e) {
                    GameState.gameState = GameState.GameS.LOAD;
                    result = 5;
                    LOGGER.error("ReplyStartExistSingle", (Throwable)e);
                }
                catch (Exception e) {
                    LOGGER.error("ReplyStartExistSingle", (Throwable)e);
                    GameState.gameState = GameState.GameS.LOAD;
                    result = 3;
                }
            } else {
                result = 2;
            }
        }
        MessageRequest mrq = new MessageRequest();
        mrq.send(1538, new Object[]{GameState.gameid, (byte)result, port});
    }
}

