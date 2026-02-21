package com.xigua.baseAPI.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerBuyItemSuccessEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    public final Player player;

    public PlayerBuyItemSuccessEvent(Player player) {
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
