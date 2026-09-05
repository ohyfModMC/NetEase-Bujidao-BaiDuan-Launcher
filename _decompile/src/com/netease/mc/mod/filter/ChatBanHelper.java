/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.filter;

import com.netease.mc.mod.filter.FilterWrapper;
import com.netease.mc.mod.network.message.request.MessageRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatBanHelper {
    private static boolean chatBan = true;
    private static long banChatExpireAt = 0L;
    private static long timeDelta = 0L;
    public static Object chatBanlock = new Object();
    private static final int TIMEOUT = 10000;
    protected static final Logger logger = LogManager.getLogger();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean InChatBan() {
        if (!chatBan) {
            return false;
        }
        long now = System.currentTimeMillis() / 1000L;
        if (banChatExpireAt > timeDelta + now) {
            return true;
        }
        MessageRequest mrq = new MessageRequest();
        mrq.send(4613, new Object[0]);
        Object object = chatBanlock;
        synchronized (object) {
            try {
                chatBanlock.wait(10000L);
                if (banChatExpireAt == 0L) {
                    return false;
                }
                return chatBan;
            }
            catch (Throwable e) {
                FilterWrapper.CatchException(e);
                return false;
            }
        }
    }

    public static void update(boolean ban, long expireAt, long delta) {
        chatBan = ban;
        banChatExpireAt = expireAt;
        timeDelta = delta;
    }

    public static String getBanMessage() {
        Date date = new Date(banChatExpireAt * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy\u5e74MM\u6708dd\u65e5HH\u65f6mm\u5206");
        return String.format("\u60a8\u88ab\u7cfb\u7edf\u6682\u65f6\u7981\u8a00\uff0c\u65f6\u95f4\u81f3%s", sdf.format(date));
    }
}

