/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.common.GameState
 *  com.netease.mc.mod.network.message.reply.MessageReply
 *  com.netease.mc.mod.network.socket.NetworkHandler
 *  net.minecraft.client.Minecraft
 *  net.minecraftforge.common.MinecraftForge
 */
package com.netease.mc.mod.filter;

import com.netease.mc.mod.filter.BannerFilter;
import com.netease.mc.mod.filter.ClientFilterHandler;
import com.netease.mc.mod.filter.FilterHelper;
import com.netease.mc.mod.filter.FilterWhiteListHelper;
import com.netease.mc.mod.filter.FilterWrapper;
import com.netease.mc.mod.filter.reply.ChatBanReply;
import com.netease.mc.mod.filter.reply.FilterSALogReply;
import com.netease.mc.mod.filter.reply.FilterWordReply;
import com.netease.mc.mod.filter.reply.ItemBanReply;
import com.netease.mc.mod.network.common.GameState;
import com.netease.mc.mod.network.message.reply.MessageReply;
import com.netease.mc.mod.network.socket.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

public class FilterMod {
    public static void init() {
        FilterHelper.loadFilterReInAnotherThread();
        FilterWhiteListHelper.init();
        BannerFilter.init();
        MinecraftForge.EVENT_BUS.register((Object)new ClientFilterHandler());
        NetworkHandler.networkHandler.registerAsync(4608, (MessageReply)new FilterWordReply());
        NetworkHandler.networkHandler.registerAsync(4610, (MessageReply)new FilterSALogReply());
        NetworkHandler.networkHandler.registerAsync(4613, (MessageReply)new ChatBanReply());
        NetworkHandler.networkHandler.registerAsync(4614, (MessageReply)new ItemBanReply());
        if (GameState.userPropertiesEx.GameType == 2) {
            FilterWrapper.cacheNames.put(Minecraft.m_91087_().m_91094_().m_92546_(), Minecraft.m_91087_().m_91094_().m_92546_());
        }
    }
}

