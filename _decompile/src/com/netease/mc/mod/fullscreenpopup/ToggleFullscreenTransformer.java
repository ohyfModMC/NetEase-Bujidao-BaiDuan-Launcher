/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.blaze3d.platform.VideoMode
 *  com.mojang.blaze3d.platform.Window
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.opengl.GL11
 */
package com.netease.mc.mod.fullscreenpopup;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import com.netease.mc.mod.network.message.request.MessageRequest;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class ToggleFullscreenTransformer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static int m_showGameStorePopupCommand = 2305;
    private static int m_refreshPopupHeadCommand = 2306;
    private static int m_sendgraphicCardCommand = 2307;
    private static final Minecraft client = Minecraft.m_91087_();
    public static boolean fullscreen;
    public static boolean disable;
    public static KeyMapping ignoreKeyBinding;

    public static void showGameStorePopup() {
        LOGGER.info("showGameStorePopup");
        MessageRequest mrq = new MessageRequest();
        mrq.send(m_showGameStorePopupCommand, new Object[]{""});
    }

    public static void refreshPopupHead() {
        LOGGER.info("refreshPopupHead");
        MessageRequest mrq = new MessageRequest();
        mrq.send(m_refreshPopupHeadCommand, new Object[]{""});
    }

    static VideoMode getVideoModeOrDefault(Optional<VideoMode> p_197992_1_) {
        if (p_197992_1_.isPresent()) {
            VideoMode lvt_2_1_ = p_197992_1_.get();
            for (VideoMode lvt_4_1_ : Lists.reverse((List)ToggleFullscreenTransformer.client.m_91268_().m_85450_().f_84937_)) {
                if (!lvt_4_1_.equals((Object)lvt_2_1_)) continue;
                return lvt_4_1_;
            }
        }
        return client.m_91268_().m_85450_().m_84950_();
    }

    public static void updateVideoMode() {
        boolean hasMoniter;
        boolean bl = hasMoniter = GLFW.glfwGetWindowMonitor((long)client.m_91268_().m_85439_()) != 0L;
        if (fullscreen) {
            GLFW.glfwSetWindowAttrib((long)client.m_91268_().m_85439_(), (int)131077, (int)0);
            VideoMode videoMode = ToggleFullscreenTransformer.getVideoModeOrDefault(client.m_91268_().m_85436_());
            if (!hasMoniter) {
                ToggleFullscreenTransformer.client.m_91268_().f_85350_ = ToggleFullscreenTransformer.client.m_91268_().f_85357_;
                ToggleFullscreenTransformer.client.m_91268_().f_85351_ = ToggleFullscreenTransformer.client.m_91268_().f_85358_;
                ToggleFullscreenTransformer.client.m_91268_().f_85352_ = ToggleFullscreenTransformer.client.m_91268_().f_85359_;
                ToggleFullscreenTransformer.client.m_91268_().f_85353_ = ToggleFullscreenTransformer.client.m_91268_().f_85360_;
            }
            int[] xpos = new int[1];
            int[] ypos = new int[1];
            GLFW.glfwGetMonitorPos((long)client.m_91268_().m_85450_().m_84954_(), (int[])xpos, (int[])ypos);
            ToggleFullscreenTransformer.client.m_91268_().f_85357_ = xpos[0];
            ToggleFullscreenTransformer.client.m_91268_().f_85358_ = ypos[0];
            ToggleFullscreenTransformer.client.m_91268_().f_85359_ = videoMode.m_85332_();
            ToggleFullscreenTransformer.client.m_91268_().f_85360_ = videoMode.m_85335_();
            LOGGER.info(String.format("%d %d %d %d", ToggleFullscreenTransformer.client.m_91268_().f_85357_, ToggleFullscreenTransformer.client.m_91268_().f_85358_, ToggleFullscreenTransformer.client.m_91268_().f_85359_, ToggleFullscreenTransformer.client.m_91268_().f_85360_));
            GLFW.glfwFocusWindow((long)client.m_91268_().m_85439_());
            GLFW.glfwSetWindowMonitor((long)client.m_91268_().m_85439_(), (long)0L, (int)ToggleFullscreenTransformer.client.m_91268_().f_85357_, (int)ToggleFullscreenTransformer.client.m_91268_().f_85358_, (int)ToggleFullscreenTransformer.client.m_91268_().f_85359_, (int)ToggleFullscreenTransformer.client.m_91268_().f_85360_, (int)-1);
        } else {
            GLFW.glfwSetWindowAttrib((long)client.m_91268_().m_85439_(), (int)131077, (int)1);
            ToggleFullscreenTransformer.client.m_91268_().f_85357_ = ToggleFullscreenTransformer.client.m_91268_().f_85350_;
            ToggleFullscreenTransformer.client.m_91268_().f_85358_ = ToggleFullscreenTransformer.client.m_91268_().f_85351_;
            ToggleFullscreenTransformer.client.m_91268_().f_85359_ = ToggleFullscreenTransformer.client.m_91268_().f_85352_;
            ToggleFullscreenTransformer.client.m_91268_().f_85360_ = ToggleFullscreenTransformer.client.m_91268_().f_85353_;
            LOGGER.info(String.format("%d %d %d %d", ToggleFullscreenTransformer.client.m_91268_().f_85357_, ToggleFullscreenTransformer.client.m_91268_().f_85358_, ToggleFullscreenTransformer.client.m_91268_().f_85359_, ToggleFullscreenTransformer.client.m_91268_().f_85360_));
            GLFW.glfwSetWindowMonitor((long)client.m_91268_().m_85439_(), (long)0L, (int)ToggleFullscreenTransformer.client.m_91268_().f_85357_, (int)ToggleFullscreenTransformer.client.m_91268_().f_85358_, (int)ToggleFullscreenTransformer.client.m_91268_().f_85359_, (int)ToggleFullscreenTransformer.client.m_91268_().f_85360_, (int)-1);
        }
    }

    public static void toggleFullScreenWrapper(Window window) {
        if (disable) {
            ToggleFullscreenTransformer.client.m_91268_().f_85355_ = !ToggleFullscreenTransformer.client.m_91268_().f_85355_;
        } else {
            ToggleFullscreenTransformer.setFullscreenState(!fullscreen);
            ToggleFullscreenTransformer.refreshPopupHead();
        }
    }

    public static boolean isFullscreenWrapper(Window window) {
        if (disable) {
            return ToggleFullscreenTransformer.client.m_91268_().f_85355_;
        }
        return fullscreen;
    }

    public static void setFullscreenState(boolean state) {
        LOGGER.info(String.format("setFullscreenState: %b", state));
        ToggleFullscreenTransformer.client.m_91268_().f_85355_ = false;
        fullscreen = state;
        ToggleFullscreenTransformer.client.f_91066_.m_231829_().m_231514_((Object)state);
        ToggleFullscreenTransformer.updateVideoMode();
    }

    public static void sendGraphicCard() {
        String disCard = GL11.glGetString((int)7937);
        LOGGER.info("sendGraphicCard: " + disCard);
        MessageRequest mrq = new MessageRequest();
        mrq.send(m_sendgraphicCardCommand, new Object[]{disCard});
    }

    static {
        disable = true;
        ignoreKeyBinding = new KeyMapping("key.fullscreenwindowed.unused", 0, "key.categories.misc");
    }
}

