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

    // 数据库配置
    public String getDatabaseUrl() {
        return config.getString("database.host", "jdbc:mysql://localhost:3306");
    }

    public String getDatabaseUsername() {
        return config.getString("database.username", "root");
    }

    public String getDatabasePassword() {
        return config.getString("database.password", "");
    }

    public String getTableName() {
        return config.getString("database.table_name", "");
    }

    public String getDatabaseName() {
        return config.getString("database.database_name", "minecraft");
    }

    public boolean getIsDebug() {
        return config.getBoolean("is-debug", false);
    }
}
