/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.netease.mc.mod.network.message.reply.Reply
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.opencommand.reply;

import com.google.gson.Gson;
import com.netease.mc.mod.network.message.reply.Reply;
import com.netease.mc.mod.network.message.request.MessageRequest;
import java.util.Collection;
import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReplyAllPlayersList
extends Reply {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int SMID = 4097;
    private static final int GetPlayersListCmd = 4098;
    private static Gson gson = new Gson();

    public void handler() {
        Minecraft mc = Minecraft.m_91087_();
        Collection playerList = mc.f_91074_.f_108617_.m_105142_();
        HashMap<String, String> matchedPlayersMap = new HashMap<String, String>();
        for (PlayerInfo player : playerList) {
            String uuid = player.m_105312_().getId().toString();
            String name = player.m_105312_().getName().toString();
            LOGGER.info("ReplyAllPlayersList: " + uuid + " " + name);
            matchedPlayersMap.put(uuid, name);
        }
        String selfName = mc.f_91074_.m_7755_().getString();
        LOGGER.info("ReplyAllPlayersList: self " + selfName);
        matchedPlayersMap.put("self", selfName);
        String json = gson.toJson(matchedPlayersMap);
        MessageRequest mrq = new MessageRequest();
        mrq.send(4098, new Object[]{json});
    }
}

