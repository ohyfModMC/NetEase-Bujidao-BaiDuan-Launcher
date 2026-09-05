/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.suggestion.Suggestion
 *  com.netease.mc.mod.network.common.Common
 *  javax.annotation.Nullable
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.CommandSuggestions
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractCommandBlockEditScreen
 *  net.minecraft.client.gui.screens.inventory.BookEditScreen
 *  net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen
 *  net.minecraft.client.renderer.debug.BrainDebugRenderer$BrainDump
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Component$Serializer
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.protocol.game.ClientboundBossEventPacket
 *  net.minecraft.network.protocol.game.ClientboundBossEventPacket$AddOperation
 *  net.minecraft.network.protocol.game.ClientboundBossEventPacket$UpdateNameOperation
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
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.animal.horse.AbstractHorse
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.decoration.ItemFrame
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.vehicle.AbstractMinecartContainer
 *  net.minecraft.world.inventory.LoomMenu
 *  net.minecraft.world.inventory.PlayerEnderChestContainer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BaseCommandBlock
 *  net.minecraft.world.level.block.entity.BaseContainerBlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.CommandBlockEntity
 *  net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity
 *  net.minecraft.world.level.block.entity.JigsawBlockEntity
 *  net.minecraft.world.level.block.entity.JukeboxBlockEntity
 *  net.minecraft.world.level.block.entity.LecternBlockEntity
 *  net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
 *  net.minecraft.world.level.block.entity.SignBlockEntity
 *  net.minecraft.world.level.block.entity.SignText
 *  net.minecraft.world.level.block.entity.StructureBlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.saveddata.maps.MapDecoration
 *  net.minecraft.world.scores.Objective
 *  net.minecraft.world.scores.PlayerTeam
 *  net.minecraft.world.scores.Score
 *  net.minecraft.world.scores.ScoreboardSaveData
 *  net.minecraft.world.scores.criteria.ObjectiveCriteria
 *  net.minecraft.world.scores.criteria.ObjectiveCriteria$RenderType
 *  org.apache.logging.log4j.LogManager
 */
package com.netease.mc.mod.filter;

import com.mojang.brigadier.suggestion.Suggestion;
import com.netease.mc.mod.filter.BannerFilter;
import com.netease.mc.mod.filter.ChatBanHelper;
import com.netease.mc.mod.filter.FilterHelper;
import com.netease.mc.mod.filter.FilterWhiteListHelper;
import com.netease.mc.mod.filter.Helper;
import com.netease.mc.mod.filter.ItemBanHelper;
import com.netease.mc.mod.filter.NbtTagVisitor;
import com.netease.mc.mod.filter.old.ILoomMenuOld;
import com.netease.mc.mod.filter.old.IObjectiveOld;
import com.netease.mc.mod.filter.old.IPlayerTeamOld;
import com.netease.mc.mod.filter.old.IScoreOld;
import com.netease.mc.mod.filter.old.IScoreboardOld;
import com.netease.mc.mod.network.common.Common;
import com.netease.mc.mod.oldInterface.IAbstractCommandBlockEditScreenOld;
import com.netease.mc.mod.oldInterface.IBaseCommandBlockOld;
import com.netease.mc.mod.oldInterface.IBlockEntityOld;
import com.netease.mc.mod.oldInterface.IBrainDebugRendererOld;
import com.netease.mc.mod.oldInterface.IChatScreenOld;
import com.netease.mc.mod.oldInterface.IClientPacketListenerOld;
import com.netease.mc.mod.oldInterface.ICommandSuggestionsOld;
import com.netease.mc.mod.oldInterface.IEntityOld;
import com.netease.mc.mod.oldInterface.IGameProfileOld;
import com.netease.mc.mod.oldInterface.IItemStackOld;
import com.netease.mc.mod.oldInterface.IPlayerOld;
import com.netease.mc.mod.oldInterface.IRandomizableContainerBlockEntityOld;
import com.netease.mc.mod.oldInterface.IScoreboardSaveDataOld;
import com.netease.mc.mod.oldInterface.IStructureBlockEditScreenOld;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractCommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.ScoreboardSaveData;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.apache.logging.log4j.LogManager;

