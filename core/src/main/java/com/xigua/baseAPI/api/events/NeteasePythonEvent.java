package com.xigua.baseAPI.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.util.Map;

public class NeteasePythonEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    protected final String namespace;
    protected final String systemName;
    protected final String pyEventName;
    protected final Map<String, Object> data;

    public NeteasePythonEvent(Player player, String namespace, String systemName, String pyEventName, Map<String, Object> data) {
        super(player);
        this.namespace = namespace;
        this.systemName = systemName;
        this.pyEventName = pyEventName;
        this.data = data;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getSystemName() {
        return this.systemName;
    }

    public String getPyEventName() {
        return this.pyEventName;
    }

    public Map<String, Object> getData() {
        return this.data;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
