/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.common.GameState
 *  com.netease.mc.mod.network.common.GameState$GameS
 *  com.netease.mc.mod.network.message.reply.Reply
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.Difficulty
 *  net.minecraft.world.level.GameRules
 *  net.minecraft.world.level.GameType
 *  net.minecraft.world.level.LevelSettings
 *  net.minecraft.world.level.WorldDataConfiguration
 *  net.minecraft.world.level.levelgen.WorldDimensions
 *  net.minecraft.world.level.levelgen.WorldOptions
 *  net.minecraft.world.level.levelgen.presets.WorldPreset
 *  net.minecraft.world.level.levelgen.presets.WorldPresets
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.friendplay.message.reply;

import com.netease.mc.mod.friendplay.FriendPlayMod;
import com.netease.mc.mod.network.common.GameState;
import com.netease.mc.mod.network.message.reply.Reply;
import com.netease.mc.mod.network.message.request.MessageRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReplyNewSingleV2
extends Reply {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int SMID = 1540;
    private static ResourceKey<WorldPreset> preset = null;

    public static WorldDimensions createWorldDimensions(RegistryAccess p_251732_) {
        if (preset == null) {
            return null;
        }
        return ((WorldPreset)p_251732_.m_175515_(Registries.f_256729_).m_246971_(preset).m_203334_()).m_247748_();
    }

    public void handler(byte gameType, byte onlineGameType, byte isCheat, boolean othercheat, byte isBuild, byte isBonus, byte worldType, byte isOnlineGame, String Seed, String save, String levelName) {
        String type = FriendPlayMod.getGameType(gameType);
        String onlineType = FriendPlayMod.getGameType(onlineGameType);
        boolean cheat = isCheat == 1;
        boolean build = isBuild == 1;
        boolean bonus = isBonus == 1;
        boolean onlineGame = isOnlineGame == 1;
        byte result = 0;
        int port = 0;
        if (GameState.gameState == GameState.GameS.INIT) {
            result = 1;
        } else if (GameState.gameState == GameState.GameS.LOAD) {
            try {
                Minecraft mc = Minecraft.m_91087_();
                Path p = mc.m_91392_().m_78257_().resolve(save);
                List list = mc.m_91392_().m_289863_().m_289885_(p, true);
                if (!list.isEmpty()) {
                    throw new IOException("Path " + save + " is not a directory");
                }
                preset = null;
                boolean ishardcore = false;
                if (gameType == 2) {
                    ishardcore = true;
                }
                GameType wType = GameType.m_46400_((String)type);
                long worldSeed = new Random().nextLong();
                if (Seed != null && Seed.length() != 0) {
                    try {
                        long tmp = Long.parseLong(Seed);
                        if (tmp != 0L) {
                            worldSeed = tmp;
                        }
                    }
                    catch (NumberFormatException e) {
                        worldSeed = Seed.hashCode();
                    }
                }
                LevelSettings worldsettings = new LevelSettings(levelName, wType, ishardcore, Difficulty.NORMAL, cheat, new GameRules(), WorldDataConfiguration.f_244649_);
                ResourceKey[] presets = new ResourceKey[]{WorldPresets.f_226437_, WorldPresets.f_226438_, WorldPresets.f_226439_, WorldPresets.f_226440_};
                preset = presets[worldType];
                WorldOptions options = new WorldOptions(worldSeed, build, bonus);
                mc.m_231466_().m_233157_(save, worldsettings, options, ReplyNewSingleV2::createWorldDimensions);
                if (onlineGame) {
                    FriendPlayMod.setLanGameStates(onlineGame, GameType.m_46400_((String)onlineType), othercheat);
                    return;
                }
                GameState.gameState = GameState.GameS.SINGLE;
            }
            catch (IOException e) {
                GameState.gameState = GameState.GameS.LOAD;
                result = 4;
                LOGGER.error("ReplyNewSingleV2", (Throwable)e);
            }
            catch (Exception e) {
                LOGGER.error("ReplyNewSingleV2", (Throwable)e);
                GameState.gameState = GameState.GameS.LOAD;
                result = 3;
            }
        } else {
            result = 2;
        }
        MessageRequest mrq = new MessageRequest();
        mrq.send(1540, new Object[]{GameState.gameid, result, port});
    }
}

