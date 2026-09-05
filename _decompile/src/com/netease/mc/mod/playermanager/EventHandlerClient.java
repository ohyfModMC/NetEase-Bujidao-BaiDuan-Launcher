/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.netease.mc.mod.network.message.request.MessageRequest
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package com.netease.mc.mod.playermanager;

import com.netease.mc.mod.network.message.request.MessageRequest;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandlerClient {
    private static int ClientConnectedToServerCmd = 1042;

    @SubscribeEvent
    public void OnPlayerLoginedEvent(PlayerEvent.PlayerLoggedInEvent event) {
        MessageRequest mrq = new MessageRequest();
        mrq.send(ClientConnectedToServerCmd, new Object[0]);
    }
}

