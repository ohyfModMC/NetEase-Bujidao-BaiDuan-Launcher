/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.io.Files
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.netease.mc.mod.network.common.GameState
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.fml.common.Mod
 *  org.apache.commons.io.IOUtils
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod;

import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.mc.mod.departmod.guibuttondisplay.GuiButtonTransfer;
import com.netease.mc.mod.encryption.EncryptionEnableWrapper;
import com.netease.mc.mod.filter.FilterMod;
import com.netease.mc.mod.friendplay.FriendPlayMod;
import com.netease.mc.mod.fullscreenpopup.FullscreenPopup;
import com.netease.mc.mod.network.common.GameState;
import com.netease.mc.mod.network.networkMod.NetworkSocketMod;
import com.netease.mc.mod.opencommand.OpenCommandMod;
import com.netease.mc.mod.playermanager.PlayerManagerMod;
import com.netease.mc.mod.screenshot.ScreenShotMod;
import com.netease.mc.mod.skin.SkinMod;
import com.netease.mc.mod.store.StoreMod;
import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value="netease_official")
public class NeteaseOfficialMod {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "netease_official";
    private Boolean needPopup;

    public NeteaseOfficialMod() {
        NetworkSocketMod.init();
        FriendPlayMod.init();
        GuiButtonTransfer.Init();
        SkinMod.init();
        PlayerManagerMod.init();
        FilterMod.init();
        ScreenShotMod.init();
        if (this.CheckGamePopUp()) {
            OpenCommandMod.init();
            FullscreenPopup.init();
        }
        if (GameState.userPropertiesEx != null && GameState.userPropertiesEx.GameType == 2) {
            StoreMod.init();
        }
        MinecraftForge.EVENT_BUS.register((Object)this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean CheckGamePopUp() {
        if (this.needPopup != null) {
            return this.needPopup;
        }
        File modSetting = new File("modsetting.cfg");
        BufferedReader bufferedreader = null;
        try {
            if (modSetting.exists() && modSetting.isFile()) {
                String line;
                bufferedreader = Files.newReader((File)modSetting, (Charset)StandardCharsets.UTF_8);
                StringBuffer buffer = new StringBuffer();
                while ((line = bufferedreader.readLine()) != null) {
                    buffer.append(line);
                }
                String jsonText = buffer.toString();
                JsonElement jsonElement = JsonParser.parseString((String)jsonText);
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    if (jsonObject.has("GamePopUP") && jsonObject.get("GamePopUP").isJsonPrimitive()) {
                        this.needPopup = jsonObject.get("GamePopUP").getAsBoolean();
                    } else {
                        LOGGER.error("The 'GamePopUP' field is missing or not a boolean.");
                    }
                    if (jsonObject.has("NetworkEncrypt") && jsonObject.get("NetworkEncrypt").isJsonPrimitive()) {
                        EncryptionEnableWrapper.NetworkEncrypt = jsonObject.get("NetworkEncrypt").getAsBoolean();
                    } else {
                        LOGGER.error("The 'NetworkEncrypt' field is missing or not a boolean.");
                    }
                    if (jsonObject.has("NetworkCompressionThreshold")) {
                        JsonElement networkCompressionElement = jsonObject.get("NetworkCompressionThreshold");
                        if (networkCompressionElement.isJsonPrimitive() && networkCompressionElement.getAsJsonPrimitive().isNumber()) {
                            EncryptionEnableWrapper.NetworkCompressionThreshold = networkCompressionElement.getAsInt();
                        } else {
                            LOGGER.error("The 'NetworkCompressionThreshold' field is not an integer.");
                        }
                    } else {
                        LOGGER.error("The 'NetworkCompressionThreshold' field is missing.");
                    }
                } else {
                    LOGGER.error("modsetting.cfg is not a valid JSON object.");
                }
                boolean bl = this.needPopup;
                IOUtils.closeQuietly((Reader)bufferedreader);
                return bl;
            }
            IOUtils.closeQuietly(bufferedreader);
        }
        catch (Exception e) {
            LOGGER.error("CheckGamePopUp", (Throwable)e);
        }
        finally {
            IOUtils.closeQuietly(bufferedreader);
        }
        this.needPopup = true;
        return true;
    }
}

