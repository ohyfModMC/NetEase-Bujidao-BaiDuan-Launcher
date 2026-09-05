/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.syncher.EntityDataAccessor
 */
package com.netease.mc.mod.oldInterface;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;

public interface IEntityOld {
    public void loadOld(CompoundTag var1);

    public void onSyncedDataUpdatedOld(EntityDataAccessor var1);
}

