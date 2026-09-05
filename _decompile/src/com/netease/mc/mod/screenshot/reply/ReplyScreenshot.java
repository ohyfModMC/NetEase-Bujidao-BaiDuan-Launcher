/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.message.reply.Reply
 */
package com.netease.mc.mod.screenshot.reply;

import com.netease.mc.mod.network.message.reply.Reply;
import com.netease.mc.mod.screenshot.ScreenShotHandler;

public class ReplyScreenshot
extends Reply {
    public static final int SMID = 4612;

    public void handler() {
        ScreenShotHandler.doScreenShot();
    }
}

