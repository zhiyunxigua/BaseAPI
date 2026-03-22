package com.xigua.baseAPI.api.events;

import com.xigua.baseAPI.api.InputMode;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerInputModeChangeEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final InputMode oldInputMode;
    private final InputMode newInputMode;

    public PlayerInputModeChangeEvent(Player player, InputMode oldInputMode, InputMode newInputMode) {
        super(player);
        this.oldInputMode = oldInputMode;
        this.newInputMode = newInputMode;
    }

    public InputMode getOldInputMode() {
        return oldInputMode;
    }

    public InputMode getNewInputMode() {
        return newInputMode;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}