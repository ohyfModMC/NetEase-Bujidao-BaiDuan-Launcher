/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.netease.mc.mod.network.message.reply.Reply
 */
package com.netease.mc.mod.filter.reply;

import com.google.gson.Gson;
import com.netease.mc.mod.filter.FilterWhiteListHelper;
import com.netease.mc.mod.filter.model.FilterSaLog;
import com.netease.mc.mod.network.message.reply.Reply;

public class FilterSALogReply
extends Reply {
    public static final int SMID = 4610;

    public void handler(String json) {
        Gson gson = new Gson();
        FilterSaLog[] saLogList = (FilterSaLog[])gson.fromJson(json, FilterSaLog[].class);
        if (saLogList.length == 0) {
            FilterWhiteListHelper.clearCache();
            return;
        }
        for (FilterSaLog saLog : saLogList) {
            FilterWhiteListHelper.AddWhiteList(saLog.output, saLog.timeStamp);
        }
    }
}

