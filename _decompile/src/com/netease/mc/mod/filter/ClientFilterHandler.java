/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonSyntaxException
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen
 *  net.minecraft.client.gui.screens.inventory.AnvilScreen
 *  net.minecraft.client.gui.screens.inventory.BookEditScreen
 *  net.minecraft.client.gui.screens.inventory.BookViewScreen
 *  net.minecraft.client.gui.screens.inventory.BookViewScreen$BookAccess
 *  net.minecraft.client.gui.screens.inventory.BookViewScreen$WrittenBookAccess
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Component$Serializer
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.level.block.entity.SignBlockEntity
 *  net.minecraft.world.level.block.entity.SignText
 *  net.minecraftforge.client.event.ClientChatReceivedEvent
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.client.event.ScreenEvent$Init$Post
 *  net.minecraftforge.client.event.ScreenEvent$Init$Pre
 *  net.minecraftforge.client.event.ScreenEvent$KeyPressed
 *  net.minecraftforge.client.event.ScreenEvent$MouseButtonPressed
 *  net.minecraftforge.event.level.LevelEvent$Save
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.filter;

import com.google.gson.JsonSyntaxException;
import com.netease.mc.mod.filter.FilterHelper;
import com.netease.mc.mod.filter.FilterTextField;
import com.netease.mc.mod.filter.FilterWhiteListHelper;
import com.netease.mc.mod.filter.Helper;
import com.netease.mc.mod.filter.ItemBanHelper;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientFilterHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Queue<Long> chatTimes = new LinkedList<Long>();
    private final int MAX_CHATS = 200;

    private void FilterEditSignScreen(AbstractSignEditScreen screen) {
        SignText tile;
        SignBlockEntity sign = screen.f_244140_;
        SignText[] texts = new SignText[]{sign.m_277142_(), sign.m_277159_()};
        SignText signText = tile = screen.f_276451_ ? sign.m_277142_() : sign.m_277159_();
        if (null != tile) {
            int size = tile.f_276632_.length;
            if (ItemBanHelper.InItemBan("minecraft:sign")) {
                Helper.printAtChannelChat(ItemBanHelper.getBanMessage("minecraft:sign"));
                MutableComponent line = Component.m_237113_((String)"");
                for (int i = 0; i < size; ++i) {
                    tile.f_276632_[i] = line;
                    screen.f_244359_[i] = "";
                }
                return;
            }
            for (int i = 0; i < size; ++i) {
                Component input = tile.f_276632_[i];
                Component line = FilterHelper.filterChatComponentText(input, true, 1);
                String aferFilterMsg = line.getString();
                if (!aferFilterMsg.isEmpty()) {
                    LogManager.getLogger().info("[sign]:" + aferFilterMsg);
                }
                screen.f_244359_[i] = aferFilterMsg;
                tile.f_276632_[i] = line;
            }
        }
    }

    @SubscribeEvent
    public void onEditEscape(ScreenEvent.KeyPressed event) {
        if (event.getScreen() instanceof AbstractSignEditScreen && event.getKeyCode() == 256) {
            this.FilterEditSignScreen((AbstractSignEditScreen)event.getScreen());
        }
    }

    @SubscribeEvent
    public void InitButtonGUI(ScreenEvent.Init.Pre event) {
        BookViewScreen book;
        Screen gui = event.getScreen();
        if (gui instanceof BookEditScreen) {
            try {
                if (ItemBanHelper.InItemBan("minecraft:writable_book")) {
                    Minecraft.m_91087_().m_91152_((Screen)null);
                    Helper.printAtChannelChat(ItemBanHelper.getBanMessage("minecraft:writable_book"));
                    return;
                }
                book = (BookEditScreen)gui;
                ListTag bookPages = new ListTag();
                book.f_98070_.stream().map(StringTag::m_129297_).forEach(arg_0 -> bookPages.add(arg_0));
                if (!book.f_98070_.isEmpty()) {
                    for (int pageIdx = 0; pageIdx < bookPages.size(); ++pageIdx) {
                        String s = bookPages.m_128778_(pageIdx);
                        s = FilterWhiteListHelper.filter(s, false, 0, false);
                        StringTag tag = StringTag.m_129297_((String)s);
                        bookPages.set(pageIdx, (Tag)tag);
                        book.f_98070_.set(pageIdx, s);
                    }
                    book.f_98065_.m_41700_("pages", (Tag)bookPages);
                }
                String title = FilterWhiteListHelper.filter(book.f_98071_.trim(), false, 0, false);
                book.f_98065_.m_41700_("title", (Tag)StringTag.m_129297_((String)title));
            }
            catch (Exception e) {
                this.CatchException(e);
            }
        }
        if (gui instanceof BookViewScreen) {
            book = (BookViewScreen)gui;
            try {
                BookViewScreen.BookAccess field_214168_c = book.f_98253_;
                if (field_214168_c instanceof BookViewScreen.WrittenBookAccess) {
                    BookViewScreen.WrittenBookAccess info = (BookViewScreen.WrittenBookAccess)field_214168_c;
                    List pages = info.f_98320_;
                    ArrayList<String> filters = new ArrayList<String>(pages);
                    for (int i = 0; i < pages.size(); ++i) {
                        String s = (String)pages.get(i);
                        try {
                            MutableComponent t = Component.Serializer.m_130701_((String)s);
                            t = FilterHelper.filterChatComponentText((Component)t, false, 0);
                            filters.set(i, Component.Serializer.m_130703_((Component)t));
                            continue;
                        }
                        catch (JsonSyntaxException e) {
                            s = FilterWhiteListHelper.filter(s, false, 0, false);
                            filters.set(i, s);
                        }
                    }
                    info.f_98320_ = filters;
                }
            }
            catch (Exception e) {
                this.CatchException(e);
            }
        }
    }

    @SubscribeEvent
    public void onMouseClickDone(ScreenEvent.MouseButtonPressed event) {
        if (event.getScreen() instanceof AbstractSignEditScreen) {
            this.FilterEditSignScreen((AbstractSignEditScreen)event.getScreen());
        }
    }

    @SubscribeEvent
    public void onInitGui(ScreenEvent.Init.Post event) {
        Screen gui = event.getScreen();
        if (gui instanceof AnvilScreen) {
            AnvilScreen anvil = (AnvilScreen)gui;
            if (null != anvil.f_97871_) {
                anvil.f_96540_.remove(anvil.f_97871_);
                anvil.f_97871_ = new FilterTextField(anvil.f_97871_);
                anvil.f_97871_.m_94190_(false);
                anvil.f_97871_.m_94182_(false);
                anvil.f_97871_.m_94151_(arg_0 -> ((AnvilScreen)anvil).m_97898_(arg_0));
                anvil.f_96540_.add(anvil.f_97871_);
                anvil.m_7522_((GuiEventListener)anvil.f_97871_);
                anvil.f_97871_.m_93692_(true);
                anvil.m_264313_((GuiEventListener)anvil.f_97871_);
            }
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onChatMessageReceiveEvent(ClientChatReceivedEvent event) {
        String str = event.getMessage().getString();
        if (FilterWhiteListHelper.InWhite(str, false)) {
            return;
        }
        long now = System.currentTimeMillis();
        if (this.chatTimes.size() < 200) {
            this.chatTimes.add(now);
        } else {
            if (this.chatTimes.peek() + 5000L > now) {
                event.setMessage((Component)Component.m_237113_((String)""));
                return;
            }
            this.chatTimes.poll();
            this.chatTimes.add(now);
        }
        Helper.debugLog("onChatMessageReceiveEvent: " + event.getMessage().getString());
        event.setMessage(FilterHelper.filterChatComponentText(event.getMessage(), false, 0));
    }

    private void CatchException(Exception e) {
        StringWriter result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        LogManager.getLogger().info(((Object)result).toString());
    }

    @SubscribeEvent
    public void onWordSave(LevelEvent.Save event) {
        FilterWhiteListHelper.save();
    }

    @SubscribeEvent
    public void onLoggedOutEvent(ClientPlayerNetworkEvent.LoggingOut event) {
        FilterWhiteListHelper.NotifyToLauncher();
        FilterWhiteListHelper.save();
    }
}

