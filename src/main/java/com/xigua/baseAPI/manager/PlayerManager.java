package com.xigua.baseAPI.manager;

import com.xigua.baseAPI.BaseAPI;
import com.xigua.baseAPI.api.InputMode;
import org.apache.http.concurrent.FutureCallback;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PlayerManager {
    private final BaseAPI plugin;
    private final HashMap<UUID, InputMode> playerInputMode;
    private final HashMap<UUID, Long> playerUidCache;

    public PlayerManager(BaseAPI plugin) {
        this.plugin = plugin;
        this.playerInputMode = new HashMap<>();
        this.playerUidCache = new HashMap<>();
    }

    public void setPlayerInputMode(Player player, InputMode inputMode) {
        playerInputMode.put(player.getUniqueId(), inputMode);
    }

    public InputMode getPlayerInputMode(Player player) {
        return playerInputMode.get(player.getUniqueId());
    }

    public long getCachedUid(Player player) {
        return playerUidCache.getOrDefault(player.getUniqueId(), -1L);
    }

    /**
     * 设置玩家UID缓存
     * @param player 玩家
     * @param uid 网易UID
     */
    public void setCachedUid(Player player, long uid) {
        playerUidCache.put(player.getUniqueId(), uid);
        if (plugin.getConfigManager().getIsDebug()) {
            plugin.getLogger().info("缓存玩家 " + player.getName() + " 的UID: " + uid);
        }
    }

    /**
     * 检查是否有缓存的UID
     * @param player 玩家
     * @return 是否有缓存
     */
    public boolean hasCachedUid(Player player) {
        return playerUidCache.containsKey(player.getUniqueId());
    }

    /**
     * 获取缓存的UID（异步加载，如果没有则请求）
     * @param player 玩家
     * @param callback 回调
     */
    public void getOrFetchUid(Player player, UidCallback callback) {
        UUID uuid = player.getUniqueId();

        // 先检查缓存
        if (playerUidCache.containsKey(uuid)) {
            long cachedUid = playerUidCache.get(uuid);
            if (plugin.getConfigManager().getIsDebug()) {
                plugin.getLogger().info("使用缓存的UID: " + cachedUid + " for " + player.getName());
            }
            callback.onResult(cachedUid);
            return;
        }

        // 缓存不存在，发起HTTP请求
        if (plugin.getConfigManager().getIsDebug()) {
            plugin.getLogger().info("缓存未命中，请求UID: " + player.getName());
        }

        plugin.queryPlayerUid(player, new FutureCallback<>() {
            @Override
            public void completed(java.util.Map<String, Object> result) {
                long uid = (long) result.get("uid");
                setCachedUid(player, uid);
                callback.onResult(uid);
            }

            @Override
            public void failed(Exception ex) {
                plugin.getLogger().warning("获取玩家 " + player.getName() + " UID失败: " + ex.getMessage());
                callback.onError(ex);
            }

            @Override
            public void cancelled() {
                callback.onError(new Exception("获取UID被取消"));
            }
        });
    }

    /**
     * 移除玩家的所有缓存（退出时调用）
     * @param player 玩家
     */
    public void removePlayerCache(Player player) {
        UUID uuid = player.getUniqueId();
        playerUidCache.remove(uuid);
        playerInputMode.remove(uuid);
        if (plugin.getConfigManager().getIsDebug()) {
            plugin.getLogger().info("清除玩家 " + player.getName() + " 的缓存数据");
        }
    }

    // 回调接口
    public interface UidCallback {
        void onResult(long uid);
        default void onError(Exception ex) {}
    }
}
