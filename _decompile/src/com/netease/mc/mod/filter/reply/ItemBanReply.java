/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.message.reply.Reply
 */
package com.netease.mc.mod.filter.reply;

import com.netease.mc.mod.filter.ItemBanHelper;
import com.netease.mc.mod.network.message.reply.Reply;

public class ItemBanReply
extends Reply {
    public static final int SMID = 4614;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void handler(String itemName, boolean ban, long banChatExpiredAt, String reason, long delta) {
        ItemBanHelper.update(itemName, ban, banChatExpiredAt, reason, delta);
        Object object = ItemBanHelper.itemBanlock;
        synchronized (object) {
            try {
                ItemBanHelper.itemBanlock.notify();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

