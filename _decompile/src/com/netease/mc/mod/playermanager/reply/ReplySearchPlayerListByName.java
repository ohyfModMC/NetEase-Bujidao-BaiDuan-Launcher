/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.netease.mc.mod.network.message.reply.Reply
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  net.minecraft.client.Minecraft
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.playermanager.reply;

import com.google.gson.Gson;
import com.netease.mc.mod.network.message.reply.Reply;
import com.netease.mc.mod.network.message.request.MessageRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReplySearchPlayerListByName
extends Reply {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int SMID = 1041;
    private static final int SearchPlayersNameCmd = 1040;
    private static Gson gson = new Gson();

    public void handler(String searchName) {
        HashMap<String, String> matchedPlayersMap = new HashMap<String, String>();
        Minecraft mc = Minecraft.m_91087_();
        if (null == mc.f_91074_) {
            return;
        }
        Collection playerList = mc.f_91074_.f_108617_.m_105142_();
        for (Object player : playerList) {
            String string = player.m_105312_().getId().toString();
            String name = player.m_105312_().getName();
            if (!name.contains(searchName)) continue;
            matchedPlayersMap.put(string, name);
        }
        Object matchedPlayersStr = "";
        for (Map.Entry entry : matchedPlayersMap.entrySet()) {
            matchedPlayersStr = (String)matchedPlayersStr + (String)entry.getKey() + ":" + (String)entry.getValue() + ", ";
        }
        LOGGER.info("SearchPlayerListByName: " + searchName);
        LOGGER.info("matched players are: " + (String)matchedPlayersStr);
        String json = gson.toJson(matchedPlayersMap);
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.send(1040, new Object[]{json});
    }
}

