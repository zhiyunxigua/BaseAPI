package com.xigua.baseAPI.manager;

import com.xigua.baseAPI.BaseAPI;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final BaseAPI plugin;
    private final FileConfiguration config;

    public ConfigManager(BaseAPI plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    // 是否使用自定义商店
    public boolean getUseCustomShop() {
        return config.getBoolean("use-custom-shop", false);
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
}
