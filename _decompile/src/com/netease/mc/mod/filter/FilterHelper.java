/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Joiner
 *  com.netease.mc.mod.network.common.GameState
 *  com.netease.mc.mod.network.common.Library
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.ComponentContents
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.contents.LiteralContents
 *  net.minecraft.network.chat.contents.TranslatableContents
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.WrittenBookItem
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.filter;

import com.google.common.base.Joiner;
import com.netease.mc.mod.filter.AESHelper;
import com.netease.mc.mod.filter.FilterWhiteListHelper;
import com.netease.mc.mod.filter.FilterWrapper;
import com.netease.mc.mod.filter.Helper;
import com.netease.mc.mod.filter.LoadFilterReRunnable;
import com.netease.mc.mod.network.common.GameState;
import com.netease.mc.mod.network.common.Library;
import com.netease.mc.mod.network.message.request.MessageRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FilterHelper {
    public static final Logger logger = LogManager.getLogger(FilterHelper.class);
    private static List<String> namingFilterRegularExpList = Collections.synchronizedList(new ArrayList());
    private static List<String> chatFilterRegularExpList = Collections.synchronizedList(new ArrayList());
    public static long lastLoadFilterReTime = 0L;
    static volatile boolean ThreadRunning = false;
    public static int globalWordId = 0;
    public static ConcurrentLinkedQueue<String> needLauncherList = new ConcurrentLinkedQueue();
    public static ConcurrentLinkedQueue<String> launcherList = new ConcurrentLinkedQueue();
    public static HashMap<Integer, Object> lockObjectMap = new HashMap();
    public static HashMap<Integer, String> filterWordMap = new HashMap();
    private static final int TIMEOUT = 1000;
    private static String[] whiteCmds = new String[]{"ability ", "clear ", "clone ", "connect ", "deop ", "difficulty ", "effect ", "enchant ", "experience ", "fill ", "gamemode ", "gamerule ", "give ", "help ", "kill ", "list ", "locate ", "mobevent ", "op ", "replaceitem ", "setblock", "setmaxplayers ", "setworldspawn ", "spawnpoint ", "spreadplayers ", "stopsound ", "teleport ", "testforblock ", "testforblocks ", "tickingarea ", "time ", "tp ", "weather ", "xp "};
    private static ArrayList<String> whiteMsgs = new ArrayList<String>(){
        {
            this.add("{\"text\":\"npcPhase\"}");
            this.add("npcPhase");
            this.add("2be4bffc-dbcb-45f8-8abe-7bbc4ca0baff");
            this.add("e62e0bbc-6014-4567-bf3b-440dc8461b10");
            this.add("e5c2e7bf-c365-4e28-bbc0-8b8808a9af22");
            this.add("89be8c5a-f261-45e7-8166-3487bbc6e91c");
            this.add("bbc4773a-36ce-4be7-aff0-91fa99b1bf6f");
            this.add("402bbc85-58c1-4baa-ba93-ba24479d5956");
            this.add("f4a12a2c-126b-40b1-8bbc-9930cec62142");
            this.add("144bd2b2-753e-4399-bbc6-917d0bc7f20f");
            this.add("3a4be989-1af8-4bbc-b2b1-aa9d9f9f1960");
        }
    };

    public static String doSdkFilter(String message, boolean nameLib) {
        Object[] lines = message.split("\n", -1);
        for (int i = 0; i < lines.length; ++i) {
            lines[i] = FilterHelper.doSdkFilterInner((String)lines[i], nameLib);
        }
        String result = Joiner.on((String)"\n").join(lines);
        return result;
    }

    private static String doSdkFilterInner(String message, boolean nameLib) {
        int code = 0;
        try {
            Class<?> lib = Class.forName("com.netease.mc.mod.network.common.Library");
            java.lang.reflect.Method m = lib.getDeclaredMethod(nameLib ? "reviewName" : "reviewWord", String.class);
            Object r = m.invoke(null, message);
            if (r instanceof Integer) code = (Integer) r;
        } catch (Throwable t) {
            code = 0;
        }
        Helper.debugLog("doSdkFilterInner:code:" + code + ":" + message);
        switch (code) {
            case 0: {
                return message;
            }
            case 1: {
                String replaceStr = new String(new char[message.length()]);
                replaceStr = replaceStr.replace('\u0000', '*');
                launcherList.add(message);
                if (!ThreadRunning) {
                    FilterHelper.doLauncherSdkFilterHandler();
                }
                return replaceStr;
            }
        }
        return FilterHelper.doLauncherSdkFilterInner(message);
    }

    public static void doLauncherSdkFilterHandler() {
        FilterWrapper.getSchduler().schedule(new Runnable(){

            @Override
            public void run() {
                ThreadRunning = true;
                try {
                    while (!launcherList.isEmpty()) {
                        while (!launcherList.isEmpty()) {
                            needLauncherList.add(launcherList.poll());
                        }
                        Thread.sleep(10L);
                        while (!needLauncherList.isEmpty()) {
                            FilterHelper.doLauncherSdkFilterInner(needLauncherList.poll());
                        }
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                ThreadRunning = false;
            }
        }, 1L, TimeUnit.MILLISECONDS);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String doLauncherSdkFilterInner(String message) {
        Helper.debugLog("doLauncherSdkFilterInner message:" + message);
        if (message == null || message.isEmpty()) {
            return message;
        }
        String result = message;
        MessageRequest mrq = new MessageRequest();
        int wordId = globalWordId++;
        Object object = new Object();
        lockObjectMap.put(wordId, object);
        mrq.send(4608, new Object[]{wordId, message});
        Object object2 = lockObjectMap.get(wordId);
        synchronized (object2) {
            try {
                lockObjectMap.get(wordId).wait(1000L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        lockObjectMap.remove(wordId);
        if (filterWordMap.containsKey(wordId)) {
            result = filterWordMap.get(wordId);
            filterWordMap.remove(wordId);
        }
        return result;
    }

    public static void loadFilterReInAnotherThread() {
        LoadFilterReRunnable loadThread = new LoadFilterReRunnable();
        FilterWrapper.getSchduler().scheduleAtFixedRate(loadThread, 0L, 20L, TimeUnit.MINUTES);
    }

    public static void loadFilterRe() {
        lastLoadFilterReTime = System.currentTimeMillis();
        String decryptionKey = GameState.filterkey;
        String filterPathPrefex = GameState.filterpath;
        logger.info("filterkey: " + decryptionKey);
        logger.info("filterpath: " + filterPathPrefex);
        ArrayList<String> md5List = FilterHelper.readFilterReMd5(filterPathPrefex + "/gamelib.txt", decryptionKey);
        ArrayList<String> reList = FilterHelper.readFilterReFromFile(filterPathPrefex + "/GAME_LIB.txt", decryptionKey, true, md5List.size() == 1 ? md5List.get(0) : null);
        logger.info("load filter re success!!! ");
        ArrayList<String> combined = new ArrayList<String>(reList);
        FilterHelper.setNamingFilterRE(combined);
        FilterHelper.setChatFilterRE(combined);
    }

    private static ArrayList<String> readFilterReMd5(String path, String decryptionKey) {
        ArrayList<String> md5List = new ArrayList<String>();
        if (null != decryptionKey && !decryptionKey.isEmpty()) {
            Path fileLocation = Paths.get(path, new String[0]);
            try {
                byte[] data = Files.readAllBytes(fileLocation);
                byte[] reBytes = AESHelper.Decrypt(data, decryptionKey);
                String reStr = new String(reBytes, StandardCharsets.UTF_8);
                String[] reStrArray = reStr.split("\n");
                md5List = new ArrayList<String>(Arrays.asList(reStrArray));
            }
            catch (Exception e) {
                logger.error("readFilterReMd5", (Throwable)e);
            }
        }
        return md5List;
    }

    private static ArrayList<String> readFilterReFromFile(String path, String decryptionKey, boolean checkMd5, String md5) {
        ArrayList<String> reList = new ArrayList<String>();
        if (!(null == decryptionKey || "" == decryptionKey || checkMd5 && null == md5)) {
            Path fileLocation = Paths.get(path, new String[0]);
            try {
                byte[] data = Files.readAllBytes(fileLocation);
                byte[] reBytes = AESHelper.Decrypt(data, decryptionKey);
                String reStr = new String(reBytes, StandardCharsets.UTF_8);
                String strMd5 = FilterHelper.getStringMd5(data);
                if (reStr.equals("") || !checkMd5 || !md5.equals(strMd5)) {
                    // empty if block
                }
                String[] reStrArray = reStr.split("\n");
                reList = new ArrayList<String>(Arrays.asList(reStrArray));
            }
            catch (Exception e) {
                logger.error("readFilterReFromFile", (Throwable)e);
            }
        }
        return reList;
    }

    private static String getStringMd5(byte[] original) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(original);
            byte[] digest = md.digest();
            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xFF));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void setNamingFilterRE(ArrayList<String> reList) {
        namingFilterRegularExpList = Collections.synchronizedList(reList);
    }

    public static void setChatFilterRE(ArrayList<String> reList) {
        chatFilterRegularExpList = Collections.synchronizedList(reList);
    }

    public static String filterMessage(String message, List<String> patternList) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        Object[] lines = message.split("\n", -1);
        for (int i = 0; i < lines.length; ++i) {
            lines[i] = FilterHelper.filterMessageInner((String)lines[i], patternList);
        }
        String result = Joiner.on((String)"\n").join(lines);
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String filterMessageInner(String message, List<String> patternList) {
        if (whiteMsgs.contains(message)) {
            return message;
        }
        String msgForFilter = message;
        int specialReplace = 32;
        char[] preFilterChars = message.toCharArray();
        StringBuilder specialFilterChars = new StringBuilder();
        for (int i = 0; i < preFilterChars.length; ++i) {
            if (preFilterChars[i] == '*') {
                specialFilterChars.append(' ');
                continue;
            }
            if (preFilterChars[i] == '\u00a7') {
                ++i;
                continue;
            }
            specialFilterChars.append(preFilterChars[i]);
        }
        String afterFilter = specialFilterChars.toString();
        List<String> list = patternList;
        synchronized (list) {
            block6: for (String re : patternList) {
                try {
                    Pattern pat = null;
                    pat = Pattern.compile(re);
                    Matcher matcher = pat.matcher(afterFilter);
                    while (matcher.find()) {
                        String temp = afterFilter.substring(matcher.start(), matcher.end());
                        String replaceStr = new String(new char[temp.length()]);
                        if (temp.length() == 0 || temp.equals(replaceStr)) continue block6;
                        afterFilter = String.join((CharSequence)"", afterFilter.substring(0, matcher.start()), replaceStr, afterFilter.substring(matcher.end(), afterFilter.length()));
                        matcher = pat.matcher(afterFilter);
                    }
                }
                catch (Throwable e) {
                }
            }
        }
        afterFilter = afterFilter.replaceAll("\u0000", "*");
        char[] afterFilterChars = afterFilter.toCharArray();
        StringBuilder resultChars = new StringBuilder();
        int j = 0;
        for (int i = 0; i < preFilterChars.length && j < afterFilterChars.length; ++i) {
            if (preFilterChars[i] == '*') {
                resultChars.append('*');
                ++j;
                continue;
            }
            if (preFilterChars[i] == '\u00a7') {
                resultChars.append(preFilterChars[i]);
                if (i + 1 >= preFilterChars.length) continue;
                resultChars.append(preFilterChars[i + 1]);
                ++i;
                continue;
            }
            resultChars.append(afterFilterChars[j]);
            ++j;
        }
        String result = resultChars.toString();
        if (!result.equals(message)) {
            FilterHelper.doLauncherSdkFilterInner(message);
        }
        return result;
    }

    public static String doNamingFilter(String message) {
        return FilterHelper.filterMessage(message, namingFilterRegularExpList);
    }

    public static Component filterChatComponentText(Component component, boolean useSdk, int logType) {
        return FilterHelper.filterChatComponentText(component, useSdk, logType, false);
    }

    public static Component filterChatComponentText(Component component, boolean useSdk, int logType, boolean nameLib) {
        String filterStr;
        if (component == null) {
            return component;
        }
        String str = component.getString();
        if (str.equals(filterStr = FilterWhiteListHelper.filter(str, useSdk, logType, nameLib))) {
            return component;
        }
        if (component.m_214077_() instanceof TranslatableContents) {
            TranslatableContents chat = (TranslatableContents)component.m_214077_();
            Object[] args = chat.m_237523_();
            for (int i = 0; i < args.length; ++i) {
                if (args[i] instanceof Component) {
                    args[i] = FilterHelper.filterChatComponentText((Component)args[i], useSdk, logType, nameLib);
                    continue;
                }
                if (!(args[i] instanceof String)) continue;
                args[i] = FilterWhiteListHelper.filter((String)args[i], useSdk, logType, nameLib);
            }
            TranslatableContents translatableContents = new TranslatableContents(chat.m_237508_(), chat.m_264577_(), args);
            MutableComponent translatableComponent = MutableComponent.m_237204_((ComponentContents)translatableContents);
            translatableComponent.m_6270_(component.m_7383_());
            ArrayList sliblings = new ArrayList();
            for (Component subChat : component.m_7360_()) {
                translatableComponent.m_7220_(FilterHelper.filterChatComponentText(subChat, useSdk, logType, nameLib));
            }
            return translatableComponent;
        }
        if (component.m_214077_() instanceof LiteralContents) {
            String text = ((LiteralContents)component.m_214077_()).f_237368_();
            MutableComponent textComponent = str.equals(text) ? Component.m_237113_((String)filterStr) : Component.m_237113_((String)FilterWhiteListHelper.filter(text, useSdk, logType, nameLib));
            textComponent.m_6270_(component.m_7383_());
            for (Component subChat : component.m_7360_()) {
                textComponent.m_7220_(FilterHelper.filterChatComponentText(subChat, useSdk, logType, nameLib));
            }
            return textComponent;
        }
        return component;
    }

    public static boolean isWhiteListCmd(String text) {
        String cmd = text;
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1).trim();
            for (String white : whiteCmds) {
                if (!cmd.startsWith(white)) continue;
                return true;
            }
        }
        return false;
    }

    public static void filterItemStack(ItemStack itemstack) {
        FilterHelper.filterItemStack(itemstack, true);
    }

    public static void filterItemStack(ItemStack itemstack, boolean enableFilterLog) {
        Item item;
        if (itemstack == null) {
            return;
        }
        if (itemstack.m_41788_()) {
            int logType = enableFilterLog ? 4 : 0;
            Component name = FilterHelper.filterChatComponentText(itemstack.m_41786_(), false, logType);
            itemstack.m_41714_(name);
        }
        if ((item = itemstack.m_41720_()) instanceof WrittenBookItem) {
            CompoundTag nbttagcompound = itemstack.m_41783_();
            if (nbttagcompound == null) {
                return;
            }
            int logType = enableFilterLog ? 3 : 0;
            String title = nbttagcompound.m_128461_("title");
            title = FilterWhiteListHelper.filter(title, false, logType);
            String author = nbttagcompound.m_128461_("author");
            author = FilterWhiteListHelper.filter(author, false, logType);
            ListTag nbttaglist = nbttagcompound.m_128437_("pages", 8);
            for (int i = 0; i < nbttaglist.size(); ++i) {
                String s = nbttaglist.m_128778_(i);
                s = FilterWhiteListHelper.filter(s, false, logType);
                nbttaglist.set(i, (Tag)StringTag.m_129297_((String)s));
            }
            nbttagcompound.m_128359_("title", title);
            nbttagcompound.m_128359_("author", author);
            nbttagcompound.m_128365_("pages", (Tag)nbttaglist);
        }
        if (item instanceof BlockItem) {
            CompoundTag nbttagcompound;
            if (!(((BlockItem)item).m_40614_() instanceof ShulkerBoxBlock)) {
                return;
            }
            CompoundTag itemCompound = itemstack.m_41783_();
            if (itemCompound != null && (nbttagcompound = itemCompound.m_128469_("BlockEntityTag")) != null) {
                ListTag itemtaglist = nbttagcompound.m_128437_("Items", 10);
                if (itemtaglist != null) {
                    for (int i = 0; i < itemtaglist.size(); ++i) {
                        CompoundTag compound = itemtaglist.m_128728_(i);
                        ItemStack stack = ItemStack.m_41712_(compound);
                        FilterHelper.filterItemStack(stack, enableFilterLog);
                        stack.m_41739_(compound);
                        itemtaglist.set(i, (Tag)compound);
                    }
                    nbttagcompound.m_128365_("Items", (Tag)itemtaglist);
                }
                itemCompound.m_128365_("BlockEntityTag", (Tag)nbttagcompound);
            }
        }
    }

    public class ReviewCode {
        public static final int OK = 0;
        public static final int Sheld = 1;
        public static final int Failed = 2;
    }
}

