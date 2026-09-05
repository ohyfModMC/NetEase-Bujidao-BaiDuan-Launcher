/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.netease.mc.mod.network.message.reply.Reply
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.level.GameRules
 *  net.minecraft.world.level.GameRules$Category
 *  net.minecraft.world.level.GameRules$Key
 *  net.minecraft.world.level.Level
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.opencommand.reply;

import com.google.gson.Gson;
import com.netease.mc.mod.network.message.reply.Reply;
import com.netease.mc.mod.network.message.request.MessageRequest;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReplyGameRules
extends Reply {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int SMID = 4100;
    public static final int GameRuleCMD = 4101;
    public static final int LoadWorld = 4104;
    public static final int ShortCuts = 4105;
    private static Gson gson = new Gson();
    public static String[] RuleNames = new String[]{"doDaylightCycle", "doMobLoot", "mobGriefing", "doFireTick", "doMobSpawning", "keepInventory"};
    static Map<String, String> rules = new HashMap<String, String>();
    private static final Map<String, GameRules.Category> categories = new HashMap<String, GameRules.Category>(){
        {
            this.put("doFireTick", GameRules.Category.UPDATES);
            this.put("doMobLoot", GameRules.Category.DROPS);
            this.put("doMobSpawning", GameRules.Category.SPAWNING);
            this.put("keepInventory", GameRules.Category.PLAYER);
            this.put("mobGriefing", GameRules.Category.MOBS);
        }
    };

    public void handler() {
        LOGGER.info("ReplyGameRules:");
        ReplyGameRules.SendGameRules();
    }

    public static void InitGameRule() {
        if (rules.isEmpty()) {
            GameRules gameRule = Minecraft.m_91087_().f_91073_.m_6106_().m_5470_();
            for (String s : RuleNames) {
                rules.put(s, gameRule.m_46207_(new GameRules.Key(s, categories.getOrDefault(s, GameRules.Category.PLAYER))) ? "1" : "0");
            }
        }
    }

    public static void SendGameRules() {
        Minecraft mc = Minecraft.m_91087_();
        ReplyGameRules.InitGameRule();
        GameRules gameRule = null;
        if (mc.m_91092_() != null) {
            gameRule = mc.m_91092_().m_129880_(Level.f_46428_).m_6106_().m_5470_();
            for (String s : RuleNames) {
                rules.put(s, gameRule.m_46207_(new GameRules.Key(s, categories.getOrDefault(s, GameRules.Category.PLAYER))) ? "1" : "0");
            }
        }
        rules.put("gamemode", "" + mc.f_91072_.m_105295_().m_46392_());
        String msg = gson.toJson(rules);
        MessageRequest mrq = new MessageRequest();
        mrq.send(4101, new Object[]{msg});
    }

    public static String[] GetGameRuleValue() {
        Minecraft mc = Minecraft.m_91087_();
        if (mc.m_91092_() != null) {
            GameRules gamerule = mc.m_91092_().m_129880_(Level.f_46428_).m_6106_().m_5470_();
            String[] values = new String[RuleNames.length];
            for (int i = 0; i < RuleNames.length; ++i) {
                values[i] = "" + gamerule.m_46207_(new GameRules.Key(RuleNames[i], categories.getOrDefault(RuleNames[i], GameRules.Category.PLAYER)));
            }
            return values;
        }
        return new String[0];
    }

    public static void SetGameRule(String type, String value) {
        ReplyGameRules.InitGameRule();
        if (rules.containsKey(type)) {
            String vs = value.equals("true") ? "1" : "0";
            rules.put(type, vs);
        }
    }

    public static void SendLoadWorld() {
        MessageRequest mrq = new MessageRequest();
        mrq.send(4104, new Object[]{"loadworld"});
    }

    public static void SendShortcuts() {
        MessageRequest mrq = new MessageRequest();
        mrq.send(4105, new Object[]{""});
    }
}

