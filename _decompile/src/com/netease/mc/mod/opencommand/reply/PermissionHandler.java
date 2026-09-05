/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.message.reply.Reply
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.server.IntegratedServer
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.opencommand.reply;

import com.netease.mc.mod.network.message.reply.Reply;
import com.netease.mc.mod.network.message.request.MessageRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PermissionHandler
extends Reply {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int SMID = 4102;
    public static final int PermissionCMD = 4103;

    public void handler() {
        LOGGER.info("PermissionHandler reply");
        Minecraft mc = Minecraft.m_91087_();
        if (mc.f_91074_ == null) {
            LOGGER.error("mc.player==null");
            return;
        }
        IntegratedServer server = mc.m_91092_();
        if (server != null) {
            boolean permission = server.m_6846_().m_11303_(mc.f_91074_.m_36316_());
            if (permission) {
                PermissionHandler.SendPermission();
            } else {
                String serverOwener = server.m_236731_().getName();
                if (serverOwener.equals(mc.f_91074_.m_7755_().getString())) {
                    LOGGER.info("set Op permission for owner: " + serverOwener);
                    server.m_6846_().m_5749_(mc.f_91074_.m_36316_());
                    PermissionHandler.SendPermission();
                } else {
                    LOGGER.error("you are not the server owner, permission denied");
                }
            }
        } else {
            LOGGER.error("getSingleplayerServer null error");
        }
    }

    public static void SendPermission() {
        MessageRequest mrq = new MessageRequest();
        mrq.send(4103, new Object[]{"true"});
    }
}

