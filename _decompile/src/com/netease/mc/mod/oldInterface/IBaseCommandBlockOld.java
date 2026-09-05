/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 */
package com.netease.mc.mod.oldInterface;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public interface IBaseCommandBlockOld {
    public void loadOld(CompoundTag var1);

    public Component getLastOutputOld();
}

