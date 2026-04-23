package com.xiaomian124.safeworld;

import org.bukkit.plugin.java.JavaPlugin;

public final class SafeworldPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new SafeworldListener(), this);
        getLogger().info("SafeWorld 插件已启用 - 已禁止危险行为和跨维度传送");
    }

    @Override
    public void onDisable() {
        getLogger().info("SafeWorld 插件已禁用");
    }
}