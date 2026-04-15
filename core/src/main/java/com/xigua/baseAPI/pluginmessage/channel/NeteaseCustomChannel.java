package com.xigua.baseAPI.pluginmessage.channel;

import com.xigua.baseAPI.BaseAPI;
import com.xigua.baseAPI.api.InputMode;
import com.xigua.baseAPI.api.events.ClientLoadAddonFinishEvent;
import com.xigua.baseAPI.api.events.NeteasePythonEvent;
import com.xigua.baseAPI.api.events.PlayerInputModeChangeEvent;
import com.xigua.baseAPI.api.events.UiInitFinished;
import com.xigua.baseAPI.api.playerInfo.PlayerInfo;
import com.xigua.baseAPI.api.protocol.packet.PyRpcPacker;
import com.xigua.baseAPI.pluginmessage.PluginMessageChannel;
import com.xigua.baseAPI.util.PluginMessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class NeteaseCustomChannel implements PluginMessageChannel {
    private final BaseAPI plugin;
    private final PluginMessageUtils pluginMessageUtils;
    private Map<UUID, PlayerInfo> playerInfos;

    @Override
    public String getIdentifier() {
        return "floodgate:netease";
    }

    public NeteaseCustomChannel(BaseAPI plugin, PluginMessageUtils pluginMessageUtils, Map<UUID, PlayerInfo> playerInfos) {
        this.plugin = plugin;
        this.pluginMessageUtils = pluginMessageUtils;
        this.playerInfos = playerInfos;
    }

    @Override
    public Result handleProxyCall(byte[] data, UUID sourceUuid, String sourceUsername,
                                  Identity sourceIdentity) {
        if (sourceIdentity == Identity.SERVER) {
            // send it to the client
            return Result.forward();
        }
        if (sourceIdentity == Identity.PLAYER) {
            return Result.forward();
        }

        return Result.handled();
    }

    @Override
    public Result handleServerCall(byte[] data, UUID playerUuid, String playerName) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) {
            return Result.handled();
        }

        try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data)) {
            Object unpacked = PyRpcPacker.unpackObject(unpacker);

            String method;
            List args;

            if (unpacked instanceof List) {
                List list = (List) unpacked;
                if (list.size() < 2) return Result.handled();

                method = (String) list.get(0);
                args = (List) list.get(1);

            } else if (unpacked instanceof Map) {
                Map map = (Map) unpacked;
                List list = (List) map.get("value");
                if (list == null || list.size() < 2) return Result.handled();

                method = (String) list.get(0);
                args = (List) ((Map) list.get(1)).get("value");

            } else {
                return Result.handled();
            }
            switch (method) {
                case "ModEventC2S":
                    handleModEvent(player, args);
                    break;
                case "ClientLoadAddonsFinishedFromGac":
                    handleAddonFinish(player);
                    break;
                case "SetPlayerInfo":
                    this.setPlayerInfo(player, (Map<String, Object>) args.get(0));
                    break;
                case "PlayerInputMode":
                    this.playerInputMode(player, (Map<String, Object>) args.get(0));
                    break;
                default:
                    break;
            }

        } catch (IOException ignored) {
        }

        return Result.handled();
    }

    private void handleModEvent(Player player, List args) {
        if (args.size() < 4) {
            plugin.getLogger().warning("ModEventC2S 参数数量不足: " + args.size());
            return;
        }

        try {
            String namespace = (String) args.get(0);
            String system = (String) args.get(1);
            String event = (String) args.get(2);
            Map<String, Object> data = (Map<String, Object>) args.get(3);

            if (plugin.getConfigManager().getIsDebug()) {
                plugin.getLogger().info(String.format("模组事件: %s:%s:%s from %s",
                        namespace, system, event, player.getName()));
            }
            Event bukkitEvent;
            if (Objects.equals(namespace, plugin.getConfigManager().getClientNamespace()) && Objects.equals(system, plugin.getConfigManager().getClientSystemName())) {
                if (plugin.getConfigManager().getIsDebug()) {
                    plugin.getLogger().info(String.format("客户端模组，%s 事件", event));
                }
                bukkitEvent = switch (event) {
                    case "UiInitFinished" -> new UiInitFinished(player);
                    default -> new NeteasePythonEvent(player, namespace, system, event, data);
                };
            } else {
                bukkitEvent = new NeteasePythonEvent(player, namespace, system, event, data);
            }
            Bukkit.getPluginManager().callEvent(bukkitEvent);
        } catch (ClassCastException ignored) {
        }
    }

    private void handleAddonFinish(Player player) {
        if (plugin.getConfigManager().getIsDebug()) {
            plugin.getLogger().fine("玩家附加包加载完成: " + player.getName());
        }
        Bukkit.getPluginManager().callEvent(new ClientLoadAddonFinishEvent(player));
    }

    public void setPlayerInfo(Player player, Map<String, Object> data) {
        UUID uuid = player.getUniqueId();
        if (plugin.getConfigManager().getIsDebug()) {
            plugin.getLogger().info("SetPlayerInfo " + data);
        }
        PlayerInfo info = new PlayerInfo();
        info.setGameId((String) data.getOrDefault("GameId", "0"));
        info.setGameKey((String) data.getOrDefault("GameKey", ""));
        info.setTestServer((boolean) data.getOrDefault("TestServer", true));
        info.setShopServerUrl((String) data.getOrDefault("ShopServerUrl", ""));
        info.setWebServerUrl((String) data.getOrDefault("WebServerUrl", ""));
        info.setProxyUid(((Number) data.getOrDefault("ProxyUid", 0)).longValue());
        Object uuidObj = data.getOrDefault("Uuid", uuid);
        if (uuidObj instanceof String) {
            try {
                info.setUuid(UUID.fromString((String) uuidObj));
            } catch (IllegalArgumentException ignored) {
            }
        }
        this.playerInfos.put(uuid, info);
    }

    public void playerInputMode(Player player, Map<String, Object> data) {
        UUID uuid = player.getUniqueId();

        // 获取输入模式
        Object inputModeObj = data.get("input_mode");
        InputMode inputMode = null;

        if (inputModeObj instanceof String) {
            try {
                inputMode = InputMode.valueOf((String) inputModeObj);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown input mode: " + inputModeObj);
            }
        }

        // 更新玩家信息
        PlayerInfo playerInfo = playerInfos.get(uuid);
        if (playerInfo == null) {
            playerInfo = new PlayerInfo();
            playerInfos.put(uuid, playerInfo);
        }

        InputMode oldInputMode = playerInfo.getInputMode();
        playerInfo.setInputMode(inputMode);

        PlayerInputModeChangeEvent event = new PlayerInputModeChangeEvent(player, oldInputMode, inputMode);
        Bukkit.getPluginManager().callEvent(event);
    }

    public boolean sendPacket(UUID player, byte[] packet) {
        return pluginMessageUtils.sendMessage(player, getIdentifier(), packet);
    }

    public boolean sendModEvent(UUID player, String namespace, String system, String event, Map<String, Object> data) {
        try {
            byte[] packet = PyRpcPacker.pack(namespace, system, event, data);
            return sendPacket(player, packet);
        } catch (IOException e) {
            return false;
        }
    }
}
