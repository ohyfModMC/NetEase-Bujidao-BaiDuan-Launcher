/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.common.GameState
 *  com.netease.mc.mod.network.message.reply.Reply
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  net.minecraft.client.Minecraft
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.friendplay.message.reply;

import com.netease.mc.mod.network.common.GameState;
import com.netease.mc.mod.network.message.reply.Reply;
import com.netease.mc.mod.network.message.request.MessageRequest;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReplyOpScreenShot
extends Reply {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int SMID = 2305;

    public void handler(String path) {
        byte result = 0;
        try {
            LOGGER.info(String.format("ReplyOpScreenShot path: ", path));
            Minecraft mc = Minecraft.m_91087_();
            BufferedImage screenshot = new Robot().createScreenCapture(new Rectangle(mc.m_91268_().m_85447_(), mc.m_91268_().m_85448_(), mc.m_91268_().m_85441_(), mc.m_91268_().m_85442_()));
            File file = new File(path);
            ImageIO.write((RenderedImage)screenshot, "png", file);
            MessageRequest mrq = new MessageRequest();
            mrq.send(2305, new Object[]{result, GameState.gameid, file.getAbsolutePath()});
        }
        catch (Exception e) {
            result = 1;
            LOGGER.error("ReplyOpScreenShot", (Throwable)e);
            MessageRequest mrq = new MessageRequest();
            mrq.send(2305, new Object[]{result, GameState.gameid, ""});
        }
    }
}

