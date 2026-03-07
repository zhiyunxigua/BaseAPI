/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Floodgate
 */

package com.xigua.baseAPI.pluginmessage.channel;

import com.xigua.baseAPI.BaseAPI;
import com.xigua.baseAPI.api.events.ClientLoadAddonFinishEvent;
import com.xigua.baseAPI.api.events.NeteasePythonEvent;
import com.xigua.baseAPI.api.playerInfo.PlayerInfo;
import com.xigua.baseAPI.api.protocol.packet.PyRpcPacker;
import com.xigua.baseAPI.pluginmessage.PluginMessageChannel;
import com.xigua.baseAPI.util.PluginMessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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

            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().fine("收到网易RPC消息: " + unpacked.getClass().getName());
            }

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

            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().fine(String.format("模组事件: %s:%s:%s from %s",
                        namespace, system, event, player.getName()));
            }

            NeteasePythonEvent bukkitEvent = new NeteasePythonEvent(player, namespace, system, event, data);
            Bukkit.getPluginManager().callEvent(bukkitEvent);

        } catch (ClassCastException ignored) {

        }
    }

    private void handleAddonFinish(Player player) {
        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().fine("玩家附加包加载完成: " + player.getName());
        }
        Bukkit.getPluginManager().callEvent(new ClientLoadAddonFinishEvent(player));
    }

    public void setPlayerInfo(Player player, Map<String, Object> data) {
        UUID uuid = player.getUniqueId();
        PlayerInfo info = new PlayerInfo();
        System.out.println(data.getOrDefault("ProxyUid", 0));
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
