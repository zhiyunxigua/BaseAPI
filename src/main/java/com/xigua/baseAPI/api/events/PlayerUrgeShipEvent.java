package com.xigua.baseAPI.api.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerUrgeShipEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public PlayerUrgeShipEvent(Player player) {
        super(player);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
