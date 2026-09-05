/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.game.ClientboundBossEventPacket
 *  net.minecraft.network.protocol.game.ClientboundDisconnectPacket
 *  net.minecraft.network.protocol.game.ClientboundMapItemDataPacket
 *  net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket
 *  net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
 *  net.minecraft.network.protocol.game.ClientboundSetScorePacket
 *  net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundTagQueryPacket
 */
package com.netease.mc.mod.oldInterface;

import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;

public interface IClientPacketListenerOld {
    public void handleOpenScreenOld(ClientboundOpenScreenPacket var1);

    public void handleDisconnectOld(ClientboundDisconnectPacket var1);

    public void handleMapItemDataOld(ClientboundMapItemDataPacket var1);

    public void handleTagQueryPacketOld(ClientboundTagQueryPacket var1);

    public void handlePlayerCombatKillOld(ClientboundPlayerCombatKillPacket var1);

    public void setActionBarTextOld(ClientboundSetActionBarTextPacket var1);

    public void setTitleTextOld(ClientboundSetTitleTextPacket var1);

    public void setSubtitleTextOld(ClientboundSetSubtitleTextPacket var1);

    public void handleBossUpdateOld(ClientboundBossEventPacket var1);

    public void handleSetPlayerTeamPacketOld(ClientboundSetPlayerTeamPacket var1);

    public void handleSetScoreOld(ClientboundSetScorePacket var1);
}

