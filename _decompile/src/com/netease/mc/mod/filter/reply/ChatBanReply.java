/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.message.reply.Reply
 */
package com.netease.mc.mod.filter.reply;

import com.netease.mc.mod.filter.ChatBanHelper;
import com.netease.mc.mod.network.message.reply.Reply;

public class ChatBanReply
extends Reply {
    public static final int SMID = 4613;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void handler(boolean ban, long banChatExpiredAt, long delta) {
        ChatBanHelper.update(ban, banChatExpiredAt, delta);
        Object object = ChatBanHelper.chatBanlock;
        synchronized (object) {
            try {
                ChatBanHelper.chatBanlock.notify();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

