/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.ChatOptionsScreen
 *  net.minecraft.client.gui.screens.OptionsScreen
 *  net.minecraft.client.gui.screens.PauseScreen
 *  net.minecraft.client.gui.screens.TitleScreen
 *  net.minecraft.client.gui.screens.social.SocialInteractionsScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraftforge.client.event.ScreenEvent$Init$Post
 *  net.minecraftforge.client.gui.widget.ForgeSlider
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.departmod.guibuttondisplay;

import com.netease.mc.mod.departmod.guibuttondisplay.IGuiInitTransfer;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatOptionsScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiButtonTransfer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ConcurrentHashMap<Class<?>, IGuiInitTransfer> initTransfer = new ConcurrentHashMap();

    @SubscribeEvent
    public void onInitGui(ScreenEvent.Init.Post event) {
        try {
            Class<?> gui = event.getScreen().getClass();
            if (initTransfer.containsKey(gui)) {
                initTransfer.get(gui).transfer(event);
            }
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private static void InitScreenChatOptions(ScreenEvent.Init.Post event) {
        List widgets = event.getListenersList();
        for (GuiEventListener widget : widgets) {
            if (!(widget instanceof ForgeSlider)) continue;
            ForgeSlider slider = (ForgeSlider)widget;
            slider.m_93666_((Component)Component.m_237113_((String)slider.m_6035_().getString().replace("px", "\u50cf\u7d20")));
        }
    }

    private static void moveTo(GuiEventListener from, GuiEventListener to) {
        if (from instanceof AbstractWidget && to instanceof AbstractWidget) {
            ((AbstractWidget)from).m_252865_(((AbstractWidget)to).m_252754_());
            ((AbstractWidget)from).m_253211_(((AbstractWidget)to).m_252907_());
        }
    }

    private static void InitOptionsScreen(ScreenEvent.Init.Post event) {
        List widgets = event.getListenersList();
        int startIndex = -1;
        if (widgets.size() == 11) {
            event.removeListener((GuiEventListener)widgets.get(1));
            startIndex = 1;
        } else if (widgets.size() == 12) {
            startIndex = 3;
        }
        if (startIndex < 0) {
            return;
        }
        GuiEventListener skin = (GuiEventListener)widgets.get(startIndex);
        GuiEventListener language = (GuiEventListener)widgets.get(startIndex + 4);
        GuiEventListener chat = (GuiEventListener)widgets.get(startIndex + 5);
        GuiEventListener resource = (GuiEventListener)widgets.get(startIndex + 6);
        GuiEventListener access = (GuiEventListener)widgets.get(startIndex + 7);
        event.removeListener(skin);
        event.removeListener(language);
        GuiButtonTransfer.moveTo(access, chat);
        GuiButtonTransfer.moveTo(resource, language);
        GuiButtonTransfer.moveTo(chat, skin);
    }

    private static void InitGuiMainMenu(ScreenEvent.Init.Post event) {
        List widgets = event.getListenersList();
        GuiEventListener realms = (GuiEventListener)widgets.get(2);
        GuiEventListener mods = (GuiEventListener)widgets.get(3);
        GuiEventListener language = (GuiEventListener)widgets.get(4);
        event.removeListener(realms);
        event.removeListener(mods);
        event.removeListener(language);
    }

    private static void InitGuiIngameMenu(ScreenEvent.Init.Post event) {
        List widgets = event.getListenersList();
        GuiEventListener feedback = (GuiEventListener)widgets.get(3);
        GuiEventListener bug = (GuiEventListener)widgets.get(4);
        GuiEventListener openToLan = (GuiEventListener)widgets.get(6);
        GuiEventListener mod = (GuiEventListener)widgets.get(7);
        event.removeListener(feedback);
        event.removeListener(bug);
        event.removeListener(openToLan);
        event.removeListener(mod);
    }

    private static void InitSocialInteractions(ScreenEvent.Init.Post event) {
        List widgets = event.getListenersList();
        GuiEventListener feedback = (GuiEventListener)widgets.get(3);
        event.removeListener(feedback);
    }

    public static void Init() {
        MinecraftForge.EVENT_BUS.register((Object)new GuiButtonTransfer());
        IGuiInitTransfer screenChat = new IGuiInitTransfer(){

            @Override
            public void transfer(ScreenEvent.Init.Post event) {
                GuiButtonTransfer.InitScreenChatOptions(event);
            }
        };
        initTransfer.put(ChatOptionsScreen.class, screenChat);
        IGuiInitTransfer guiOptionsScreen = new IGuiInitTransfer(){

            @Override
            public void transfer(ScreenEvent.Init.Post event) {
                GuiButtonTransfer.InitOptionsScreen(event);
            }
        };
        initTransfer.put(OptionsScreen.class, guiOptionsScreen);
        IGuiInitTransfer guiMainMenu = new IGuiInitTransfer(){

            @Override
            public void transfer(ScreenEvent.Init.Post event) {
                GuiButtonTransfer.InitGuiMainMenu(event);
            }
        };
        initTransfer.put(TitleScreen.class, guiMainMenu);
        IGuiInitTransfer guiIngameMenu = new IGuiInitTransfer(){

            @Override
            public void transfer(ScreenEvent.Init.Post event) {
                GuiButtonTransfer.InitGuiIngameMenu(event);
            }
        };
        initTransfer.put(PauseScreen.class, guiIngameMenu);
        IGuiInitTransfer guiSocialInteractions = new IGuiInitTransfer(){

            @Override
            public void transfer(ScreenEvent.Init.Post event) {
                GuiButtonTransfer.InitSocialInteractions(event);
            }
        };
        initTransfer.put(SocialInteractionsScreen.class, guiSocialInteractions);
    }
}

