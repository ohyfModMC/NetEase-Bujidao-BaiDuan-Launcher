/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.message.reply.Reply
 */
package com.netease.mc.mod.filter.reply;

import com.netease.mc.mod.filter.FilterHelper;
import com.netease.mc.mod.network.message.reply.Reply;

public class FilterWordReply
extends Reply {
    public static final int SMID = 4608;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void handler(int wordId, String word) {
        if (!FilterHelper.lockObjectMap.containsKey(wordId)) {
            return;
        }
        Object object = FilterHelper.lockObjectMap.get(wordId);
        synchronized (object) {
            try {
                FilterHelper.filterWordMap.put(wordId, word);
                FilterHelper.lockObjectMap.get(wordId).notify();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

