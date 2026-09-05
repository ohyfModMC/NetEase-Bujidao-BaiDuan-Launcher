/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.server.IntegratedServer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.contents.TranslatableContents
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.client.event.ClientChatReceivedEvent
 *  net.minecraftforge.client.event.InputEvent$Key
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.opencommand;

import com.netease.mc.mod.fullscreenpopup.handler.KeyInputEventHandler;
import com.netease.mc.mod.opencommand.OpenCommandKeyBindings;
import com.netease.mc.mod.opencommand.reply.PermissionHandler;
import com.netease.mc.mod.opencommand.reply.ReplyGameRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameRuleEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private boolean loadSended = false;

    @SubscribeEvent
    public void OnClientChatReceivedEvent(ClientChatReceivedEvent event) {
        Component msgC = event.getMessage();
        if (msgC.m_214077_() instanceof TranslatableContents) {
            TranslatableContents msg = (TranslatableContents)msgC.m_214077_();
            String key = msg.m_237508_();
            Object[] args = msg.m_237523_();
            if ("commands.gamerule.set".equals(key)) {
                String des = (String)args[0];
                String res = (String)args[1];
                ReplyGameRules.SetGameRule(des, res);
                ReplyGameRules.SendGameRules();
            } else if ("gameMode.changed".equals(key) || "commands.defaultgamemode.success".equals(key) || "commands.gamemode.success.self".equals(key) || "commands.gamemode.success.other".equals(key)) {
                ReplyGameRules.SendGameRules();
            } else if ("permission".equals(key)) {
                PermissionHandler.SendPermission();
            } else if ("gamerule".equals(key) && args.length == ReplyGameRules.RuleNames.length) {
                for (int i = 0; i < args.length; ++i) {
                    ReplyGameRules.SetGameRule(ReplyGameRules.RuleNames[i], (String)args[i]);
                }
            }
        }
    }

    @SubscribeEvent
    public void OnEntityJoinWorldEvent(EntityJoinLevelEvent event) {
        if (this.loadSended) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Minecraft mc = Minecraft.m_91087_();
            if (mc.f_91074_ != null && mc.f_91074_.m_7755_().equals(entity.m_7755_())) {
                ReplyGameRules.SendLoadWorld();
                this.loadSended = true;
            }
        }
    }

    @SubscribeEvent
    public void OnPlayerLoginedEvent(PlayerEvent.PlayerLoggedInEvent event) {
        IntegratedServer server;
        Minecraft mc = Minecraft.m_91087_();
        if (mc.f_91074_ != null && !mc.f_91074_.m_7755_().equals(event.getEntity().m_7755_()) && (server = mc.m_91092_()) != null && server.m_6846_().m_11303_(event.getEntity().m_36316_())) {
            ServerPlayer player = server.m_6846_().m_11259_(event.getEntity().m_20148_());
            MutableComponent permission = Component.m_237115_((String)"permission");
            permission.m_130946_("true");
            player.m_5661_((Component)permission, true);
            MutableComponent gamerule = Component.m_237115_((String)"permission");
            for (String str : ReplyGameRules.GetGameRuleValue()) {
                gamerule.m_130946_(str);
            }
            player.m_5661_((Component)gamerule, true);
        }
    }

    @SubscribeEvent
    public void handleKeyInputEvent(InputEvent.Key event) {
        if (KeyInputEventHandler.isKeyPressed(event, OpenCommandKeyBindings.openCommand) && !KeyInputEventHandler.isAltDown(event)) {
            ReplyGameRules.SendShortcuts();
        }
    }
}