public class FilterWrapper {
    private static ScheduledExecutorService schduler = Executors.newScheduledThreadPool(10);
    static volatile boolean ThreadRunning = false;
    private static ConcurrentLinkedQueue<BlockEntity> needHandleBlockEntities = new ConcurrentLinkedQueue();
    private static ConcurrentLinkedQueue<BlockEntity> handlingBlockEntities = new ConcurrentLinkedQueue();
    public static HashMap<String, String> cacheNames = new HashMap();

    public static void startReadFromNbt() {
        schduler.schedule(new Runnable(){

            @Override
            public void run() {
                ThreadRunning = true;
                try {
                    while (!needHandleBlockEntities.isEmpty()) {
                        while (!needHandleBlockEntities.isEmpty()) {
                            handlingBlockEntities.add(needHandleBlockEntities.poll());
                        }
                        Thread.sleep(10L);
                        while (!handlingBlockEntities.isEmpty()) {
                            FilterWrapper.filterTileEntity(handlingBlockEntities.poll(), false);
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

    public static void updateLocalCopyWrapper(BookEditScreen book, boolean publish) {
        ListTag bookPages = new ListTag();
        book.f_98070_.stream().map(StringTag::m_129297_).forEach(arg_0 -> bookPages.add(arg_0));
        if (!book.f_98070_.isEmpty()) {
            for (int pageIdx = 0; pageIdx < bookPages.size(); ++pageIdx) {
                String s = bookPages.m_128778_(pageIdx);
                s = FilterWhiteListHelper.filter(s, true, 3);
                StringTag tag = StringTag.m_129297_((String)s);
                bookPages.set(pageIdx, (Tag)tag);
                book.f_98070_.set(pageIdx, s);
            }
            book.f_98065_.m_41700_("pages", (Tag)bookPages);
        }
        if (publish) {
            book.f_98065_.m_41700_("author", (Tag)StringTag.m_129297_((String)book.f_98064_.m_36316_().getName()));
            String title = FilterWhiteListHelper.filter(book.f_98071_.trim(), true, 3, true);
            book.f_98065_.m_41700_("title", (Tag)StringTag.m_129297_((String)title));
            book.f_98071_ = title;
        }
    }

    public static boolean handleChatInputWrapper(IChatScreenOld gui, String s, boolean b) {
        if (ChatBanHelper.InChatBan()) {
            Helper.printAtChannelChat(ChatBanHelper.getBanMessage());
            return false;
        }
        s = FilterWhiteListHelper.filter(s, true, 0);
        return gui.handleChatInputOld(s, b);
    }

    @Nullable
    public static BlockEntity loadStaticWrapper(BlockPos p_155242_, BlockState p_155243_, CompoundTag p_155244_) {
        String s = p_155244_.m_128461_("id");
        ResourceLocation resourcelocation = ResourceLocation.m_135820_((String)s);
        if (resourcelocation == null) {
            Common.Log((String)("Block entity has invalid type:" + s));
            return null;
        }
        return BuiltInRegistries.f_257049_.m_6612_(resourcelocation).map(p_155240_ -> {
            try {
                return p_155240_.m_155264_(p_155242_, p_155243_);
            }
            catch (Throwable var5) {
                return null;
            }
        }).map(p_155249_ -> {
            try {
                p_155249_.m_142466_(p_155244_);
                FilterWrapper.filterTileEntity(p_155249_);
                return p_155249_;
            }
            catch (Throwable var4) {
                return null;
            }
        }).orElseGet(() -> null);
    }

    public static void filterTileEntity(BlockEntity entity) {
        FilterWrapper.filterTileEntity(entity, true);
    }

    public static void filterTileEntity(BlockEntity entity, boolean enableFilterLog) {
        EnchantmentTableBlockEntity table;
        int logType;
        if (entity instanceof SignBlockEntity) {
            SignText[] texts;
            for (SignText signText : texts = new SignText[]{((SignBlockEntity)entity).m_277142_(), ((SignBlockEntity)entity).m_277159_()}) {
                for (Component input : signText.f_276632_) {
                    int logType2 = enableFilterLog ? 1 : 0;
                    Component line = FilterHelper.filterChatComponentText(input, false, logType2);
                    if (!line.getString().isEmpty()) {
                        signText.f_276632_[i] = line;
                    }
                    signText.f_276467_ = null;
                }
            }
        }
        if (entity instanceof CommandBlockEntity) {
            CommandBlockEntity commandBlock = (CommandBlockEntity)entity;
            String command = commandBlock.m_59141_().m_45438_();
            logType = enableFilterLog ? 5 : 0;
            command = FilterWhiteListHelper.filter(command, false, logType);
            commandBlock.m_59141_().m_6590_(command);
        }
        if (entity instanceof StructureBlockEntity) {
            StructureBlockEntity structure = (StructureBlockEntity)entity;
            String name = structure.m_59895_();
            logType = enableFilterLog ? 6 : 0;
            name = FilterWhiteListHelper.filter(name, false, logType);
            structure.m_59868_(name);
            String data = structure.m_59907_();
            data = FilterWhiteListHelper.filter(data, false, logType);
            structure.m_59887_(data);
        }
        if (entity instanceof BaseContainerBlockEntity) {
            BaseContainerBlockEntity chest = (BaseContainerBlockEntity)entity;
            try {
                int logType3;
                int n = logType3 = enableFilterLog ? 2 : 0;
                if (chest.m_8077_()) {
                    Component name = FilterHelper.filterChatComponentText(chest.m_7755_(), false, logType3);
                    chest.m_58638_(name);
                }
                for (int i = 0; i < chest.m_6643_(); ++i) {
                    ItemStack itemStack = chest.m_8020_(i);
                    FilterHelper.filterItemStack(itemStack, enableFilterLog);
                }
            }
            catch (Throwable e) {
                FilterWrapper.CatchException(e);
            }
        }
        if (entity instanceof JukeboxBlockEntity) {
            JukeboxBlockEntity jukebox = (JukeboxBlockEntity)entity;
            ItemStack itemStack = jukebox.m_8020_(0);
            FilterHelper.filterItemStack(itemStack, enableFilterLog);
        }
        if (entity instanceof EnchantmentTableBlockEntity && (table = (EnchantmentTableBlockEntity)entity).m_8077_()) {
            int logType4 = enableFilterLog ? 2 : 0;
            Component name = FilterHelper.filterChatComponentText(table.m_7755_(), false, logType4);
            table.m_59272_(name);
        }
        if (entity instanceof LecternBlockEntity) {
            table = (LecternBlockEntity)entity;
            ItemStack itemStack = table.m_59566_();
            FilterHelper.filterItemStack(itemStack, enableFilterLog);
        }
        if (entity instanceof JigsawBlockEntity) {
            Helper.debugLog("JigsawBlockEntity!!!!!!!!!");
            int logType5 = enableFilterLog ? 6 : 0;
            JigsawBlockEntity jigsaw = (JigsawBlockEntity)entity;
            String name = jigsaw.m_59442_().toString();
            String target = jigsaw.m_59443_().toString();
            String pool = jigsaw.m_222765_().toString();
            String finalState = jigsaw.m_59445_();
            String newName = FilterWhiteListHelper.filter(name, false, logType5);
            String newTarget = FilterWhiteListHelper.filter(target, false, logType5);
            String newPool = FilterWhiteListHelper.filter(pool, false, logType5);
            String newFinalState = FilterWhiteListHelper.filter(finalState, false, logType5);
            if (!name.equals(newName)) {
                jigsaw.m_59435_(new ResourceLocation("empty"));
            }
            if (!target.equals(newTarget)) {
                jigsaw.m_59438_(new ResourceLocation("empty"));
            }
            if (!pool.equals(newPool)) {
                jigsaw.m_222763_(ResourceKey.m_135785_((ResourceKey)Registries.f_256948_, (ResourceLocation)new ResourceLocation("empty")));
            }
            if (!finalState.equals(newFinalState)) {
                jigsaw.m_59431_("minecraft:air");
            }
        }
    }

    public static void loadWrapper(IEntityOld entityOld, CompoundTag tag) {
        entityOld.loadOld(tag);
        Entity entity = (Entity)entityOld;
        FilterWrapper.filterEntity(entity);
    }

    public static void filterEntity(Entity entity) {
        Component name = FilterHelper.filterChatComponentText(entity.m_7755_(), false, 0);
        entity.m_6593_(name);
        if (entity instanceof AbstractMinecartContainer) {
            AbstractMinecartContainer chest = (AbstractMinecartContainer)entity;
            for (int i = 0; i < chest.m_6643_(); ++i) {
                ItemStack itemStack = chest.m_8020_(i);
                FilterHelper.filterItemStack(itemStack);
            }
        }
        if (entity instanceof AbstractHorse) {
            AbstractHorse chestHorse = (AbstractHorse)entity;
            SimpleContainer horseChest = chestHorse.f_30520_;
            for (int i = 0; i < horseChest.m_6643_(); ++i) {
                ItemStack itemStack = horseChest.m_8020_(i);
                FilterHelper.filterItemStack(itemStack);
            }
        }
        if (entity instanceof ItemFrame) {
            ItemFrame itemFrame = (ItemFrame)entity;
            ItemStack itemStack = itemFrame.m_31822_();
            FilterHelper.filterItemStack(itemStack);
        }
        if (entity instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand)entity;
            for (ItemStack itemStack : armorStand.m_6167_()) {
                FilterHelper.filterItemStack(itemStack);
            }
            for (ItemStack itemStack : armorStand.m_6168_()) {
                FilterHelper.filterItemStack(itemStack);
            }
        }
        if (entity instanceof ItemEntity) {
            ItemEntity item = (ItemEntity)entity;
            FilterHelper.filterItemStack(item.m_32055_());
        }
    }

    public static void loadWrapper(IBlockEntityOld blockEntityOld, CompoundTag tag) {
        blockEntityOld.loadOld(tag);
        BlockEntity entity = (BlockEntity)blockEntityOld;
        needHandleBlockEntities.add(entity);
        if (!ThreadRunning) {
            FilterWrapper.startReadFromNbt();
        }
    }

    public static void loadWrapper(IBaseCommandBlockOld old, CompoundTag tag) {
        old.loadOld(tag);
        BaseCommandBlock entity = (BaseCommandBlock)old;
        String oldCommand = entity.m_45438_();
        String newCommand = FilterWhiteListHelper.filter(oldCommand, false, 0);
        if (!newCommand.equals(oldCommand)) {
            entity.m_6590_(newCommand);
        }
    }

    public static Component getLastOutputWrapper(IBaseCommandBlockOld old) {
        Component component = old.getLastOutputOld();
        return FilterHelper.filterChatComponentText(component, false, 0);
    }

    public static void readAdditionalSaveDataWrapper(IPlayerOld iplayer, CompoundTag compound) {
        iplayer.readAdditionalSaveDataOld(compound);
        Player player = (Player)iplayer;
        for (ItemStack itemStack : player.f_36093_.f_35975_) {
            FilterHelper.filterItemStack(itemStack);
        }
        for (ItemStack itemStack : player.f_36093_.f_35974_) {
            FilterHelper.filterItemStack(itemStack);
        }
        for (ItemStack itemStack : player.f_36093_.f_35976_) {
            FilterHelper.filterItemStack(itemStack);
        }
        PlayerEnderChestContainer enderChest = player.m_36327_();
        for (int i = 0; i < enderChest.m_6643_(); ++i) {
            ItemStack itemStack = enderChest.m_8020_(i);
            FilterHelper.filterItemStack(itemStack);
        }
    }

    public static ScoreboardSaveData loadWrapper(IScoreboardSaveDataOld saveDataOld, CompoundTag compound) {
        String name;
        String displayName;
        CompoundTag nbttagcompound;
        ListTag nbt;
        CompoundTag displaySlots = compound.m_128469_("DisplaySlots");
        if (displaySlots != null) {
            for (int i = 0; i < displaySlots.m_128440_(); ++i) {
                String displayName2 = displaySlots.m_128461_("slot_" + i);
                if (displayName2.isEmpty()) continue;
                displayName2 = FilterWhiteListHelper.filter(displayName2, false, 7, true);
                displaySlots.m_128359_("slot_" + i, displayName2);
            }
            displaySlots.m_128365_("DisplaySlots", (Tag)displaySlots);
        }
        if ((nbt = compound.m_128437_("Objectives", 10)) != null) {
            for (int i = 0; i < nbt.size(); ++i) {
                nbttagcompound = nbt.m_128728_(i);
                displayName = nbttagcompound.m_128461_("DisplayName");
                name = nbttagcompound.m_128461_("Name");
                displayName = FilterWhiteListHelper.filter(displayName, false, 7, true);
                name = FilterWhiteListHelper.filter(name, false, 7, true);
                nbttagcompound.m_128359_("DisplayName", displayName);
                nbttagcompound.m_128359_("Name", name);
                nbt.set(i, (Tag)nbttagcompound);
            }
            compound.m_128365_("Objectives", (Tag)nbt);
        }
        if ((nbt = compound.m_128437_("Teams", 10)) != null) {
            for (int i = 0; i < nbt.size(); ++i) {
                nbttagcompound = nbt.m_128728_(i);
                displayName = nbttagcompound.m_128461_("DisplayName");
                name = nbttagcompound.m_128461_("Name");
                String memberNamePrefix = nbttagcompound.m_128461_("MemberNamePrefix");
                String memberNameSuffix = nbttagcompound.m_128461_("MemberNameSuffix");
                ListTag players = nbttagcompound.m_128437_("Players", 8);
                if (players != null) {
                    for (int j = 0; j < players.size(); ++j) {
                        String playerName = players.m_128778_(j);
                        playerName = FilterWhiteListHelper.filter(playerName, false, 7, true);
                        players.set(j, (Tag)StringTag.m_129297_((String)playerName));
                    }
                    nbttagcompound.m_128365_("Players", (Tag)players);
                }
                displayName = FilterWhiteListHelper.filter(displayName, false, 7, true);
                name = FilterWhiteListHelper.filter(name, false, 7, true);
                memberNamePrefix = FilterWhiteListHelper.filter(memberNamePrefix, false, 7, true);
                memberNameSuffix = FilterWhiteListHelper.filter(memberNameSuffix, false, 7, true);
                nbttagcompound.m_128359_("DisplayName", displayName);
                nbttagcompound.m_128359_("Name", name);
                nbttagcompound.m_128359_("MemberNamePrefix", memberNamePrefix);
                nbttagcompound.m_128359_("MemberNameSuffix", memberNameSuffix);
                nbt.set(i, (Tag)nbttagcompound);
            }
            compound.m_128365_("Teams", (Tag)nbt);
        }
        if ((nbt = compound.m_128437_("PlayerScores", 10)) != null) {
            for (int i = 0; i < nbt.size(); ++i) {
                nbttagcompound = nbt.m_128728_(i);
                displayName = nbttagcompound.m_128461_("DisplayName");
                name = nbttagcompound.m_128461_("Name");
                String objective = nbttagcompound.m_128461_("Objective");
                displayName = FilterWhiteListHelper.filter(displayName, false, 7, true);
                name = FilterWhiteListHelper.filter(name, false, 7, true);
                objective = FilterWhiteListHelper.filter(objective, false, 7, true);
                nbttagcompound.m_128359_("DisplayName", displayName);
                nbttagcompound.m_128359_("Name", name);
                nbttagcompound.m_128359_("Objective", objective);
                nbt.set(i, (Tag)nbttagcompound);
            }
            compound.m_128365_("PlayerScores", (Tag)nbt);
        }
        ((ScoreboardSaveData)saveDataOld).m_77760_(true);
        return saveDataOld.loadOld(compound);
    }

    public static Objective addObjectiveWrapper(IScoreboardOld old, String name, ObjectiveCriteria criteria, Component displayName, ObjectiveCriteria.RenderType renderType) {
        Helper.debugLog("addObjectiveWrapper name: " + name + " displayName: " + displayName.getString() + " criteria:" + ObjectiveCriteria.m_166115_().toString());
        String filterName = FilterWhiteListHelper.filter(name, false, 0, true);
        Component filterDisplayName = FilterHelper.filterChatComponentText(displayName, false, 0, true);
        Helper.debugLog("addObjectiveWrapper filterName: " + filterName + " filterDisplayName: " + filterDisplayName.getString());
        return old.addObjectiveOld(filterName, criteria, filterDisplayName, renderType);
    }

    public static PlayerTeam addPlayerTeamWrapper(IScoreboardOld old, String name) {
        String filterName = FilterWhiteListHelper.filter(name, false, 7, true);
        return old.addPlayerTeamOld(filterName);
    }

    public static boolean addPlayerToTeamWrapper(IScoreboardOld old, String player, PlayerTeam team) {
        String filterName = FilterWhiteListHelper.filter(player, false, 7, true);
        return old.addPlayerToTeamOld(filterName, team);
    }

    public static void setDisplayNameWrapper(IObjectiveOld old, Component name) {
        Component filterName = FilterHelper.filterChatComponentText(name, false, 7, true);
        old.setDisplayNameOld(filterName);
    }

    public static void setScoreWrapper(IScoreOld old, int score) {
        old.setScoreOld(score);
    }

    public static Score getOrCreatePlayerScoreWrapper(IScoreboardOld old, String name, Objective p_83473_) {
        Helper.debugLog("m_83485_Wrapper!");
        if (cacheNames.containsKey(name)) {
            return old.getOrCreatePlayerScoreOld(cacheNames.get(name), p_83473_);
        }
        String newName = FilterWhiteListHelper.filter(name, false, 0, true);
        cacheNames.put(name, newName);
        return old.getOrCreatePlayerScoreOld(newName, p_83473_);
    }

    public static void setDisplayNameWrapper(IPlayerTeamOld old, Component name) {
        Component filterName = FilterHelper.filterChatComponentText(name, false, 0, true);
        old.setDisplayNameOld(filterName);
    }

    public static void setPlayerPrefixWrapper(IPlayerTeamOld old, Component name) {
        Component filterName = FilterHelper.filterChatComponentText(name, false, 0, true);
        old.setPlayerPrefixOld(filterName);
    }

    public static void setPlayerSuffixWrapper(IPlayerTeamOld old, Component name) {
        Component filterName = FilterHelper.filterChatComponentText(name, false, 0, true);
        old.setPlayerSuffixOld(filterName);
    }

    public static void onDoneWrapper(IAbstractCommandBlockEditScreenOld commandBlockOld) {
        AbstractCommandBlockEditScreen commandBlock = (AbstractCommandBlockEditScreen)commandBlockOld;
        LogManager.getLogger().info("onDoneWrapper");
        if (ItemBanHelper.InItemBan("minecraft:command_block")) {
            Helper.printAtChannelChat(ItemBanHelper.getBanMessage("minecraft:command_block"));
            Minecraft.m_91087_().m_91152_((Screen)null);
            return;
        }
        LogManager.getLogger().info("func_1952341_kWrapper");
        EditBox commandTextField = commandBlock.f_97646_;
        String text = commandTextField.m_94155_();
        if (!FilterHelper.isWhiteListCmd(text)) {
            text = FilterWhiteListHelper.filter(text, true, 5);
            commandTextField.m_94144_(text);
        }
        commandBlockOld.onDoneOld();
    }

    public static ScheduledExecutorService getSchduler() {
        return schduler;
    }

    public static void onDoneWrapper(IStructureBlockEditScreenOld editStructureOld) {
        StructureBlockEditScreen editStructure = (StructureBlockEditScreen)editStructureOld;
        EditBox nameEdit = editStructure.f_99357_;
        String text = nameEdit.m_94155_();
        text = FilterWhiteListHelper.filter(text, true, 6);
        nameEdit.m_94144_(text);
        EditBox dataEdit = editStructure.f_99366_;
        String data = dataEdit.m_94155_();
        data = FilterWhiteListHelper.filter(data, true, 6);
        dataEdit.m_94144_(data);
        editStructureOld.onDoneOld();
    }

    public static void unpackLootTableWrapper(IRandomizableContainerBlockEntityOld old, Player playerEntity) {
        RandomizableContainerBlockEntity entity = (RandomizableContainerBlockEntity)old;
        if (entity.m_58904_() == null) {
            return;
        }
        old.unpackLootTableOld(playerEntity);
    }

    public static String getNameWrapper(IGameProfileOld profile) {
        String name = profile.getNameOld();
        if (cacheNames.containsKey(name)) {
            return cacheNames.get(name);
        }
        String newName = FilterWhiteListHelper.filter(name, false, 0, true);
        cacheNames.put(name, newName);
        return newName;
    }

    public static void onSyncedDataUpdatedWrapper(IEntityOld old, EntityDataAccessor p_20059_) {
        Component oldName;
        old.onSyncedDataUpdatedOld(p_20059_);
        Entity entity = (Entity)old;
        if (p_20059_.equals((Object)Entity.f_19833_) && (oldName = entity.m_7770_()) != null) {
            Component newName = FilterHelper.filterChatComponentText(entity.m_7755_(), false, 0);
            Helper.debugLog("onSyncedDataUpdatedWrapper:" + oldName.getString() + ":" + newName.getString());
            if (!oldName.getString().equals(newName.getString())) {
                entity.m_6593_(newName);
            }
        }
    }

    public static void setTagWrapper(IItemStackOld old, CompoundTag tag) {
        try {
            if (tag != null) {
                CompoundTag nbttagcompound;
                MutableComponent component;
                CompoundTag display;
                if (tag.m_128425_("display", 10) && (display = tag.m_128469_("display")) != null && display.m_128425_("Name", 8) && (component = Component.Serializer.m_130701_((String)display.m_128461_("Name"))) != null) {
                    Component newcomponent = FilterHelper.filterChatComponentText((Component)component, false, 0);
                    if (!component.getString().equals(newcomponent.getString())) {
                        display.m_128359_("Name", Component.Serializer.m_130703_((Component)newcomponent));
                    }
                }
                if (tag.m_128425_("title", 8)) {
                    String title = tag.m_128461_("title");
                    title = FilterWhiteListHelper.filter(title, false, 0, false);
                    tag.m_128359_("title", title);
                }
                if (tag.m_128425_("author", 8)) {
                    String author = tag.m_128461_("author");
                    author = FilterWhiteListHelper.filter(author, false, 0);
                    tag.m_128359_("author", author);
                }
                if (tag.m_128425_("BlockEntityTag", 10) && (nbttagcompound = tag.m_128469_("BlockEntityTag")) != null) {
                    ListTag itemtaglist = nbttagcompound.m_128437_("Items", 10);
                    if (itemtaglist != null) {
                        for (int i = 0; i < itemtaglist.size(); ++i) {
                            CompoundTag compound = itemtaglist.m_128728_(i);
                            ItemStack stack = new ItemStack(compound);
                            FilterHelper.filterItemStack(stack, false);
                            stack.m_41739_(compound);
                            itemtaglist.set(i, (Tag)compound);
                        }
                        nbttagcompound.m_128365_("Items", (Tag)itemtaglist);
                    }
                    tag.m_128365_("BlockEntityTag", (Tag)nbttagcompound);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        old.setTagOld(tag);
    }

    public static void handleOpenScreenWrapper(IClientPacketListenerOld old, ClientboundOpenScreenPacket packet) {
        Component newTitle;
        Component title = packet.m_132629_();
        packet.f_132613_ = newTitle = FilterHelper.filterChatComponentText(title, false, 0);
        old.handleOpenScreenOld(packet);
    }

    public static void handleDisconnectWrapper(IClientPacketListenerOld old, ClientboundDisconnectPacket packet) {
        Component newReason;
        Component reason = packet.m_132085_();
        packet.f_132075_ = newReason = FilterHelper.filterChatComponentText(reason, false, 0);
        old.handleDisconnectOld(packet);
    }

    public static void handleMapItemDataWrapper(IClientPacketListenerOld old, ClientboundMapItemDataPacket packet) {
        if (packet.f_132419_ == null) {
            old.handleMapItemDataOld(packet);
            return;
        }
        for (int i = 0; i < packet.f_132419_.size(); ++i) {
            Component newMapTempName;
            MapDecoration mapTemp = (MapDecoration)packet.f_132419_.get(i);
            Component mapTempName = mapTemp.m_77810_();
            if (mapTempName == null) continue;
            mapTemp.f_77795_ = newMapTempName = FilterHelper.filterChatComponentText(mapTempName, false, 0);
        }
        old.handleMapItemDataOld(packet);
    }

    public static void handleTagQueryPacketWrapper(IClientPacketListenerOld old, ClientboundTagQueryPacket packet) {
        CompoundTag tag = packet.m_133509_();
        if (tag != null) {
            NbtTagVisitor visitor = new NbtTagVisitor();
            visitor.createFilterVisitor().visit((Tag)tag);
        }
        old.handleTagQueryPacketOld(packet);
    }

    public static void handlePlayerCombatKillWrapper(IClientPacketListenerOld old, ClientboundPlayerCombatKillPacket packet) {
        Component newMessage;
        Component message = packet.m_179079_();
        packet.f_179060_ = newMessage = FilterHelper.filterChatComponentText(message, false, 0);
        old.handlePlayerCombatKillOld(packet);
    }

    public static void setActionBarTextWrapper(IClientPacketListenerOld old, ClientboundSetActionBarTextPacket packet) {
        Component newText;
        Component text = packet.m_179210_();
        packet.f_179199_ = newText = FilterHelper.filterChatComponentText(text, false, 0);
        old.setActionBarTextOld(packet);
    }

    public static void setTitleTextWrapper(IClientPacketListenerOld old, ClientboundSetTitleTextPacket packet) {
        Component newText;
        Component text = packet.m_179399_();
        packet.f_179388_ = newText = FilterHelper.filterChatComponentText(text, false, 0);
        old.setTitleTextOld(packet);
    }

    public static void setSubtitleTextWrapper(IClientPacketListenerOld old, ClientboundSetSubtitleTextPacket packet) {
        Component newText;
        Component text = packet.m_179385_();
        packet.f_179374_ = newText = FilterHelper.filterChatComponentText(text, false, 0);
        old.setSubtitleTextOld(packet);
    }

    public static void handleBossUpdateWrapper(IClientPacketListenerOld old, ClientboundBossEventPacket packet) {
        if (packet.f_131751_ instanceof ClientboundBossEventPacket.AddOperation) {
            Component newName;
            Component name = ((ClientboundBossEventPacket.AddOperation)packet.f_131751_).f_178664_;
            ((ClientboundBossEventPacket.AddOperation)packet.f_131751_).f_178664_ = newName = FilterHelper.filterChatComponentText(name, false, 0);
        } else if (packet.f_131751_ instanceof ClientboundBossEventPacket.UpdateNameOperation) {
            Component newName;
            Component name = ((ClientboundBossEventPacket.UpdateNameOperation)packet.f_131751_).f_178723_;
            ((ClientboundBossEventPacket.UpdateNameOperation)packet.f_131751_).f_178723_ = newName = FilterHelper.filterChatComponentText(name, false, 0);
        }
        old.handleBossUpdateOld(packet);
    }

    public static void addOrUpdateBrainDumpWrapper(IBrainDebugRendererOld old, BrainDebugRenderer.BrainDump p_113220_) {
        String oldString = p_113220_.f_113295_;
        String newString = FilterWhiteListHelper.filter(oldString, false, 0);
        old.addOrUpdateBrainDumpOld(p_113220_);
    }

    public static void showSuggestionsWrapper(ICommandSuggestionsOld old, boolean p_93931_) {
        old.showSuggestionsOld(p_93931_);
        if (old instanceof CommandSuggestions) {
            CommandSuggestions commandSuggestions = (CommandSuggestions)old;
            if (null == commandSuggestions.f_93866_) {
                return;
            }
            List suggestionList = commandSuggestions.f_93866_.f_93949_;
            HashMap<Integer, Suggestion> filterMap = new HashMap<Integer, Suggestion>();
            for (int i = 0; i < suggestionList.size(); ++i) {
                String newText;
                Suggestion suggestion = (Suggestion)suggestionList.get(i);
                String text = suggestion.getText();
                if (text.equals(newText = FilterWhiteListHelper.filter(text, false, 0))) continue;
                filterMap.put(i, new Suggestion(suggestion.getRange(), newText, suggestion.getTooltip()));
            }
            for (Map.Entry kv : filterMap.entrySet()) {
                suggestionList.set((Integer)kv.getKey(), (Suggestion)kv.getValue());
            }
        }
    }

    public static void CatchException(Throwable e) {
        StringWriter result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        LogManager.getLogger().info(((Object)result).toString());
    }

    public static void removePlayerTeamWrapper(IScoreboardOld old, PlayerTeam team) {
        try {
            old.removePlayerTeamOld(team);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removePlayerFromTeamWrapper(IScoreboardOld old, String p1, PlayerTeam p2) {
        try {
            old.removePlayerFromTeamOld(p1, p2);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleSetPlayerTeamPacketWrapper(IClientPacketListenerOld old, ClientboundSetPlayerTeamPacket packet) {
        try {
            old.handleSetPlayerTeamPacketOld(packet);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleSetScoreWrapper(IClientPacketListenerOld old, ClientboundSetScorePacket packet) {
        try {
            old.handleSetScoreOld(packet);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setupResultSlotWrapper(ILoomMenuOld old, Holder p_219992_) {
        old.setupResultSlotOld(p_219992_);
        LoomMenu menu = (LoomMenu)old;
        ItemStack itemstack = menu.m_39897_().m_7993_();
        BannerFilter.checkBanner(itemstack);
    }
}

