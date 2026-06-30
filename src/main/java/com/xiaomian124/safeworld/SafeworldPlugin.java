package com.xiaomian124.safeworld;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class SafeworldPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        FileConfiguration config = getConfig();
        SafeworldListener listener = new SafeworldListener(config);

        getServer().getPluginManager().registerEvents(listener, this);
        getLogger().info("SafeWorld 插件已启用 - 已加载配置");
    }

    @Override
    public void onDisable() {
        getLogger().info("SafeWorld 插件已禁用");
    }
}