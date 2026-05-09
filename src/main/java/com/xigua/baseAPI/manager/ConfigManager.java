package com.xigua.baseAPI.manager;

import com.xigua.baseAPI.BaseAPI;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

public class ConfigManager {
    private BaseAPI plugin;
    private FileConfiguration config;
    @Getter
    private final NeteaseShop neteaseShop;

    public ConfigManager(BaseAPI plugin) {
        this.plugin = plugin;
        ensureConfigFile();
        this.config = plugin.getConfig();
        this.neteaseShop = new NeteaseShop(config, this.isTestServer());
    }

    private void ensureConfigFile() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        config.options().copyDefaults(true);
        plugin.saveConfig();
        plugin.reloadConfig();
    }

    public boolean isTestServer() {
        return config.getBoolean("is-test-server", false);

    }

    // 网易RPC通道名字
    public String getNeteasePythonRpcPluginMassageName() {
        return config.getString("pythonRPC-plugin-massage-name", "floodgate:netease");
    }

    public boolean getIsDebug() {
        return config.getBoolean("debug", false);
    }

    public boolean getEnablePing() {
        return config.getBoolean("enable-ping", false);
    }

    public String getClientNamespace() {
        return config.getString("client.namespace", "Xigua_common");
    }

    public String getClientSystemName() {
        return config.getString("client.system-name", "main");
    }

    @Getter
    public static class NeteaseShop {
        private final Bedrock BE;
        private final JAVA JE;
        private final boolean enableJAVA;
        private final FloodgateApi floodgateApi;

        public NeteaseShop(FileConfiguration config, boolean isTestServer) {
            floodgateApi = FloodgateApi.getInstance();
            this.BE = new Bedrock(config, isTestServer);
            this.JE = new JAVA(config, isTestServer);
            this.enableJAVA = config.getBoolean("enable-java");
        }

        public String getShopServerUrl(UUID uuid) {
            if (!enableJAVA) {
                return BE.getShopServerUrl();
            } else if (floodgateApi.isFloodgatePlayer(uuid)) {
                return BE.getShopServerUrl();
            } else {
                return JE.getShopServerUrl();
            }
        }

        public String getGameId(UUID uuid) {
            if (!enableJAVA) {
                return BE.getGameId();
            } else if (floodgateApi.isFloodgatePlayer(uuid)) {
                return BE.getGameId();
            } else {
                return JE.getGameId();
            }
        }

        public String getGameKey(UUID uuid) {
            if (!enableJAVA) {
                return BE.getGameKey();
            } else if (floodgateApi.isFloodgatePlayer(uuid)) {
                return BE.getGameKey();
            } else {
                return JE.getGameKey();
            }
        }

        @Getter
        public static class Bedrock {
            private final String shopServerUrl;
            private final String gameId;
            private final String gameKey;

            public Bedrock(FileConfiguration config, boolean isTestServer) {
                if (isTestServer) {
                    this.shopServerUrl = config.getString("netease-shop.bedrock.test-shop-url", "http://gasproxy.mc.netease.com:60001");
                    this.gameKey = config.getString("netease-shop.bedrock.test-game-key", "");
                } else {
                    this.shopServerUrl = config.getString("netease-shop.bedrock.shop-url", "http://gasproxy.mc.netease.com:60002");
                    this.gameKey = config.getString("netease-shop.bedrock.game-key", "");
                }
                this.gameId = config.getString("netease-shop.bedrock.game-id", "");
            }
        }

        @Getter
        public static class JAVA {
            private final String shopServerUrl;
            private final String gameId;
            private final String gameKey;

            public JAVA(FileConfiguration config, boolean isTestServer) {
                if (isTestServer) {
                    this.shopServerUrl = config.getString("netease-shop.java.test-shop-url", "http://gasproxy.mc.netease.com:60003");
                    this.gameKey = config.getString("netease-shop.java.test-game-key", "");
                } else {
                    this.shopServerUrl = config.getString("netease-shop.java.shop-url", "http://gasproxy.mc.netease.com:60004");
                    this.gameKey = config.getString("netease-shop.java.game-key", "");
                }
                this.gameId = config.getString("netease-shop.java.game-id", "");
            }
        }
    }
}
