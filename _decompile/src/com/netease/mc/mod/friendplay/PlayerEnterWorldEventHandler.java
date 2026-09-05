/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.common.GameState
 *  com.netease.mc.mod.network.common.GameState$GameS
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.util.HttpUtil
 *  net.minecraft.world.entity.Entity
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.friendplay;

import com.netease.mc.mod.friendplay.FriendPlayMod;
import com.netease.mc.mod.network.common.GameState;
import com.netease.mc.mod.network.message.request.MessageRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayerEnterWorldEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int SMID = 1537;
    public static boolean first_enter = true;
    public static boolean enterJoinServer = false;

    @SubscribeEvent
    public void PlayerEnterWorld(EntityJoinLevelEvent event) {
        if (!first_enter) {
            return;
        }
        Entity ent = event.getEntity();
        if (ent instanceof LocalPlayer) {
            first_enter = false;
            LocalPlayer player = (LocalPlayer)ent;
            LOGGER.info("PlayerEnterWorld: " + player.m_7755_());
            FriendPlayMod.LanGameState lgs = FriendPlayMod.getLanGameStates();
            if (lgs.isOnline) {
                boolean success;
                byte result = 0;
                Minecraft mc = Minecraft.m_91087_();
                int port = 0;
                port = HttpUtil.m_13939_();
                if (port <= 0) {
                    port = 25564;
                }
                if (!(success = mc.m_91092_().m_7386_(lgs.gameType, lgs.isCheat, port))) {
                    GameState.gameState = GameState.GameS.LOAD;
                    result = 3;
                } else {
                    GameState.gameState = GameState.GameS.SERVER;
                    result = 0;
                }
                MessageRequest mrq = new MessageRequest();
                mrq.send(1537, new Object[]{GameState.gameid, result, port});
            }
        }
    }
}

