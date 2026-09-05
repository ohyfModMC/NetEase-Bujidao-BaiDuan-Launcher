/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.message.reply.Reply
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.skin.message.reply;

import com.netease.mc.mod.network.message.reply.Reply;
import com.netease.mc.mod.skin.SkinHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoadSkinReplyV2
extends Reply {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int SMID = 2050;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void handler(String name, String skinPath, String capePath, int isSlim) {
        if (!SkinHandler.lockObjectMap.containsKey(name)) {
            LOGGER.error("[Skin]: name:" + name + " do not exist!");
            return;
        }
        Object object = SkinHandler.lockObjectMap.get(name);
        synchronized (object) {
            try {
                SkinHandler.nameSkinMap.put(name, skinPath);
                SkinHandler.nameCapeMap.put(name, capePath);
                SkinHandler.nameSkinMode.put(name, isSlim == 1);
                SkinHandler.lockObjectMap.get(name).notify();
            }
            catch (Exception e) {
                LOGGER.error("LoadSkinReply2", (Throwable)e);
            }
        }
    }
}

