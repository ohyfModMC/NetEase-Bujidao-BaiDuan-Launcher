/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.Screenshot
 *  net.minecraft.client.gui.screens.PauseScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraftforge.client.event.ScreenEvent$Init
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$RenderTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.screenshot;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.netease.mc.mod.network.message.request.MessageRequest;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScreenShotHandler {
    public static int step = 0;
    public static int tick = 0;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    private static final Logger logger = LogManager.getLogger();

    @SubscribeEvent
    public void onClientTick(TickEvent.RenderTickEvent event) {
        if (step > 0 && event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.m_91087_();
            switch (step) {
                case 1: {
                    mc.m_91152_(null);
                    ++step;
                    break;
                }
                case 2: {
                    ++step;
                    break;
                }
                case 3: {
                    try {
                        File file1 = new File(mc.f_91069_, "screenshots");
                        File file2 = ScreenShotHandler.getTimestampedPNGFileForDirectory(file1);
                        Screenshot.m_92295_((File)mc.f_91069_, (String)file2.getName(), (RenderTarget)mc.m_91385_(), x -> {});
                        mc.m_91152_((Screen)new PauseScreen(true));
                        MessageRequest mrq = new MessageRequest();
                        mrq.send(4612, new Object[]{file2.getCanonicalPath()});
                        logger.info("File name:" + file2.getCanonicalPath());
                    }
                    catch (Throwable e) {
                        e.printStackTrace();
                    }
                    step = 0;
                    break;
                }
                default: {
                    step = 0;
                }
            }
        }
    }

    public static void doScreenShot() {
        step = 1;
    }

    private static File getTimestampedPNGFileForDirectory(File gameDirectory) {
        String s = DATE_FORMAT.format(new Date()).toString();
        int i = 1;
        File file1;
        while ((file1 = new File(gameDirectory, s + (String)(i == 1 ? "" : "_" + i) + ".png")).exists()) {
            ++i;
        }
        return file1;
    }

    @SubscribeEvent
    public void onInguiEvent(ScreenEvent.Init event) {
        if (step == 0) {
            return;
        }
        Minecraft mc = Minecraft.m_91087_();
        mc.m_91152_(null);
    }
}

