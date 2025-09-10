package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {
    private final ProShield plugin;
    private final GUIManager gui;
    private final GUICache cache;

    public GUIListener(ProShield plugin, GUIManager gui, GUICache cache) {
        this.plugin = plugin;
        this.gui = gui;
        this.cache = cache;
    }

    private boolean isSameItem(ItemStack clicked, Material expected) {
        return clicked != null && clicked.getType() == expected && clicked.hasItemMeta();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory inv = event.getInventory();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        String title = event.getView().getTitle();
        event.setCancelled(true); // prevent taking GUI items

        // --- MAIN MENU ---
        if (title.equals("ProShield Menu")) {
            if (isSameItem(clicked, Material.GRASS_BLOCK)) {
                player.performCommand("proshield claim");
            } else if (isSameItem(clicked, Material.BARRIER)) {
                player.performCommand("proshield unclaim");
            } else if (isSameItem(clicked, Material.BOOK)) {
                player.performCommand("proshield info");
            } else if (isSameItem(clicked, Material.PAPER)) {
                player.performCommand("proshield help");
            } else if (isSameItem(clicked, Material.COMPASS)) {
                gui.openAdmin(player);
            } else if (isSameItem(clicked, Material.ARROW)) {
                player.closeInventory();
            }
        }

        // --- ADMIN MENU ---
        else if (title.equals("ProShield Admin")) {
            if (isSameItem(clicked, Material.FLINT_AND_STEEL)) {
                plugin.getConfig().set("protection.fire.enabled",
                        !plugin.getConfig().getBoolean("protection.fire.enabled"));
                plugin.saveConfig();
                player.sendMessage("§cFire protection toggled.");
            } else if (isSameItem(clicked, Material.TNT)) {
                plugin.getConfig().set("protection.explosions.enabled",
                        !plugin.getConfig().getBoolean("protection.explosions.enabled"));
                plugin.saveConfig();
                player.sendMessage("§cExplosion protection toggled.");
            } else if (isSameItem(clicked, Material.ENDERMAN_SPAWN_EGG)) {
                plugin.getConfig().set("protection.entity-grief.enabled",
                        !plugin.getConfig().getBoolean("protection.entity-grief.enabled"));
                plugin.saveConfig();
                player.sendMessage("§cEntity grief toggled.");
            } else if (isSameItem(clicked, Material.OAK_DOOR)) {
                plugin.getConfig().set("protection.interactions.enabled",
                        !plugin.getConfig().getBoolean("protection.interactions.enabled"));
                plugin.saveConfig();
                player.sendMessage("§cInteractions toggled.");
            } else if (isSameItem(clicked, Material.DIAMOND_SWORD)) {
                plugin.getConfig().set("protection.pvp-in-claims",
                        !plugin.getConfig().getBoolean("protection.pvp-in-claims"));
                plugin.saveConfig();
                player.sendMessage("§cPvP protection toggled.");
            } else if (isSameItem(clicked, Material.CHEST)) {
                plugin.getConfig().set("claims.keep-items.enabled",
                        !plugin.getConfig().getBoolean("claims.keep-items.enabled"));
                plugin.saveConfig();
                player.sendMessage("§cKeep items toggled.");
            } else if (isSameItem(clicked, Material.LAVA_BUCKET)) {
                player.performCommand("proshield purgeexpired 30 dryrun");
            } else if (isSameItem(clicked, Material.REDSTONE)) {
                player.performCommand("proshield reload");
            } else if (isSameItem(clicked, Material.COMPARATOR)) {
                player.performCommand("proshield debug toggle");
            } else if (isSameItem(clicked, Material.BEDROCK)) {
                plugin.getConfig().set("spawn.block-claiming",
                        !plugin.getConfig().getBoolean("spawn.block-claiming"));
                plugin.saveConfig();
                player.sendMessage("§cSpawn Guard toggled.");
            } else if (isSameItem(clicked, Material.ENDER_PEARL)) {
                player.sendMessage("§eUse /proshield tp <player|claim> to teleport.");
            } else if (isSameItem(clicked, Material.PAPER)) {
                player.performCommand("proshield help");
            } else if (isSameItem(clicked, Material.ARROW)) {
                gui.openMain(player, true);
            }
        }

        // --- PLAYER MENU (NEW in 1.2.5) ---
        else if (title.equals("ProShield Player")) {
            if (isSameItem(clicked, Material.PLAYER_HEAD)) {
                player.sendMessage("§aUse /proshield trust <player> [role] to trust a player.");
            } else if (isSameItem(clicked, Material.ZOMBIE_HEAD)) {
                player.sendMessage("§cUse /proshield untrust <player> to untrust a player.");
            } else if (isSameItem(clicked, Material.NAME_TAG)) {
                player.sendMessage("§bUse /proshield trust <player> <role> to manage roles.");
            } else if (isSameItem(clicked, Material.WRITABLE_BOOK)) {
                player.sendMessage("§eUse /proshield transfer <player> to transfer ownership.");
            } else if (isSameItem(clicked, Material.BANNER)) {
                player.sendMessage("§dUse /proshield flags to manage claim flags.");
            } else if (isSameItem(clicked, Material.GLASS)) {
                player.performCommand("proshield preview");
            } else if (isSameItem(clicked, Material.PAPER)) {
                player.performCommand("proshield help");
            } else if (isSameItem(clicked, Material.ARROW)) {
                gui.openMain(player, false);
            }
        }
    }
}
