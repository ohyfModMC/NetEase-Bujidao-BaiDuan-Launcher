/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.scores.Objective
 *  net.minecraft.world.scores.PlayerTeam
 *  net.minecraft.world.scores.Score
 *  net.minecraft.world.scores.criteria.ObjectiveCriteria
 *  net.minecraft.world.scores.criteria.ObjectiveCriteria$RenderType
 */
package com.netease.mc.mod.filter.old;

import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public interface IScoreboardOld {
    public Objective addObjectiveOld(String var1, ObjectiveCriteria var2, Component var3, ObjectiveCriteria.RenderType var4);

    public PlayerTeam addPlayerTeamOld(String var1);

    public boolean addPlayerToTeamOld(String var1, PlayerTeam var2);

    public Score getOrCreatePlayerScoreOld(String var1, Objective var2);

    public void removePlayerFromTeamOld(String var1, PlayerTeam var2);

    public void removePlayerTeamOld(PlayerTeam var1);
}

