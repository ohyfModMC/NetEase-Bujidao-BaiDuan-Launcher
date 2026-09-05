/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.HttpAuthenticationService
 *  com.netease.mc.mod.network.message.reply.MessageReply
 *  com.netease.mc.mod.network.socket.NetworkHandler
 *  javax.annotation.Nullable
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.client.multiplayer.ServerList
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.PacketUtils
 *  net.minecraft.network.protocol.game.ClientboundServerDataPacket
 *  net.minecraft.network.protocol.login.ClientboundHelloPacket
 *  net.minecraft.util.thread.BlockableEventLoop
 *  net.minecraft.world.level.GameType
 *  net.minecraftforge.common.MinecraftForge
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.friendplay;

import com.mojang.authlib.HttpAuthenticationService;
import com.netease.mc.mod.friendplay.GuiOpenEventHandler;
import com.netease.mc.mod.friendplay.PlayerEnterWorldEventHandler;
import com.netease.mc.mod.friendplay.ShieldNoLocalPlayerHandler;
import com.netease.mc.mod.friendplay.message.reply.ReplyJoinGame;
import com.netease.mc.mod.friendplay.message.reply.ReplyNewClient;
import com.netease.mc.mod.friendplay.message.reply.ReplyNewSingleV2;
import com.netease.mc.mod.friendplay.message.reply.ReplyOpScreenShot;
import com.netease.mc.mod.friendplay.message.reply.ReplyReconnect;
import com.netease.mc.mod.friendplay.message.reply.ReplyStartExistSingle;
import com.netease.mc.mod.network.message.reply.MessageReply;
import com.netease.mc.mod.network.socket.NetworkHandler;
import com.netease.mc.mod.oldInterface.IClientHandshakePacketListenerImplOld;
import java.io.IOException;
import java.net.URL;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FriendPlayMod {
    private static final Logger LOGGER = LogManager.getLogger();
    public static boolean IsDisconnect = false;
    private static LanGameState lanGameState;

    public static String performGetRequestWrapper(HttpAuthenticationService service, URL url, @Nullable String authentication) throws IOException {
        return null;
    }

    public static void handleServerDataWrapper(ClientPacketListener listener, ClientboundServerDataPacket packet) {
        Minecraft mc = Minecraft.m_91087_();
        PacketUtils.m_131363_((Packet)packet, (PacketListener)listener, (BlockableEventLoop)mc);
        if (listener.m_245416_() != null) {
            listener.m_245416_().f_105365_ = packet.m_271805_();
            packet.m_271815_().ifPresent(arg_0 -> ((ServerData)listener.m_245416_()).m_271813_(arg_0));
            listener.m_245416_().m_242965_(packet.m_242957_());
            ServerList.m_105446_((ServerData)listener.m_245416_());
        }
    }

    public static boolean isValidUsernameWrapper(String name) {
        return true;
    }

    public static void handleHelloWrapper(IClientHandshakePacketListenerImplOld old, ClientboundHelloPacket packet) {
        LogManager.getLogger().info("Client handleHello packet");
        old.handleHelloOld(packet);
    }

    public static void init() {
        NetworkHandler.networkHandler.register(1799, (MessageReply)new ReplyJoinGame());
        NetworkHandler.networkHandler.register(1538, (MessageReply)new ReplyStartExistSingle());
        NetworkHandler.networkHandler.register(1793, (MessageReply)new ReplyNewClient());
        NetworkHandler.networkHandler.register(1540, (MessageReply)new ReplyNewSingleV2());
        NetworkHandler.networkHandler.register(1796, (MessageReply)new ReplyReconnect());
        NetworkHandler.networkHandler.registerAsync(2305, (MessageReply)new ReplyOpScreenShot());
        MinecraftForge.EVENT_BUS.register((Object)new GuiOpenEventHandler());
        MinecraftForge.EVENT_BUS.register((Object)new PlayerEnterWorldEventHandler());
        MinecraftForge.EVENT_BUS.register((Object)new ShieldNoLocalPlayerHandler());
        MinecraftForge.EVENT_BUS.register((Object)new PlayerEnterWorldEventHandler());
    }

    public static LanGameState getLanGameStateObj() {
        if (lanGameState == null) {
            lanGameState = new LanGameState();
        }
        return lanGameState;
    }

    public static void setLanGameStates(boolean bonline, GameType gtype, boolean cheat) {
        FriendPlayMod.getLanGameStateObj();
        FriendPlayMod.lanGameState.isOnline = bonline;
        FriendPlayMod.lanGameState.gameType = gtype;
        FriendPlayMod.lanGameState.isCheat = cheat;
    }

    public static LanGameState getLanGameStates() {
        return FriendPlayMod.getLanGameStateObj();
    }

    public static String getGameType(byte b) {
        switch (b) {
            case 0: {
                return "survival";
            }
            case 1: {
                return "creative";
            }
            case 2: {
                return "hardcore";
            }
            case 3: {
                return "adventure";
            }
            case 4: {
                return "spectator";
            }
        }
        return "survival";
    }

    public static class LanGameState {
        public boolean isOnline = false;
        public GameType gameType = null;
        public boolean isCheat = false;
    }
}

