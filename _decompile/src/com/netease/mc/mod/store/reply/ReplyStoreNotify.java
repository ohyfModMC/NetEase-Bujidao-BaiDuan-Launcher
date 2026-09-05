/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.message.reply.Reply
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.store.reply;

import com.netease.mc.mod.network.message.reply.Reply;
import com.netease.mc.mod.store.StoreMod;
import com.netease.mc.mod.store.message.StoreNotifyMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReplyStoreNotify
extends Reply {
    public static final int SMID = 263;
    public static final Logger logger = LogManager.getLogger();

    public void handler() {
        logger.info("reply of store notify");
        StoreMod.INSTANCE.sendToServer((Object)new StoreNotifyMessage());
    }
}

