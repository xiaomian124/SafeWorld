package com.xiaomian124.safeworld;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Map;

public class SafeworldListener implements Listener {

    // 配置
    private final int maxEnchantLevel;
    private final int maxAttributeAmount;
    private final boolean forbidCommandBooks;
    private final boolean forbidInvisibleArmorStand;
    private final boolean forbidTextDisplay;
    private final boolean forbidBlockDisplay;
    private final boolean forbidSpawnEgg;
    private final int maxPotionAmplifier;

    public SafeworldListener(FileConfiguration config) {
        this.maxEnchantLevel = config.getInt("forbidden.max-enchant-level", 5);
        this.maxAttributeAmount = config.getInt("forbidden.max-attribute-amount", 100);
        this.forbidCommandBooks = config.getBoolean("forbidden.forbid-command-books", true);
        this.forbidInvisibleArmorStand = config.getBoolean("forbidden.entity.forbid-invisible-armor-stand", true);
        this.forbidTextDisplay = config.getBoolean("forbidden.entity.forbid-abnormal-text-display", true);
        this.forbidBlockDisplay = config.getBoolean("forbidden.entity.forbid-block-display", true);
        this.forbidSpawnEgg = config.getBoolean("forbidden.entity.forbid-spawner", true);
        this.maxPotionAmplifier = config.getInt("forbidden.max-potion-amplifier", 1);
    }

