/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  net.minecraft.nbt.ByteArrayTag
 *  net.minecraft.nbt.ByteTag
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.DoubleTag
 *  net.minecraft.nbt.EndTag
 *  net.minecraft.nbt.FloatTag
 *  net.minecraft.nbt.IntArrayTag
 *  net.minecraft.nbt.IntTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.LongArrayTag
 *  net.minecraft.nbt.LongTag
 *  net.minecraft.nbt.ShortTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.nbt.TagVisitor
 */
package com.netease.mc.mod.filter;

import com.google.common.collect.Lists;
import com.netease.mc.mod.filter.FilterWhiteListHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagVisitor;

public class NbtTagVisitor {
    public FilterVisitor createFilterVisitor() {
        return new FilterVisitor();
    }

    public class FilterVisitor
    implements TagVisitor {
        public void visit(Tag p_178188_) {
            p_178188_.m_142327_((TagVisitor)this);
        }

        public void m_142614_(StringTag p_178186_) {
            String oldString = p_178186_.m_7916_();
            p_178186_.f_129290_ = FilterWhiteListHelper.filter(oldString, false, 0);
        }

        public void m_141946_(ByteTag p_178164_) {
        }

        public void m_142183_(ShortTag p_178184_) {
        }

        public void m_142045_(IntTag p_178176_) {
        }

        public void m_142046_(LongTag p_178182_) {
        }

        public void m_142181_(FloatTag p_178172_) {
        }

        public void m_142121_(DoubleTag p_178168_) {
        }

        public void m_142154_(ByteArrayTag p_178162_) {
        }

        public void m_142251_(IntArrayTag p_178174_) {
        }

        public void m_142309_(LongArrayTag p_178180_) {
        }

        public void m_142447_(ListTag p_178178_) {
            for (Tag tag : p_178178_) {
                new FilterVisitor().visit(tag);
            }
        }

        public void m_142303_(CompoundTag p_178166_) {
            ArrayList list = Lists.newArrayList((Iterable)p_178166_.m_128431_());
            Collections.sort(list);
            for (String s : list) {
                new FilterVisitor().visit(Objects.requireNonNull(p_178166_.m_128423_(s)));
            }
        }

        public void m_142384_(EndTag p_178220_) {
        }
    }
}

