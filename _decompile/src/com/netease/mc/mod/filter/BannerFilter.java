/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntityType
 */
package com.netease.mc.mod.filter;

import java.util.ArrayList;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BannerFilter {
    static String[][][] BannerPatterns = new String[][][]{{{"minecraft:red_banner"}, {"mr_0", "sc_15", "ts_14", "bs_14", "bo_14"}}, {{"minecraft:red_banner"}, {"cs_0", "ms_0", "mr_0", "sc_15", "mc_15"}}, {{"minecraft:white_banner"}, {"mc_14"}}, {{"minecraft:blue_banner"}, {"hhb_14", "tl_0", "rs_14"}}, {{"minecraft:blue_banner"}, {"tr_0", "bo_11", "hhb_14", "ls_14"}}, {{"minecraft:blue_banner"}, {"flo_0", "mc_11", "mc_0"}}};
    static BannerPatternTreeNode root;

    public static void init() {
        root = new BannerPatternTreeNode("");
        for (String[][] pattern : BannerPatterns) {
            String name = pattern[0][0];
            BannerPatternTreeNode node = root.getOrCreateChild(pattern[0][0]);
            Arrays.sort(pattern[1]);
            for (String p : pattern[1]) {
                node = node.getOrCreateChild(p);
            }
        }
    }

    private static boolean match(BannerPatternTreeNode root, String[] patterns) {
        BannerPatternTreeNode node = root;
        for (String pattern : patterns) {
            if ((node = node.findChild(pattern)) != null) continue;
            return false;
        }
        return node.children.size() == 0;
    }

    public static void checkBanner(ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }
        CompoundTag compoundtag = BlockItem.m_186336_((ItemStack)itemStack);
        if (compoundtag != null && compoundtag.m_128425_("Patterns", 9)) {
            ListTag listtag = compoundtag.m_128437_("Patterns", 10);
            if (listtag == null) {
                return;
            }
            String name = itemStack.m_41720_().m_5524_();
            BannerPatternTreeNode node = root.findChild(name);
            if (node == null) {
                return;
            }
            ArrayList<String> patternArr = new ArrayList<String>();
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag tag = listtag.m_128728_(i);
                String pattern = String.join((CharSequence)"_", tag.m_128461_("Pattern"), Integer.toString(tag.m_128451_("Color")));
                patternArr.add(pattern);
            }
            Object[] patterns = patternArr.toArray(new String[patternArr.size()]);
            Arrays.sort(patterns);
            if (BannerFilter.match(node, (String[])patterns)) {
                compoundtag.m_128365_("Patterns", (Tag)new ListTag());
                BlockItem.m_186338_((ItemStack)itemStack, (BlockEntityType)BlockEntityType.f_58935_, (CompoundTag)compoundtag);
            }
        }
    }

    static class BannerPatternTreeNode {
        public String value;
        public ArrayList<BannerPatternTreeNode> children = new ArrayList();

        public BannerPatternTreeNode(String _value) {
            this.value = _value;
        }

        @Nullable
        public BannerPatternTreeNode findChild(String _value) {
            for (BannerPatternTreeNode child : this.children) {
                if (!child.value.equals(_value)) continue;
                return child;
            }
            return null;
        }

        public BannerPatternTreeNode getOrCreateChild(String _value) {
            for (BannerPatternTreeNode child : this.children) {
                if (!child.value.equals(_value)) continue;
                return child;
            }
            BannerPatternTreeNode node = new BannerPatternTreeNode(_value);
            this.children.add(node);
            return node;
        }
    }
}