    // 禁止玩家点燃TNT
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getPlayer() == null) return;
        if (event.getBlock().getType() == Material.TNT) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c你无法点燃TNT！");
        }
    }

    // 禁止玩家引爆末影水晶（fix "弓箭、弩、骷髅射击" in 1.2.0）
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnderCrystalDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof EnderCrystal)) return;
        event.setCancelled(true);
        sendMessageToNearbyPlayers(event.getEntity(), "§c你无法引爆末影水晶！");
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

    // 禁止玩家进入末地和下界（fix "末影珍珠、命令、传送门" in 1.2.0）
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null) return;
        World fromWorld = event.getFrom().getWorld();
        World toWorld = event.getTo().getWorld();
        if (fromWorld.getEnvironment() == toWorld.getEnvironment()) return;
        World.Environment targetEnv = toWorld.getEnvironment();
        if (targetEnv == World.Environment.THE_END) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c你无法进入末地！");
        } else if (targetEnv == World.Environment.NETHER) {
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

    // 禁止玩家放置命令方块、屏障、结构方块、结构空位（add in 1.2.0）
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Material type = event.getBlock().getType();
        String message = null;
        if (type == Material.COMMAND_BLOCK ||
                type == Material.CHAIN_COMMAND_BLOCK ||
                type == Material.REPEATING_COMMAND_BLOCK) {
            message = "§c你无法放置命令方块！";
        } else if (type == Material.BARRIER) {
            message = "§c你无法放置屏障！";
        } else if (type == Material.STRUCTURE_BLOCK) {
            message = "§c你无法放置结构方块！";
        } else if (type == Material.STRUCTURE_VOID) {
            message = "§c你无法放置结构空位！";
        }
        if (message != null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(message);
        }
    }

    // ====================⇩ 禁止玩家刷异常NBT物品 ⇩====================（add in 1.2.0）
    // 拦截玩家捡起特殊定义物品
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        ItemStack item = event.getItem().getItemStack();
        if (isForbidden(item)) {
            event.setCancelled(true);
            event.getItem().remove();
            player.sendMessage("§c你无法获取此物品！");
        }
    }

    // 拦截容器中取出（背包、箱子等）
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        // 检查点击的物品（左键取出的物品）
        ItemStack current = event.getCurrentItem();
        if (current != null && !current.getType().isAir() && isForbidden(current)) {
            event.setCancelled(true);
            player.sendMessage("§c你无法获取此物品！");
            return;
        }

        // 检查光标上的物品（从物品栏中拿出）
        ItemStack cursor = event.getCursor();
        if (cursor != null && !cursor.getType().isAir() && isForbidden(cursor)) {
            event.setCancelled(true);
            player.sendMessage("§c你无法获取此物品！");
            event.setCursor(null);
        }
    }

    // 拦截合成结果
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack result = event.getCurrentItem();
        if (result != null && !result.getType().isAir() && isForbidden(result)) {
            event.setCancelled(true);
            player.sendMessage("§c你无法合成此物品！");
            event.getInventory().setResult(null);
        }
    }

    // 判断物品是否包含异常NBT
    private boolean isForbidden(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        // 检查附魔等级
        if (maxEnchantLevel > 0 && meta.hasEnchants()) {
            for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                if (entry.getValue() > maxEnchantLevel) return true;
            }
        }

        // 检查属性修饰符数值
        if (maxAttributeAmount > 0) {
            Map<String, Object> serialized = meta.serialize();
            if (serialized.containsKey("AttributeModifiers")) {
                List<Map<?, ?>> modifiers = (List<Map<?, ?>>) serialized.get("AttributeModifiers");
                if (modifiers != null) {
                    for (Map<?, ?> mod : modifiers) {
                        Number amount = (Number) mod.get("Amount");
                        if (amount != null && amount.doubleValue() > maxAttributeAmount) return true;
                    }
                }
            }
        }

        // 检查点击命令的书与笔
        if (forbidCommandBooks && meta instanceof BookMeta) {
            BookMeta bookMeta = (BookMeta) meta;
            List<String> pages = bookMeta.getPages();
            if (pages != null) {
                for (String page : pages) {
                    if (page != null && page.contains("\"clickEvent\"") && page.contains("\"action\":\"run_command\"")) {
                        return true;
                    }
                }
            }
        }

        // 检查药水效果等级
        if (maxPotionAmplifier >= 0 && meta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) meta;
            List<PotionEffect> effects = potionMeta.getCustomEffects();
            if (effects != null) {
                for (PotionEffect effect : effects) {
                    if (effect.getAmplifier() > maxPotionAmplifier) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    // ====================⇧ 禁止玩家刷异常NBT物品 ⇧====================

    // ====================⇩ 禁止生成特殊实体 ⇩====================（add in 1.2.0）
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();

        // 隐形盔甲架
        if (forbidInvisibleArmorStand && entity instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) entity;
            if (stand.isInvisible()) {
                event.setCancelled(true);
                sendMessageToNearbyPlayers(entity, "§c你无法生成隐形盔甲架！");
                return;
            }
        }

        // 禁止文本展示实体
        if (forbidTextDisplay && entity instanceof TextDisplay) {
            event.setCancelled(true);
            sendMessageToNearbyPlayers(entity, "§c你无法生成文本展示实体！");
            return;
        }

        // 禁止方块展示实体
        if (forbidBlockDisplay && entity instanceof BlockDisplay) {
            event.setCancelled(true);
            sendMessageToNearbyPlayers(entity, "§c你无法生成方块展示实体！");
        }
    }
    // ====================⇧ 禁止生成特殊实体 ⇧====================

    // ====================⇩ 禁止使用刷怪蛋 ⇩====================（add in 1.2.0）
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!forbidSpawnEgg) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
        ItemStack item = event.getItem();
        if (item != null && isSpawnEgg(item.getType())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c你无法使用刷怪蛋！");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDispense(BlockDispenseEvent event) {
        if (!forbidSpawnEgg) return;
        ItemStack item = event.getItem();
        if (item != null && isSpawnEgg(item.getType())) {
            event.setCancelled(true);
            if (event.getBlock().getState() instanceof org.bukkit.block.Dispenser) {
                sendMessageToNearbyPlayers(event.getBlock().getWorld(), event.getBlock().getLocation(), "§c你无法使用刷怪蛋！");
            }
        }
    }

    private boolean isSpawnEgg(Material material) {
        return material.name().endsWith("_SPAWN_EGG");
    }
    // ====================⇧ 禁止使用刷怪蛋 ⇧====================

    // 给实体附近6格内的玩家发送消息
    private void sendMessageToNearbyPlayers(Entity entity, String message) {
        for (Player p : entity.getWorld().getPlayers()) {
            if (p.getLocation().distance(entity.getLocation()) <= 6) {
                p.sendMessage(message);
            }
        }
    }

    // 给位置附近6格内的玩家发送消息
    private void sendMessageToNearbyPlayers(World world, org.bukkit.Location location, String message) {
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distance(location) <= 6) {
                p.sendMessage(message);
            }
        }
    }
}