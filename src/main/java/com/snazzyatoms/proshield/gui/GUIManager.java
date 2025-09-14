// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.expansion.ExpansionRequestManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Date;
import java.util.List;

public class GUIManager {

    private final ProShield plugin;
    private final MessagesUtil messages;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
    }

    // ========================
    // Open Menus
    // ========================
    public void openMenu(Player player, String menu) {
        Inventory inv;

        // ------------------------
        // MAIN PLAYER MENU
        // ------------------------
        if (menu.equals("main")) {
            inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&6ProShield Menu"));

            addMenuItem(inv, 11, Material.GRASS_BLOCK, "&aClaim Land",
                    List.of("&7Protect your land", "&fRadius: " + plugin.getConfig().getInt("claims.default-radius") + " blocks by default"),
                    "command:claim");

            addMenuItem(inv, 13, Material.PAPER, "&eClaim Info",
                    List.of(
                            "&7Shows your current claim details",
                            "&7Protected: Pets, Containers, Frames, Stands",
                            "&7Explosions/Fire disabled",
                            "&7Default radius: " + plugin.getConfig().getInt("claims.default-radius") + " blocks"
                    ), "command:proshield info");

            addMenuItem(inv, 15, Material.BARRIER, "&cUnclaim Land",
                    List.of("&7Remove your claim"),
                    "command:unclaim");

            addMenuItem(inv, 21, Material.EMERALD, "&aRequest Expansion",
                    List.of("&7Request to expand your claim radius", "&7Choose from preset sizes"),
                    "menu:expansion-request");

        }
        // ------------------------
        // FLAGS MENU
        // ------------------------
        else if (menu.equals("flags")) {
            inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&dClaim Flags"));

            addMenuItem(inv, 10, Material.TNT, "&cExplosions",
                    List.of("&7Toggle TNT & creeper damage", "&eDisabled = explosions blocked", "&eEnabled = explosions allowed", "&fCurrent: {state}"),
                    "flag:explosions");

            addMenuItem(inv, 11, Material.WATER_BUCKET, "&bBuckets",
                    List.of("&7Toggle bucket use inside claims", "&eDisabled = bucket use blocked", "&eEnabled = bucket use allowed", "&fCurrent: {state}"),
                    "flag:buckets");

            addMenuItem(inv, 12, Material.ITEM_FRAME, "&6Item Frames",
                    List.of("&7Toggle protection of item frames", "&eDisabled = frames can be broken", "&eEnabled = frames protected", "&fCurrent: {state}"),
                    "flag:item-frames");

            addMenuItem(inv, 13, Material.ARMOR_STAND, "&eArmor Stands",
                    List.of("&7Toggle protection of armor stands", "&eDisabled = stands can be broken", "&eEnabled = stands protected", "&fCurrent: {state}"),
                    "flag:armor-stands");

            addMenuItem(inv, 14, Material.CHEST, "&aContainers",
                    List.of("&7Toggle access to chests, hoppers, furnaces", "&eDisabled = locked", "&eEnabled = anyone can open", "&fCurrent: {state}"),
                    "flag:containers");

            addMenuItem(inv, 15, Material.BONE, "&dPets",
                    List.of("&7Toggle pet protection", "&eDisabled = pets unprotected", "&eEnabled = pets safe", "&fCurrent: {state}"),
                    "flag:pets");

            addMenuItem(inv, 16, Material.IRON_SWORD, "&cPvP",
                    List.of("&7Toggle combat inside claims", "&eDisabled = PvP blocked", "&eEnabled = PvP allowed", "&fCurrent: {state}"),
                    "flag:pvp");

            addMenuItem(inv, 17, Material.SHIELD, "&aSafe Zone",
                    List.of("&7Toggle mob spawning & hostile damage", "&eDisabled = mobs can attack", "&eEnabled = mobs blocked", "&fCurrent: {state}"),
                    "flag:safezone");

            addMenuItem(inv, 26, Material.BARRIER, "&cBack",
                    List.of("&7Return to main menu"),
                    "menu:main");
        }
        // ------------------------
        // EXPANSION REQUEST (Player)
        // ------------------------
        else if (menu.equals("expansion-request")) {
            inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&aExpansion Request"));

            addMenuItem(inv, 10, Material.REDSTONE, "&c+10 blocks", List.of("&7Request +10 block radius"), "expansion:10");
            addMenuItem(inv, 12, Material.GOLD_INGOT, "&6+15 blocks", List.of("&7Request +15 block radius"), "expansion:15");
            addMenuItem(inv, 14, Material.DIAMOND, "&b+20 blocks", List.of("&7Request +20 block radius"), "expansion:20");
            addMenuItem(inv, 16, Material.EMERALD, "&a+30 blocks", List.of("&7Request +30 block radius"), "expansion:30");

            addMenuItem(inv, 26, Material.BARRIER, "&cBack", List.of("&7Return"), "menu:main");
        }
        // ------------------------
        // ADMIN MENU (simplified example)
        // ------------------------
        else if (menu.equals("admin")) {
            inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&cAdmin Menu"));

            addMenuItem(inv, 21, Material.BOOK, "&bExpansion Requests",
                    List.of("&7Review pending claim expansion requests", "&fPending: " + ExpansionRequestManager.getRequests().size()),
                    "menu:expansion-admin");

            addMenuItem(inv, 26, Material.BARRIER, "&cBack", List.of("&7Return to main"), "menu:main");
        }
        // ------------------------
        // ADMIN EXPANSION REVIEW
        // ------------------------
        else if (menu.equals("expansion-admin")) {
            inv = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', "&bExpansion Requests"));

            int slot = 0;
            for (ExpansionRequest req : ExpansionRequestManager.getRequests()) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(req.getPlayerId());
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta meta = head.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + target.getName());
                meta.setLore(List.of(
                        ChatColor.YELLOW + "Requested: +" + req.getExtraRadius() + " blocks",
                        ChatColor.GRAY + "Time: " + new Date(req.getRequestTime()),
                        ChatColor.GREEN + "Left-click = Approve",
                        ChatColor.RED + "Right-click = Deny"
                ));
                head.setItemMeta(meta);

                inv.setItem(slot, head);
                slot++;
            }

            addMenuItem(inv, 53, Material.BARRIER, "&cBack", List.of("&7Return"), "menu:admin");
        }
        else {
            messages.debug("Unknown menu: " + menu);
            return;
        }

        player.openInventory(inv);
    }

    // ========================
    // Handle Clicks
    // ========================
    public void handleClick(InventoryClickEvent event, String action, String menu) {
        Player player = (Player) event.getWhoClicked();

        if (action.startsWith("menu:")) {
            openMenu(player, action.split(":")[1]);
        }
        else if (action.startsWith("command:")) {
            player.performCommand(action.split(":")[1]);
            player.closeInventory();
        }
        else if (action.startsWith("flag:")) {
            String flag = action.split(":")[1];
            messages.send(player, "&eToggled flag: " + flag + " (demo only, implement logic)");
            player.closeInventory();
        }
        else if (action.startsWith("expansion:")) {
            int size = Integer.parseInt(action.split(":")[1]);
            ExpansionRequestManager.addRequest(new ExpansionRequest(player.getUniqueId(), size, System.currentTimeMillis()));
            player.sendMessage(ChatColor.GREEN + "Your expansion request (+" + size + " blocks) has been submitted.");
            player.closeInventory();
        }
        else if (menu.equals("expansion-admin")) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() == Material.PLAYER_HEAD) {
                String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                ExpansionRequest targetReq = ExpansionRequestManager.getRequests().stream()
                        .filter(r -> Bukkit.getOfflinePlayer(r.getPlayerId()).getName().equals(name))
                        .findFirst().orElse(null);

                if (targetReq != null) {
                    if (event.isLeftClick()) {
                        ExpansionRequestManager.removeRequest(targetReq);
                        player.sendMessage(ChatColor.GREEN + "Approved expansion request for " + name);
                        // TODO: apply expansion in PlotManager
                    } else if (event.isRightClick()) {
                        ExpansionRequestManager.removeRequest(targetReq);
                        player.sendMessage(ChatColor.RED + "Denied expansion request for " + name);
                    }
                }
            }
        }
    }

    // ========================
    // Helper
    // ========================
    private void addMenuItem(Inventory inv, int slot, Material mat, String name, List<String> lore, String action) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).toList());
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "action"),
                org.bukkit.persistence.PersistentDataType.STRING,
                action
        );
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }
}
