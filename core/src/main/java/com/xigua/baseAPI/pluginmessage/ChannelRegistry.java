package com.xigua.baseAPI.pluginmessage;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.HashMap;
import java.util.Map;

/**
 * 通道注册器 - 统一管理所有插件消息通道
 */
public class ChannelRegistry implements PluginMessageListener {
    private final JavaPlugin plugin;
    private final Map<String, PluginMessageChannel> channels = new HashMap<>();

    public ChannelRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 注册一个通道
     * @param channel 要注册的通道
     */
    public void registerChannel(PluginMessageChannel channel) {
        String identifier = channel.getIdentifier();
        channels.put(identifier, channel);

        Messenger messenger = plugin.getServer().getMessenger();

        // 注册接收通道
        messenger.registerIncomingPluginChannel(plugin, identifier, this);
        messenger.registerOutgoingPluginChannel(plugin, identifier);

    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        PluginMessageChannel handler = channels.get(channel);
        if (handler != null) {
            try {
                PluginMessageChannel.Result result = handler.handleServerCall(
                        message,
                        player.getUniqueId(),
                        player.getName()
                );

                // 如果结果是踢出，则踢出玩家
                if (result != null && !result.isAllowed() && result.getReason() != null) {
                    player.kickPlayer(result.getReason());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}