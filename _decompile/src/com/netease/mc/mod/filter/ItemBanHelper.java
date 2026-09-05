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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItemBanHelper {
    static final Map<String, ItemBanInfo> ItemBanMap = new ConcurrentHashMap<String, ItemBanInfo>();
    public static Object itemBanlock = new Object();

    public static boolean InItemBan(String itemName) {
        ItemBanInfo info = ItemBanMap.getOrDefault(itemName, null);
        if (info == null) {
            info = new ItemBanInfo(itemName);
            ItemBanMap.put(itemName, info);
        }
        return info.InItemBan();
    }

    public static void update(String itemName, boolean ban, long expireAt, String reason, long delta) {
        ItemBanInfo info = ItemBanMap.getOrDefault(itemName, null);
        if (info == null) {
            info = new ItemBanInfo(itemName);
            ItemBanMap.put(itemName, info);
        }
        info.update(itemName, ban, expireAt, reason, delta);
    }

    public static String getBanMessage(String itemName) {
        ItemBanInfo info = ItemBanMap.getOrDefault(itemName, null);
        if (info == null) {
            return "";
        }
        return info.getBanMessage();
    }

    public static class ItemBanInfo {
        public String itemName;
        public String banReason = "";
        private boolean itemBan = true;
        private volatile long banItemExpireAt = 0L;
        private volatile long timeDelta = 0L;
        private final int TIMEOUT = 10000;
        protected final Logger logger = LogManager.getLogger();

        public ItemBanInfo(String name) {
            this.itemName = name;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public boolean InItemBan() {
            if (!this.itemBan) {
                return false;
            }
            long now = System.currentTimeMillis() / 1000L;
            if (this.banItemExpireAt > this.timeDelta + now) {
                return true;
            }
            MessageRequest mrq = new MessageRequest();
            mrq.send(4614, new Object[]{this.itemName});
            Object object = itemBanlock;
            synchronized (object) {
                try {
                    itemBanlock.wait(10000L);
                    if (this.banItemExpireAt == 0L) {
                        return false;
                    }
                    return this.itemBan;
                }
                catch (Throwable e) {
                    FilterWrapper.CatchException(e);
                    return false;
                }
            }
        }

        public void update(String name, boolean ban, long expireAt, String reason, long delta) {
            this.itemName = name;
            this.itemBan = ban;
            this.banItemExpireAt = expireAt;
            this.timeDelta = delta;
            this.banReason = reason;
        }

        public String getBanMessage() {
            Date date = new Date(this.banItemExpireAt * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy\u5e74MM\u6708dd\u65e5HH\u65f6mm\u5206");
            return String.format("\u56e0%s\u60a8\u7684\u8d26\u53f7\u88ab\u7981\u6b62\u6b64\u529f\u80fd\u81f3%s, \u5982\u6709\u7591\u95ee\uff0c\u8bf7\u8054\u7cfb\u5b98\u65b9\u5ba2\u670d\u4e86\u89e3\u8be6\u60c5", this.banReason, sdf.format(date));
        }
    }
}

