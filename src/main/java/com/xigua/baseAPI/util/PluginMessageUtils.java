package com.xigua.baseAPI.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.UUID;

public class PluginMessageUtils {
    private final JavaPlugin plugin;

    public PluginMessageUtils(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean sendMessage(UUID player, String channel, byte[] data) {
        try {
            Objects.requireNonNull(Bukkit.getPlayer(player)).sendPluginMessage(plugin, channel, data);
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean sendMessage(UUID player, boolean toServer, String channel, byte[] data) {
        if (!toServer) {
            return sendMessage(player, channel, data);
        }
        throw new IllegalStateException(
                "Cannot send plugin message to server on a non-proxy platform");
    }
}
