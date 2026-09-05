/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.client.event.ScreenEvent$BackgroundRendered
 *  net.minecraftforge.client.event.ScreenEvent$Init$Pre
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.lwjgl.glfw.GLFW
 */
package com.netease.mc.mod.fullscreenpopup.handler;

import com.netease.mc.mod.fullscreenpopup.ToggleFullscreenTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class EnterWorldEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private boolean graphicSended = false;
    private boolean first = true;
    private int width = 0;
    private int height = 0;
    private Minecraft mc = Minecraft.m_91087_();

    private void updateSize() {
        this.width = this.mc.m_91268_().m_85441_();
        this.height = this.mc.m_91268_().m_85442_();
    }

    @SubscribeEvent
    public void OnFirstDrawScreen(ScreenEvent.Init.Pre event) {
        if (this.first) {
            this.updateSize();
            GLFW.glfwSetWindowTitle((long)this.mc.m_91268_().m_85439_(), (CharSequence)"\u6211\u7684\u4e16\u754c 1.18.3");
            this.first = false;
        }
    }

    @SubscribeEvent
    public void OnFirstDrawScreen(ScreenEvent.BackgroundRendered event) {
        Minecraft mc = Minecraft.m_91087_();
        if (mc.m_91268_().m_85441_() != this.width || mc.m_91268_().m_85442_() != this.height) {
            this.updateSize();
            ToggleFullscreenTransformer.refreshPopupHead();
        }
    }

    @SubscribeEvent
    public void OnPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (this.graphicSended) {
            return;
        }
        Player player = event.getEntity();
        if (null != this.mc.f_91074_ && player.m_7755_().equals(this.mc.f_91074_.m_7755_())) {
            try {
                ToggleFullscreenTransformer.sendGraphicCard();
                this.graphicSended = true;
            }
            catch (Exception e) {
                LOGGER.error("sendGraphicCard", (Throwable)e);
            }
        }
    }
}

