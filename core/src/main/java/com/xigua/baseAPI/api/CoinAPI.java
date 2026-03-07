package com.xigua.baseAPI.api;

import com.xigua.baseAPI.BaseAPI;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 硬币系统API - 其他插件通过这个类调用硬币功能
 */
public class CoinAPI {
    private final BaseAPI plugin;
    private final CoinDatabase database;

    public CoinAPI(BaseAPI plugin) {
        this.plugin = plugin;
        this.database = plugin.getCoinDatabase();
    }

    /**
     * 获取玩家硬币数量
     * @param player 玩家
     * @return 硬币数量
     */
    public CompletableFuture<Double> getCoins(Player player) {
        return database.getCoins(player.getUniqueId());
    }

    /**
     * 获取玩家硬币数量
     * @param uuid 玩家UUID
     * @return 硬币数量
     */
    public CompletableFuture<Double> getCoins(UUID uuid) {
        return database.getCoins(uuid);
    }

    /**
     * 同步获取玩家硬币数量
     * @param player 玩家
     * @return 硬币数量
     */
    public double getCoinsSync(Player player) {
        try {
            return database.getCoins(player.getUniqueId()).get();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * 增加硬币
     * @param player 玩家
     * @param amount 数量
     * @return 是否成功
     */
    public CompletableFuture<Boolean> addCoins(Player player, double amount) {
        return database.addCoins(player.getUniqueId(), amount);
    }

    /**
     * 减少硬币
     * @param player 玩家
     * @param amount 数量
     * @return 是否成功
     */
    public CompletableFuture<Boolean> removeCoins(Player player, double amount) {
        return database.removeCoins(player.getUniqueId(), amount);
    }

    /**
     * 设置硬币数量
     * @param player 玩家
     * @param amount 数量
     * @return 是否成功
     */
    public CompletableFuture<Boolean> setCoins(Player player, double amount) {
        return database.setCoins(player.getUniqueId(), amount);
    }

    /**
     * 检查是否有足够硬币
     * @param player 玩家
     * @param amount 数量
     * @return 是否足够
     */
    public CompletableFuture<Boolean> hasEnoughCoins(Player player, double amount) {
        return database.hasEnoughCoins(player.getUniqueId(), amount);
    }

    /**
     * 增加充值金额
     * @param player 玩家
     * @param amount 金额
     * @return 是否成功
     */
    public CompletableFuture<Boolean> addRechargedAmount(Player player, double amount) {
        return database.addRechargedAmount(player.getUniqueId(), amount);
    }

    /**
     * 减少充值金额
     * @param player 玩家
     * @param amount 金额
     * @return 是否成功
     */
    public CompletableFuture<Boolean> removeRechargedAmount(Player player, double amount) {
        return database.removeRechargedAmount(player.getUniqueId(), amount);
    }

    /**
     * 获取充值总额
     * @param player 玩家
     * @return 充值总额
     */
    public CompletableFuture<Double> getRechargedAmount(Player player) {
        return database.getRechargedAmount(player.getUniqueId());
    }

    /**
     * 转账（从A玩家转移到B玩家）
     * @param from 发送者
     * @param to 接收者
     * @param amount 数量
     * @return 是否成功
     */
    public CompletableFuture<Boolean> transferCoins(Player from, Player to, double amount) {
        return database.hasEnoughCoins(from.getUniqueId(), amount).thenCompose(hasEnough -> {
            if (!hasEnough) {
                return CompletableFuture.completedFuture(false);
            }

            // 在一个事务中执行两个操作
            return database.removeCoins(from.getUniqueId(), amount)
                    .thenCompose(removeSuccess -> {
                        if (removeSuccess) {
                            return database.addCoins(to.getUniqueId(), amount);
                        }
                        return CompletableFuture.completedFuture(false);
                    });
        });
    }

    /**
     * 格式化硬币显示
     * @param coins 硬币数量
     * @return 格式化字符串
     */
    public String formatCoins(double coins) {
        return String.format("%,.0f 硬币", coins);
    }

    /**
     * 获取硬币名称
     * @return 硬币
     */
    public String getCoinName() {
        return "硬币";
    }
}