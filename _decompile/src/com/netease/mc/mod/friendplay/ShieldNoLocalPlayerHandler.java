/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.common.GameState
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.friendplay;

import com.netease.mc.mod.network.common.GameState;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShieldNoLocalPlayerHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public void OnPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer)event.getEntity();
        SocketAddress addr = player.f_8906_.f_9742_.m_129523_();
        LOGGER.info(player.m_5446_() + "/" + addr.toString() + " login");
        if (addr.toString().contains("local")) {
            return;
        }
        int port = ((InetSocketAddress)addr).getPort();
        String ip = ((InetSocketAddress)addr).getHostString();
        if (!GameState.acceptList.contains(port) || ip.compareTo("127.0.0.1") != 0) {
            MutableComponent textcomponentstring = Component.m_237113_((String)"\u8bf7\u767b\u5f55\u6211\u7684\u4e16\u754c\u542f\u52a8\u5668\u540e\u8fde\u63a5\u6e38\u620f!");
            player.f_8906_.m_9942_((Component)textcomponentstring);
        }
    }

    @SubscribeEvent
    public void OnPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        try {
            ServerPlayer player = (ServerPlayer)event.getEntity();
            SocketAddress addr = player.f_8906_.f_9742_.m_129523_();
            LOGGER.info(player.m_5446_() + "/" + addr.toString() + " logout");
            if (addr.toString().contains("local")) {
                return;
            }
            int port = ((InetSocketAddress)addr).getPort();
            if (GameState.acceptList.contains(port)) {
                GameState.acceptList.remove(port);
            }
        }
        catch (Exception e) {
            LOGGER.error("OnPlayerLoggedOut", (Throwable)e);
        }
    }
}

