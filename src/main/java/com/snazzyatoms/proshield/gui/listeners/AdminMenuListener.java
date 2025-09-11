package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

/**
 * AdminMenuListener
 *
 * ✅ Handles clicks inside the Admin GUI menu
 * ✅ Includes Claim / Unclaim actions like player menu
 * ✅ Updates lore live for claim/unclaim
 * ✅ Prevents icon movement
 * ✅ Plays sound feedback
 */
public class AdminMenuListener implements Listener {

    private final ProShield plugin;
    private final GUICache cache;

    public AdminMenuListener(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        if (!cache.isAdminMenu(uuid, event.getInventory())) return;

        event.setCancelled(true); // ✅ Prevent item movement

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();
        Inventory inv = event.getInventory();

        switch (name) {
            // --- Player-like actions ---
            case "claim chunk" -> {
                boolean success = player.performCommand("claim");
                if (success) {
                    updateLore(clicked, "§a✔ Claimed");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                } else {
                    updateLore(clicked, "§c✘ Already Claimed");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.6f);
                }
                inv.setItem(event.getSlot(), clicked);
            }
            case "unclaim chunk" -> {
                boolean success = player.performCommand("unclaim");
                if (success) {
                    updateLore(clicked, "§c✘ Unclaimed");
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
                } else {
                    updateLore(clicked, "§e⚠ Nothing to Unclaim");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.6f);
                }
                inv.setItem(event.getSlot(), clicked);
            }
            case "claim info" -> {
                player.performCommand("info");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.1f);
            }
            case "trust menu" -> {
                player.closeInventory();
                player.performCommand("trust");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case "untrust menu" -> {
                player.closeInventory();
                player.performCommand("untrust");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case "flags" -> {
                player.performCommand("flags");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case "roles" -> {
                player.performCommand("roles");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }

            // --- Admin Tools ---
            case "debug logging" -> {
                boolean state = plugin.toggleDebug();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, state ? 1.3f : 0.7f);
            }
            case "wilderness messages" -> {
                boolean current = plugin.getConfig().getBoolean("messages.show-wilderness", false);
                plugin.getConfig().set("messages.show-wilderness", !current);
                plugin.saveConfig();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, !current ? 1.3f : 0.7f);
            }
            case "admin flag chat" -> {
                boolean current = plugin.getConfig().getBoolean("messages.admin-flag-chat", true);
                plugin.getConfig().set("messages.admin-flag-chat", !current);
                plugin.saveConfig();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, !current ? 1.3f : 0.7f);
            }
            case "force unclaim" -> {
                player.closeInventory();
                player.performCommand("proshield forceunclaim");
                player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1f, 1f);
            }
            case "transfer claim" -> {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Use /transfer <player> to transfer ownership.");
            }
            case "teleport to claim" -> {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Use /proshield tp <owner> to teleport to claims.");
            }
            case "purge expired claims" -> {
                player.closeInventory();
                player.performCommand("proshield purge");
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1f, 0.6f);
            }

            case "back" -> {
                player.closeInventory();
                plugin.getGuiManager().openAdminMain(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.9f);
            }

            default -> { /* ignore */ }
        }
    }

    /* -------------------------------------------------------
     * Helper: update lore text of an item
     * ------------------------------------------------------- */
    private void updateLore(ItemStack item, String status) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            lore = new java.util.ArrayList<>();
        }

        if (lore.size() > 1) {
            lore.set(lore.size() - 1, status);
        } else {
            lore.add(status);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }
}
