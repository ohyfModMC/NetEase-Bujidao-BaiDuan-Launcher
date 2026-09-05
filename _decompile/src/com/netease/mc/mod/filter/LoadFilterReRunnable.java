/*
 * Decompiled with CFR 0.152.
 */
package com.netease.mc.mod.filter;

import com.netease.mc.mod.filter.FilterHelper;

public class LoadFilterReRunnable
implements Runnable {
    @Override
    public void run() {
        FilterHelper.loadFilterRe();
    }
}

