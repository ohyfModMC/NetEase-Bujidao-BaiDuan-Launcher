/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  net.minecraft.client.Minecraft
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.IntArrayTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.Tag
 */
package com.netease.mc.mod.filter;

import com.netease.mc.mod.filter.FilterHelper;
import com.netease.mc.mod.filter.FilterWrapper;
import com.netease.mc.mod.filter.Helper;
import com.netease.mc.mod.network.message.request.MessageRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

public class FilterWhiteListHelper {
    public static Comparator<Entry> entryComparator = new Comparator<Entry>(){

        @Override
        public int compare(Entry c1, Entry c2) {
            return c2.timestamp - c1.timestamp;
        }
    };
    static final Map<Integer, Entry> maps = new ConcurrentHashMap<Integer, Entry>();
    static final Map<Integer, Entry> nameMaps = new ConcurrentHashMap<Integer, Entry>();
    static final Map<Integer, Entry> cacheMaps = new ConcurrentHashMap<Integer, Entry>();
    static final int maxMapsLen = 100000;
    static int version = 10000;
    static long UpdateTime = 0L;
    static volatile boolean isDirty = false;
    static final boolean envsdk = true;

    public static void init() {
        File dir = Minecraft.m_91087_().f_91069_;
        try {
            String content = new String(Files.readAllBytes(Paths.get(new File(dir, "WordV").getPath(), new String[0])));
            version = Integer.parseInt(content);
        }
        catch (IOException e) {
            version = 10000;
        }
        FilterWrapper.getSchduler().scheduleAtFixedRate(new Runnable(){

            @Override
            public void run() {
                FilterWhiteListHelper.NotifyToLauncher();
                FilterWhiteListHelper.save();
            }
        }, 1L, 600L, TimeUnit.SECONDS);
        int now = (int)(System.currentTimeMillis() / 1000L);
        try {
            File cacheFile = new File(dir, "wordsNew.dat");
            if (!cacheFile.exists()) {
                UpdateTime = System.currentTimeMillis();
                return;
            }
            CompoundTag compound = NbtIo.m_128939_((InputStream)new FileInputStream(cacheFile));
            ListTag nbttaglist = compound.m_128437_("data", 11);
            int tagVersion = compound.m_128451_("version");
            boolean hasUpdateTime = compound.m_128441_("UpdateTime");
            if (tagVersion != version || !hasUpdateTime) {
                UpdateTime = System.currentTimeMillis();
                return;
            }
            long lastUpdateTime = compound.m_128454_("UpdateTime");
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdateTime >= 604800000L) {
                UpdateTime = currentTime;
                return;
            }
            UpdateTime = lastUpdateTime;
            for (int i = 0; i < nbttaglist.size(); ++i) {
                int[] array = nbttaglist.m_128767_(i);
                if (array.length < 3) continue;
                Entry entry = new Entry(array);
                maps.put(entry.textHash, entry);
            }
            ListTag namenbttaglist = compound.m_128437_("nameData", 11);
            for (int i = 0; i < namenbttaglist.size(); ++i) {
                int[] array = namenbttaglist.m_128767_(i);
                if (array.length < 3) continue;
                Entry entry = new Entry(array);
                nameMaps.put(entry.textHash, entry);
            }
        }
        catch (Exception e) {
            return;
        }
    }

    public static void save() {
        if (!isDirty) {
            return;
        }
        Helper.debugLog("start save maps!");
        if (maps.size() > 100000) {
            FilterWhiteListHelper.expireMaps(50000, maps);
        }
        if (nameMaps.size() > 100000) {
            FilterWhiteListHelper.expireMaps(50000, nameMaps);
        }
        try {
            File dir = Minecraft.m_91087_().f_91069_;
            CompoundTag compound = new CompoundTag();
            compound.m_128405_("version", version);
            if (UpdateTime != 0L) {
                compound.m_128356_("UpdateTime", UpdateTime);
            }
            ListTag data = new ListTag();
            int idx = 0;
            for (Entry entry : maps.values()) {
                IntArrayTag array = new IntArrayTag(entry.toArray());
                data.add((Object)array);
                ++idx;
            }
            compound.m_128365_("data", (Tag)data);
            ListTag nameData = new ListTag();
            for (Entry entry : nameMaps.values()) {
                IntArrayTag array = new IntArrayTag(entry.toArray());
                nameData.add((Object)array);
            }
            compound.m_128365_("nameData", (Tag)nameData);
            NbtIo.m_128947_((CompoundTag)compound, (OutputStream)new FileOutputStream(new File(dir, "wordsNew.dat")));
        }
        catch (Exception e) {
            FilterWrapper.CatchException(e);
        }
    }

    public static boolean InWhite(String input, boolean nameLib) {
        Entry entry = cacheMaps.getOrDefault(input.hashCode(), null);
        if (entry != null) {
            return true;
        }
        Map<Integer, Entry> filterMap = nameLib ? nameMaps : maps;
        entry = filterMap.getOrDefault(input.hashCode(), null);
        return entry != null;
    }

    public static String filter(String input, boolean useSdk, int type) {
        return FilterWhiteListHelper.filter(input, useSdk, type, false);
    }

    public static String filter(String input, boolean useSdk, int type, boolean nameLib) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        Entry entry = cacheMaps.getOrDefault(input.hashCode(), null);
        if (entry != null) {
            return input;
        }
        Map<Integer, Entry> filterMap = nameLib ? nameMaps : maps;
        int now = (int)(System.currentTimeMillis() / 1000L);
        entry = filterMap.getOrDefault(input.hashCode(), null);
        if (entry != null) {
            if (entry.version == version) {
                return input;
            }
            if (!useSdk) {
                // empty if block
            }
            String output = FilterHelper.doSdkFilter(input, nameLib);
            FilterWhiteListHelper.AddWhiteList(output, now, nameLib);
            return output;
        }
        if (!useSdk) {
            // empty if block
        }
        String output = FilterHelper.doSdkFilter(input, nameLib);
        if (type != 0) {
            FilterWhiteListHelper.SaLog(input, output, type);
        }
        FilterWhiteListHelper.AddWhiteList(output, now, nameLib);
        return output;
    }

    private static void SaLog(String input, String output, int type) {
        Helper.debugLog("send SaLog to Launcher:" + input + " " + output);
        int now = (int)(System.currentTimeMillis() / 1000L);
        FilterWhiteListHelper.AddCacheWhiteList(output, now);
        MessageRequest mrq = new MessageRequest();
        mrq.send(4609, new Object[]{input, output, now, type});
    }

    public static void AddWhiteList(String output, int timestamp) {
        FilterWhiteListHelper.AddWhiteList(output, timestamp, false);
    }

    public static void AddWhiteList(String output, int timestamp, boolean nameLib) {
        if (cacheMaps.containsKey(output.hashCode())) {
            cacheMaps.remove(output.hashCode());
        }
        Map<Integer, Entry> filterMap = nameLib ? nameMaps : maps;
        int[] array = new int[]{output.hashCode(), timestamp, version};
        Entry entry = new Entry(array);
        filterMap.put(entry.textHash, entry);
        isDirty = true;
    }

    public static void AddCacheWhiteList(String output, int timestamp) {
        int[] array = new int[]{output.hashCode(), timestamp, version};
        Entry entry = new Entry(array);
        cacheMaps.put(entry.textHash, entry);
    }

    public static void clearCache() {
        cacheMaps.clear();
    }

    public static void expireMaps(int num, Map<Integer, Entry> map) {
        PriorityQueue<Entry> queue = new PriorityQueue<Entry>(num, entryComparator);
        for (Entry entry : map.values()) {
            if (queue.size() < num) {
                queue.add(entry);
                continue;
            }
            if (entry.timestamp >= queue.peek().timestamp) continue;
            queue.poll();
            queue.add(entry);
        }
        for (Object object : queue.toArray()) {
            Entry entry = (Entry)object;
            map.remove(entry.textHash);
        }
    }

    public static void NotifyToLauncher() {
        MessageRequest mrq = new MessageRequest();
        mrq.send(4610, new Object[0]);
    }

    public static class Entry {
        public int textHash;
        public int timestamp;
        public int version;

        public Entry(int[] array) {
            this.textHash = array[0];
            this.timestamp = array[1];
            this.version = array[2];
        }

        public int[] toArray() {
            int[] array = new int[]{this.textHash, this.timestamp, this.version};
            return array;
        }
    }
}

