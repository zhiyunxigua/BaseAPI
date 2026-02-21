package com.xigua.baseAPI.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Map;

public class NeteasePythonEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    public final Player player;
    public final String namespace;
    public final String systemName;
    public final String eventName;
    public final Map<String, Object> data;

    public NeteasePythonEvent(Player player, String namespace, String systemName, String eventName, Map<String, Object> data) {
        this.player = player;
        this.namespace = namespace;
        this.systemName = systemName;
        this.eventName = eventName;
        this.data = data;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
