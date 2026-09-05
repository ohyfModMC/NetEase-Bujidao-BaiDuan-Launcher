/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.Cache
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  com.google.common.collect.Iterables
 *  com.google.common.hash.Hashing
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonParseException
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.minecraft.MinecraftProfileTexture
 *  com.mojang.authlib.minecraft.MinecraftProfileTexture$Type
 *  com.mojang.authlib.properties.Property
 *  com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService
 *  com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.util.UUIDTypeAdapter
 *  com.netease.mc.mod.network.common.GameState
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.texture.HttpTexture
 *  net.minecraft.client.resources.SkinManager
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtUtils
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.level.block.entity.SkullBlockEntity
 *  org.apache.commons.codec.Charsets
 *  org.apache.commons.codec.binary.Base64
 *  org.apache.commons.codec.digest.DigestUtils
 *  org.apache.commons.io.FileUtils
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.skin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.util.UUIDTypeAdapter;
import com.netease.mc.mod.network.common.GameState;
import com.netease.mc.mod.network.message.request.MessageRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SkinHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final SkinHandler skinHandler = new SkinHandler();
    public static HashMap<String, Object> lockObjectMap = new HashMap();
    public static HashMap<String, String> nameSkinMap = new HashMap();
    public static HashMap<String, String> nameCapeMap = new HashMap();
    public static HashMap<String, Boolean> nameSkinMode = new HashMap();
    private static final File assetSkinsDir = new File("./assets/skins");
    private static final int TIMEOUT = 60000;
    private static Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, (Object)new UUIDTypeAdapter()).create();
    private static final Cache<String, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> cache = CacheBuilder.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).build();
    private static final LoadingCache<GameProfile, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> skinCacheLoader = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).build((CacheLoader)new CacheLoader<GameProfile, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>>(){

        public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> load(GameProfile p_load_1_) throws Exception {
            return Minecraft.m_91087_().m_91108_().getTextures(p_load_1_, false);
        }
    });
    private static ScheduledExecutorService schduler = Executors.newScheduledThreadPool(10);
    private static final String[] WHITELISTED_DOMAINS = new String[]{".minecraft.net", ".mojang.com", ".163.com", ".netease.com"};
    private static final Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> defaultReturn = Collections.emptyMap();
    private static final Set<GameProfile> onLoading = Collections.newSetFromMap(new ConcurrentHashMap());

    public static String CopySkinToAsset(File f) {
        try {
            if (f == null) {
                return null;
            }
            String sha = DigestUtils.sha256Hex((InputStream)new FileInputStream(f)).toLowerCase();
            String filename = Hashing.sha1().hashUnencodedChars((CharSequence)sha).toString();
            File subDir = new File(assetSkinsDir, filename.substring(0, 2));
            subDir.mkdirs();
            File skin = new File(subDir, filename);
            FileUtils.copyFile((File)f, (File)skin);
            return "http://127.0.0.1/" + sha;
        }
        catch (Exception ex) {
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTexturesWrapper(YggdrasilMinecraftSessionService service, GameProfile profile, boolean requireSecure) {
        String url;
        if (profile == null) {
            return new HashMap<MinecraftProfileTexture.Type, MinecraftProfileTexture>();
        }
        Thread current = Thread.currentThread();
        if (current.getName().contains("Client")) {
            Exception ex = new Exception();
            StringWriter result = new StringWriter();
            PrintWriter printWriter = new PrintWriter(result);
            ex.printStackTrace(printWriter);
            LOGGER.info(((Object)result).toString());
            return SkinHandler.getTextures(profile, requireSecure);
        }
        if (profile.getName() == null) {
            return new HashMap<MinecraftProfileTexture.Type, MinecraftProfileTexture>();
        }
        String name = profile.getName();
        LOGGER.info(String.format("player %s start loading skin , ThreadID %s", name, current.getName()));
        HashMap<MinecraftProfileTexture.Type, MinecraftProfileTexture> resultCache = (HashMap<MinecraftProfileTexture.Type, MinecraftProfileTexture>)cache.getIfPresent((Object)name);
        if (resultCache != null && resultCache.size() != 0) {
            return resultCache;
        }
        UUID uuid = profile.getId();
        boolean nonV4 = uuid == null || uuid.version() != 4;
        if (nonV4) {
            LOGGER.info(String.format("player %s non-v4 UUID, trying server texture first, UUID = %s", name, uuid == null ? "null" : uuid.toString()));
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> serverTex = SkinHandler.getTextures(profile, requireSecure);
            if (serverTex != null && !serverTex.isEmpty()) {
                if (serverTex instanceof HashMap) {
                    cache.put(name, (HashMap)serverTex);
                }
                return serverTex;
            }
            LOGGER.info(String.format("player %s no server texture, falling back to launcher 2050", name));
        }
        LOGGER.info(String.format("player %s start loading skin from netease, UUID = %s", name, uuid.toString()));
        resultCache = new HashMap<MinecraftProfileTexture.Type, MinecraftProfileTexture>();
        String LocalSkinUrl = null;
        String LocalCapeUrl = null;
        String username = profile.getName();
        Object object = new Object();
        lockObjectMap.put(username, object);
        MessageRequest mrq = new MessageRequest();
        LOGGER.info("skin:send msg to launcher!");
        mrq.send(2050, new Object[]{GameState.gameid, username, profile.getId().toString()});
        Object object2 = lockObjectMap.get(username);
        synchronized (object2) {
            try {
                lockObjectMap.get(username).wait(60000L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        lockObjectMap.remove(username);
        if (nameSkinMap.containsKey(profile.getName())) {
            LocalSkinUrl = nameSkinMap.get(username);
        }
        if (nameCapeMap.containsKey(profile.getName())) {
            LocalCapeUrl = nameCapeMap.get(username);
        }
        if (LocalSkinUrl != null && LocalSkinUrl != "") {
            LOGGER.info(String.format("player %s start loading skinurl : %s", name, LocalSkinUrl));
            url = SkinHandler.CopySkinToAsset(new File(LocalSkinUrl));
            if (url != null) {
                boolean isSlim = nameSkinMode.containsKey(name) ? nameSkinMode.get(name) : false;
                HashMap<String, String> modelmap = null;
                if (isSlim) {
                    modelmap = new HashMap<String, String>(){
                        {
                            this.put("model", "slim");
                        }
                    };
                }
                resultCache.put(MinecraftProfileTexture.Type.SKIN, new MinecraftProfileTexture(url, (Map)modelmap));
            }
        }
        if (LocalCapeUrl != null && LocalCapeUrl != "") {
            LOGGER.info(String.format("player %s start loading capeurl : %s", name, LocalCapeUrl));
            url = SkinHandler.CopySkinToAsset(new File(LocalCapeUrl));
            if (url != null) {
                resultCache.put(MinecraftProfileTexture.Type.CAPE, new MinecraftProfileTexture(url, null));
            }
        }
        cache.put(name, resultCache);
        return resultCache;
    }

    private static boolean isWhitelistedDomain(String url) {
        URI uri = null;
        try {
            uri = new URI(url);
        }
        catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL '" + url + "'");
        }
        String domain = uri.getHost();
        for (int i = 0; i < WHITELISTED_DOMAINS.length; ++i) {
            if (!domain.endsWith(WHITELISTED_DOMAINS[i])) continue;
            return true;
        }
        return false;
    }

    public static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile profile, boolean requireSecure) {
        MinecraftTexturesPayload result;
        Property textureProperty = (Property)Iterables.getFirst((Iterable)profile.getProperties().get("textures"), null);
        Minecraft mc = Minecraft.m_91087_();
        if (textureProperty == null) {
            LOGGER.info(String.format("player %s no textures property from server", profile.getName()));
            return new HashMap<MinecraftProfileTexture.Type, MinecraftProfileTexture>();
        }
        try {
            String json = new String(Base64.decodeBase64((String)textureProperty.getValue()), Charsets.UTF_8);
            result = (MinecraftTexturesPayload)gson.fromJson(json, MinecraftTexturesPayload.class);
        }
        catch (JsonParseException e) {
            LOGGER.info(String.format("player %s textures property json parse failed", profile.getName()));
            return new HashMap<MinecraftProfileTexture.Type, MinecraftProfileTexture>();
        }
        if (result.getTextures() == null) {
            LOGGER.info(String.format("player %s textures payload empty", profile.getName()));
            return new HashMap<MinecraftProfileTexture.Type, MinecraftProfileTexture>();
        }
        for (Map.Entry<MinecraftProfileTexture.Type, MinecraftProfileTexture> entry : result.getTextures().entrySet()) {
            LOGGER.info(String.format("player %s server texture %s -> %s", profile.getName(), entry.getKey(), entry.getValue().getUrl()));
        }
        return result.getTextures();
    }

    public static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getInsecureSkinInformationWrapper(SkinManager manager, final GameProfile gp) {
        if (onLoading.contains(gp)) {
            return defaultReturn;
        }
        Map ret = (Map)skinCacheLoader.getIfPresent((Object)gp);
        if (ret == null) {
            onLoading.add(gp);
            String name = gp.getName() == null ? gp.toString() : gp.getName();
            new Thread(new Runnable(){

                @Override
                public void run() {
                    skinCacheLoader.getUnchecked(gp);
                    onLoading.remove(gp);
                }
            }, "Skin-Fetch-" + name).start();
            return defaultReturn;
        }
        return ret;
    }

    public static void loadSkullTexture(final GameProfile profile, final CompoundTag target) {
        target.m_128473_("SkullOwner");
        schduler.schedule(new Runnable(){

            @Override
            public void run() {
                SkullBlockEntity.m_155738_((GameProfile)profile, prof -> target.m_128365_("SkullOwner", (Tag)NbtUtils.m_129230_((CompoundTag)new CompoundTag(), (GameProfile)prof)));
            }
        }, 1L, TimeUnit.MILLISECONDS);
    }

    public static NativeImage processLegacySkinWrapper(HttpTexture texture, NativeImage p_118033_) {
        int i = p_118033_.m_85084_();
        int j = p_118033_.m_84982_();
        if (j % 64 == 0 && i % 32 == 0) {
            boolean flag;
            boolean bl = flag = j / i == 2;
            if (flag) {
                NativeImage nativeimage = new NativeImage(64, 64, true);
                nativeimage.m_85054_(p_118033_);
                p_118033_.close();
                p_118033_ = nativeimage;
                nativeimage.m_84997_(0, 32, 64, 32, 0);
                nativeimage.m_85025_(4, 16, 16, 32, 4, 4, true, false);
                nativeimage.m_85025_(8, 16, 16, 32, 4, 4, true, false);
                nativeimage.m_85025_(0, 20, 24, 32, 4, 12, true, false);
                nativeimage.m_85025_(4, 20, 16, 32, 4, 12, true, false);
                nativeimage.m_85025_(8, 20, 8, 32, 4, 12, true, false);
                nativeimage.m_85025_(12, 20, 16, 32, 4, 12, true, false);
                nativeimage.m_85025_(44, 16, -8, 32, 4, 4, true, false);
                nativeimage.m_85025_(48, 16, -8, 32, 4, 4, true, false);
                nativeimage.m_85025_(40, 20, 0, 32, 4, 12, true, false);
                nativeimage.m_85025_(44, 20, -8, 32, 4, 12, true, false);
                nativeimage.m_85025_(48, 20, -16, 32, 4, 12, true, false);
                nativeimage.m_85025_(52, 20, -8, 32, 4, 12, true, false);
            }
            invokeHttpTextureMethod("m_118022_", p_118033_, 0, 0, 32, 16);
            if (flag) {
                invokeHttpTextureMethod("m_118012_", p_118033_, 32, 0, 64, 32);
            }
            invokeHttpTextureMethod("m_118022_", p_118033_, 0, 16, 64, 32);
            invokeHttpTextureMethod("m_118022_", p_118033_, 16, 48, 48, 64);
            return p_118033_;
        }
        p_118033_.close();
        LOGGER.warn("Discarding incorrectly sized ({}x{}) skin texture from {}", j, i, texture.toString());
        return null;
    }

    private static void invokeHttpTextureMethod(String method, NativeImage img, int x1, int y1, int x2, int y2) {
        try {
            java.lang.reflect.Method m = HttpTexture.class.getDeclaredMethod(method, NativeImage.class, int.class, int.class, int.class, int.class);
            m.setAccessible(true);
            m.invoke(null, img, x1, y1, x2, y2);
        } catch (Exception e) {
            LOGGER.warn("Failed to invoke HttpTexture." + method + ": " + e.getMessage());
        }
    }
}

