package com.xigua.baseAPI.api.versionWrapper;

import org.bukkit.Bukkit;

public class VersionMatcher {
    public VersionWrapper match() {
        String serverVersion = "";
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String[] parts = packageName.split("\\.");

        // 检查是否是新的无版本后缀结构 (如: org.bukkit.craftbukkit)
        if (parts.length >= 4) {
            String versionPart = parts[3];
            if (versionPart.startsWith("v")) {
                // 旧结构: org.bukkit.craftbukkit.v1_21_R1
                serverVersion = versionPart.substring(1);
            } else if (versionPart.equals("craftbukkit")) {
                // 新结构: org.bukkit.craftbukkit
                // 需要通过 Bukkit.getBukkitVersion() 获取版本
                String bukkitVersion = Bukkit.getBukkitVersion();
                String[] versionParts = bukkitVersion.split("\\.");
                if (versionParts.length >= 2) {
                    serverVersion = parseVersion(bukkitVersion);
                }
            }
        } else {
            // 新结构: org.bukkit.craftbukkit
            // 需要通过 Bukkit.getBukkitVersion() 获取版本
            String bukkitVersion = Bukkit.getBukkitVersion();
            String[] versionParts = bukkitVersion.split("\\.");
            if (versionParts.length >= 2) {
                serverVersion = parseVersion(bukkitVersion);
            }
        }
        try {
            return (VersionWrapper)Class.forName(this.getClass().getPackage().getName() + ".Wrapper" + serverVersion).newInstance();
        }
        catch (IllegalAccessException | InstantiationException exception) {
            throw new IllegalStateException("无法为版本实例化版本包装器 " + serverVersion, exception);
        }
        catch (ClassNotFoundException exception) {
            throw new IllegalStateException("SpigotMaster 不支持该服务器版本 \"" + serverVersion + "\" 当前支持版本\n" +
                    "1.18.2, 1.20, 1.20.1, 1.21\n\n" + exception);
        }
    }

    private String parseVersion(String bukkitVersion) {
        // 移除 -R0.1-SNAPSHOT 后缀
        String baseVersion = bukkitVersion.split("-")[0];

        // 分割版本号
        String[] parts = baseVersion.split("\\.");
        // 例如1.21.11的parts = ["1", "21", "11"]

        // 构建版本代码
        if (parts.length >= 2) {
            // 对于 1.21 使用 1_21
            return parts[0] + "_" + parts[1];
        }

        return "1_21"; // 默认
    }
}

