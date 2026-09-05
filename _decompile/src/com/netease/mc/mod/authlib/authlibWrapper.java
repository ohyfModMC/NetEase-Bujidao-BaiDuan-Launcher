/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.exceptions.AuthenticationUnavailableException
 *  com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService
 *  com.netease.mc.mod.network.common.Common
 *  com.netease.mc.mod.network.common.GameState
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.authlib;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.netease.mc.mod.authlib.AuthenticationCpp;
import com.netease.mc.mod.network.common.Common;
import com.netease.mc.mod.network.common.GameState;
import java.net.InetAddress;
import java.util.UUID;
import javax.naming.AuthenticationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class authlibWrapper {
    public static Logger logger = LogManager.getLogger();

    public static GameProfile fillGameProfileWrapper(YggdrasilMinecraftSessionService service, GameProfile profile, boolean requireSecure) {
        return profile;
    }

    public static void joinServerWrapper(YggdrasilMinecraftSessionService service, GameProfile profile, String authenticationToken, String serverId) throws AuthenticationException {
        try {
            if (GameState.userPropertiesEx == null || GameState.userPropertiesEx.GameType != 2) {
                return;
            }
            logger.info("joinServerWrapper");
            AuthenticationCpp auth = new AuthenticationCpp();
            String portString = System.getProperty("launcherControlPort");
            if (portString.isEmpty()) {
                throw new AuthenticationException("Unavailable port");
            }
            auth.Authentication(Integer.parseInt(portString), serverId);
        }
        catch (Exception e) {
            logger.info("joinServerWrapper Error");
            Common.CatchException((Throwable)e);
            throw new AuthenticationException(e.getMessage());
        }
    }

    public static GameProfile hasJoinedServerWrapper(YggdrasilMinecraftSessionService service, GameProfile user, String serverId, InetAddress address) throws AuthenticationUnavailableException {
        String name = user.getName();
        UUID uuid = UUID.nameUUIDFromBytes(name.getBytes());
        GameProfile result = new GameProfile(uuid, name);
        return result;
    }
}

