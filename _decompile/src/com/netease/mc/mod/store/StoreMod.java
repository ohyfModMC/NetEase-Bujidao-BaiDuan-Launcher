/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.common.Common
 *  com.netease.mc.mod.network.message.reply.MessageReply
 *  com.netease.mc.mod.network.socket.NetworkHandler
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.simple.SimpleChannel
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.store;

import com.netease.mc.mod.network.common.Common;
import com.netease.mc.mod.network.message.reply.MessageReply;
import com.netease.mc.mod.network.socket.NetworkHandler;
import com.netease.mc.mod.store.message.StoreNotifyMessage;
import com.netease.mc.mod.store.reply.ReplyStoreNotify;
import java.lang.reflect.Field;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StoreMod {
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation CHANNEL_NAME = new ResourceLocation("storemod:buy");
    public static SimpleChannel INSTANCE;

    public static void init() {
        try {
            Field lockField = NetworkRegistry.class.getDeclaredField("lock");
            lockField.setAccessible(true);
            boolean lock = (Boolean)lockField.get(null);
            lockField.set(null, false);
            INSTANCE = NetworkRegistry.newSimpleChannel((ResourceLocation)CHANNEL_NAME, () -> "1.0", x -> true, x -> true);
            INSTANCE.messageBuilder(StoreNotifyMessage.class, 99, NetworkDirection.PLAY_TO_CLIENT).add();
            lockField.set(null, lock);
        }
        catch (Exception e) {
            Common.CatchException((Throwable)e);
        }
        NetworkHandler.networkHandler.register(263, (MessageReply)new ReplyStoreNotify());
    }
}

