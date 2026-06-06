package com.xiaomian124.safeworld;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Enderman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.Tag;
import org.bukkit.event.block.Action;

public class SafeworldListener implements Listener {

    // 禁止玩家点燃TNT
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getPlayer() == null) return;
        if (event.getBlock().getType() == Material.TNT) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c你无法点燃TNT！");
        }
    }

    // 禁止玩家引爆末影水晶
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnderCrystalDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof EnderCrystal)) return;
        if (event.getDamager() instanceof Player) {
            event.setCancelled(true);
            ((Player) event.getDamager()).sendMessage("§c你无法引爆末影水晶！");
        }
    }

    // 禁止玩家在非下界维度引爆重生锚（使用萤石右键）
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawnAnchorInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.RESPAWN_ANCHOR) return;
        if (event.getItem() != null && event.getItem().getType() == Material.GLOWSTONE) {
            World world = event.getPlayer().getWorld();
            if (world.getEnvironment() != World.Environment.NETHER) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§c你无法引爆重生锚！");
            }
        }
    }

    // 禁止玩家激活凋灵
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWitherSpawn(EntitySpawnEvent event) {
        if (event.getEntityType().name().equals("WITHER")) {
            for (Player p : event.getEntity().getWorld().getPlayers()) {
                if (p.getLocation().distance(event.getLocation()) <= 10) {
                    event.setCancelled(true);
                    p.sendMessage("§c你无法激活凋灵！");
                }
            }
        }
    }

    // 禁止末影人搬方块
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEndermanCarry(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Enderman) {
            event.setCancelled(true);
        }
    }

    // 禁止绵羊吃草
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSheepEatGrass(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Sheep) {
            if (event.getBlock().getType() == Material.GRASS_BLOCK && event.getTo() == Material.DIRT) {
                event.setCancelled(true);
            }
        }
    }

    // 禁止玩家进入末地和下界
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPortal(PlayerPortalEvent event) {
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (cause == PlayerTeleportEvent.TeleportCause.END_PORTAL ||
                cause == PlayerTeleportEvent.TeleportCause.END_GATEWAY) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c你无法进入末地！");
        } else if (cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c你无法进入下界！");
        }
    }

    // 禁止玩家打开潜影盒（add in 1.1.0）
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onShulkerBoxOpen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (Tag.SHULKER_BOXES.isTagged(event.getClickedBlock().getType())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c你无法打开潜影盒！");
        }
    }
}