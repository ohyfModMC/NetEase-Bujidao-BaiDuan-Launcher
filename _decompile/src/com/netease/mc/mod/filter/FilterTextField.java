/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.network.chat.Component
 *  org.apache.logging.log4j.LogManager
 */
package com.netease.mc.mod.filter;

import com.netease.mc.mod.filter.FilterWhiteListHelper;
import com.netease.mc.mod.filter.Helper;
import com.netease.mc.mod.filter.ItemBanHelper;
import java.io.PrintWriter;
import java.io.StringWriter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;

public class FilterTextField
extends EditBox {
    public FilterTextField(EditBox field) {
        super(Minecraft.m_91087_().f_91062_, field.f_93620_, field.f_93621_, field.m_5711_(), field.m_93694_(), (Component)Component.m_237113_((String)field.m_94155_()));
        this.m_94190_(false);
        this.m_93692_(true);
        this.m_94202_(-1);
        this.m_94205_(-1);
        this.m_94182_(false);
        this.m_94199_(35);
    }

    public void m_94174_(String text) {
        try {
            if (ItemBanHelper.InItemBan("minecraft:anvil")) {
                Helper.printAtChannelChat(ItemBanHelper.getBanMessage("minecraft:anvil"));
                return;
            }
        }
        catch (Exception e) {
            StringWriter result = new StringWriter();
            PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
        }
        String originText = this.m_94155_();
        String filterText = FilterWhiteListHelper.filter(originText, true, 4, true);
        if (!filterText.equals(originText)) {
            LogManager.getLogger().info("Found filter message:" + filterText);
            this.m_94144_(filterText);
            super.m_94174_(filterText);
            return;
        }
        super.m_94174_(text);
    }
}

