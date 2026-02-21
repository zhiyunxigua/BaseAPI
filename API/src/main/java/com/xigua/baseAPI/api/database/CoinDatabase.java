package com.xigua.baseAPI.api.database;

import lombok.Getter;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoinDatabase {
    private Connection connection;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final String url;
    private final String username;
    private final String password;
    private final String tableName;
    private final String databaseName;

    public CoinDatabase(String url, String username, String password, String tableName, String databaseName) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.tableName = tableName;
        this.databaseName = databaseName;
    }

    public boolean connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            if (connection != null && !connection.isClosed()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("USE `" + databaseName + "`");
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "coins DOUBLE NOT NULL DEFAULT 0.0, " +
                "total_recharged DOUBLE NOT NULL DEFAULT 0.0, " +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 1. 获取玩家数据
    public CompletableFuture<PlayerCoinData> getPlayerData(UUID proxyUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM " + tableName + " WHERE uuid = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, proxyUuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return new PlayerCoinData(
                            proxyUuid,
                            rs.getDouble("coins"),
                            rs.getDouble("total_recharged")
                    );
                }
                return null;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }, executor);
    }

    // 2. 创建或更新玩家数据
    public CompletableFuture<Boolean> createOrUpdatePlayer(UUID proxyUuid, double defaultCoins) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO " + tableName + " (uuid, coins, total_recharged) " +
                    "VALUES (?, ?, 0.0) " +
                    "ON DUPLICATE KEY UPDATE last_updated = CURRENT_TIMESTAMP";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, proxyUuid.toString());
                stmt.setDouble(2, defaultCoins);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    // 3. 增加硬币
    public CompletableFuture<Boolean> addCoins(UUID proxyUuid, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            if (amount <= 0) return false;

            String sql = "INSERT INTO " + tableName + " (uuid, coins) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE coins = coins + ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, proxyUuid.toString());
                stmt.setDouble(2, amount);
                stmt.setDouble(3, amount);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    // 4. 减少硬币
    public CompletableFuture<Boolean> removeCoins(UUID proxyUuid, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            if (amount <= 0) return false;

            // 先检查余额
            String checkSql = "SELECT coins FROM " + tableName + " WHERE uuid = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                checkStmt.setString(1, proxyUuid.toString());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    double currentCoins = rs.getDouble("coins");
                    if (currentCoins < amount) {
                        return false; // 余额不足
                    }
                } else {
                    return false; // 玩家不存在
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }

            // 减少硬币
            String sql = "UPDATE " + tableName + " SET coins = coins - ? WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setDouble(1, amount);
                stmt.setString(2, proxyUuid.toString());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    // 5. 设置硬币数量
    public CompletableFuture<Boolean> setCoins(UUID proxyUuid, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            if (amount < 0) return false;

            String sql = "INSERT INTO " + tableName + " (uuid, coins) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE coins = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, proxyUuid.toString());
                stmt.setDouble(2, amount);
                stmt.setDouble(3, amount);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    // 6. 获取硬币数量
    public CompletableFuture<Double> getCoins(UUID proxyUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT coins FROM " + tableName + " WHERE uuid = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, proxyUuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getDouble("coins");
                }
                return 0.0; // 玩家不存在，返回0
            } catch (SQLException e) {
                e.printStackTrace();
                return 0.0;
            }
        }, executor);
    }

    // 7. 增加充值金额
    public CompletableFuture<Boolean> addRechargedAmount(UUID proxyUuid, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            if (amount <= 0) return false;

            String sql = "INSERT INTO " + tableName + " (uuid, total_recharged) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE total_recharged = total_recharged + ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, proxyUuid.toString());
                stmt.setDouble(2, amount);
                stmt.setDouble(3, amount);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    // 8. 减少充值金额（退款等情况）
    public CompletableFuture<Boolean> removeRechargedAmount(UUID proxyUuid, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            if (amount <= 0) return false;

            String sql = "UPDATE " + tableName + " SET total_recharged = total_recharged - ? WHERE uuid = ? AND total_recharged >= ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setDouble(1, amount);
                stmt.setString(2, proxyUuid.toString());
                stmt.setDouble(3, amount);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    // 9. 获取充值总额
    public CompletableFuture<Double> getRechargedAmount(UUID proxyUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT total_recharged FROM " + tableName + " WHERE uuid = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, proxyUuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getDouble("total_recharged");
                }
                return 0.0;
            } catch (SQLException e) {
                e.printStackTrace();
                return 0.0;
            }
        }, executor);
    }

    // 10. 检查是否有足够硬币
    public CompletableFuture<Boolean> hasEnoughCoins(UUID proxyUuid, double amount) {
        return getCoins(proxyUuid).thenApply(coins -> coins >= amount);
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            executor.shutdown();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 数据模型类
    public static class PlayerCoinData {
        @Getter
        private final UUID proxyUuid;  // 改为 UUID 类型
        @Getter
        private double coins;
        @Getter
        private double totalRecharged;

        public PlayerCoinData(UUID proxyUuid, double coins, double totalRecharged) {
            this.proxyUuid = proxyUuid;
            this.coins = coins;
            this.totalRecharged = totalRecharged;
        }
    }
}