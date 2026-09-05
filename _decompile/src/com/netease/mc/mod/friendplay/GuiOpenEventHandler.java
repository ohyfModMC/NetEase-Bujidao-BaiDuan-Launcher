/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.common.Common
 *  com.netease.mc.mod.network.common.GameState
 *  com.netease.mc.mod.network.common.GameState$GameS
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.DisconnectedScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.TitleScreen
 *  net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraftforge.client.event.ScreenEvent$Init
 *  net.minecraftforge.client.event.ScreenEvent$MouseButtonPressed
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.friendplay;

import com.netease.mc.mod.friendplay.FriendPlayMod;
import com.netease.mc.mod.network.common.Common;
import com.netease.mc.mod.network.common.GameState;
import com.netease.mc.mod.network.message.request.MessageRequest;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiOpenEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    long first_main_menu_time = 0L;

    @SubscribeEvent
    public void InitButtonGUI(ScreenEvent.Init event) {
        Screen gui = event.getScreen();
        if (null == gui) {
            return;
        }
        if (gui instanceof TitleScreen) {
            long current = Common.getSystemTimeStamp();
            if (this.first_main_menu_time > 0L) {
                if (current - this.first_main_menu_time > 5L && GameState.gameState != GameState.GameS.INIT) {
                    LOGGER.error("MainMenuOpen for the second time, this is not allowed");
                }
                return;
            }
            LOGGER.info("MainMenuOpen for the first time, request Launcher to create world");
            this.first_main_menu_time = Common.getSystemTimeStamp();
            GameState.gameState = GameState.GameS.LOAD;
            MessageRequest mrq = new MessageRequest();
            mrq.send(1281, new Object[]{GameState.gameid});
        } else if (gui instanceof JoinMultiplayerScreen) {
            LOGGER.error("MultiplayerScreen is not allowed");
        } else if (gui instanceof DisconnectedScreen) {
            FriendPlayMod.IsDisconnect = true;
            List widgets = event.getListenersList();
            for (GuiEventListener reconnect : widgets) {
                if (!(reconnect instanceof Button)) continue;
                ((Button)reconnect).m_93666_((Component)Component.m_237113_((String)"\u91cd\u65b0\u8fde\u63a5"));
                break;
            }
            LOGGER.info("set the first button in GuiDisconnected");
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public void OnMouseClick(ScreenEvent.MouseButtonPressed event) {
        if (event.getScreen() instanceof DisconnectedScreen) {
            LOGGER.info("DisconnectedScreen mouse click redirect: reconnect to server");
            MessageRequest mrq = new MessageRequest();
            mrq.send(1282, new Object[]{GameState.gameid});
            event.setCanceled(true);
        }
    }
}

