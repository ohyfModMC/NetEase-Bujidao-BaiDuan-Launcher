/*
 * Decompiled with CFR 0.152.
 */
package com.netease.mc.mod.departmod.coremod;

import java.text.DecimalFormat;

public class IStatFormaterTransformer {
    private static final DecimalFormat decimalFormat = new DecimalFormat("########0.00");

    public static String timeFormater(int number) {
        double d0 = (double)number / 20.0;
        double d1 = d0 / 60.0;
        double d2 = d1 / 60.0;
        double d3 = d2 / 24.0;
        double d4 = d3 / 365.0;
        if (d4 > 0.5) {
            return decimalFormat.format(d4) + "\u5e74";
        }
        if (d3 > 0.5) {
            return decimalFormat.format(d3) + "\u5929";
        }
        if (d2 > 0.5) {
            return decimalFormat.format(d2) + "\u5c0f\u65f6";
        }
        return d1 > 0.5 ? decimalFormat.format(d1) + "\u5206" : d0 + "\u79d2";
    }

    public static String distanceFormater(int number) {
        double d0 = (double)number / 100.0;
        double d1 = d0 / 1000.0;
        if (d1 > 0.5) {
            return decimalFormat.format(d1) + "\u5343\u7c73";
        }
        return d0 > 0.5 ? decimalFormat.format(d0) + "\u7c73" : number + "\u5398\u7c73";
    }
}

