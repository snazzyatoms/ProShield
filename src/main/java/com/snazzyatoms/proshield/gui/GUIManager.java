package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class GUIManager {

    private final ProShield plugin;

    private static final List<String> PERM_KEYS = List.of("build", "interact", "containers", "unclaim");

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /* ===================
     * Open Menu (general)
     * =================== */
    public void openMenu(Player player, String menuName) {
        if (menuName.equalsIgnoreCase("roles")) {
            openRolesMenu(player);
            return;
        }
        if (menuName.equalsIgnoreCase("flags")) {
            openFlagsMenu(player);
            return;
        }
        if (menuName.equalsIgnoreCase("world-controls")) {
            openWorldControlsMenu(player);
            return;
        }

        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus." + menuName);
        if (menuSec == null) {
            plugin.getLogger().warning("Menu not found in config: " + menuName);
            return;
        }

        String rawTitle = menuSec.getString("title", "Menu");
        String title = ChatColor.translateAlternateColorCodes('&', rawTitle.replace("{world}", player.getWorld().getName()));
        int size = menuSec.getInt("size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);

        fillMenuItems(inv, menuSec, player);
        player.openInventory(inv);
    }

    /* ======================
     * Flags menu (dynamic state)
     * ====================== */
    private void openFlagsMenu(Player player) {
        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        if (plot == null) {
            plugin.getMessagesUtil().send(player, "&cYou must stand inside a claim to manage flags.");
            return;
        }

        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus.flags");
        if (menuSec == null) return;

        String title = ChatColor.translateAlternateColorCodes('&',
                menuSec.getString("title", "&bClaim Flags"));
        int size = menuSec.getInt("size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);

        ConfigurationSection itemsSec = menuSec.getConfigurationSection("items");
        if (itemsSec != null) {
            for (String slotStr : itemsSec.getKeys(false)) {
                int slot = parseIntSafe(slotStr, -1);
                if (slot < 0) continue;

                ConfigurationSection itemSec = itemsSec.getConfigurationSection(slotStr);
                if (itemSec == null) continue;

                String action = itemSec.getString("action", "");
                String flagKey = action.startsWith("flag:") ? action.substring("flag:".length()) : null;

                Material mat = Material.matchMaterial(itemSec.getString("material", "STONE"));
                if (mat == null) mat = Material.STONE;

                ItemStack stack = new ItemStack(mat);
                ItemMeta meta = stack.getItemMeta();
                if (meta == null) continue;

                String name = itemSec.getString("name", "");
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

                List<String> lore = new ArrayList<>();
                for (String line : itemSec.getStringList("lore")) {
                    if (flagKey != null) {
                        boolean state = plot.getFlag(flagKey,
                                plugin.getConfig().getBoolean("claims.default-flags." + flagKey, false));
                        String stateText = state ? "&aON" : "&cOFF";
                        lore.add(ChatColor.translateAlternateColorCodes('&',
                                line.replace("{state}", stateText)));
                    } else {
                        lore.add(ChatColor.translateAlternateColorCodes('&', line));
                    }
                }
                meta.setLore(lore);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                stack.setItemMeta(meta);
                inv.setItem(slot, stack);
            }
        }

        player.openInventory(inv);
    }

    /* ======================
     * World Controls (per current world)
     * ====================== */
    private void openWorldControlsMenu(Player player) {
        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus.world-controls");
        if (menuSec == null) return;

        String worldName = player.getWorld().getName();
        String title = ChatColor.translateAlternateColorCodes('&',
                menuSec.getString("title", "&dWorld Controls").replace("{world}", worldName));
        int size = menuSec.getInt("size", 45);
        Inventory inv = Bukkit.createInventory(null, size, title);

        ConfigurationSection itemsSec = menuSec.getConfigurationSection("items");
        if (itemsSec != null) {
            for (String slotStr : itemsSec.getKeys(false)) {
                int slot = parseIntSafe(slotStr, -1);
                if (slot < 0) continue;

                ConfigurationSection itemSec = itemsSec.getConfigurationSection(slotStr);
                if (itemSec == null) continue;

                String action = itemSec.getString("action", "");
                Material mat = Material.matchMaterial(itemSec.getString("material", "STONE"));
                if (mat == null) mat = Material.STONE;

                ItemStack stack = new ItemStack(mat);
                ItemMeta meta = stack.getItemMeta();
                if (meta == null) continue;

                String name = itemSec.getString("name", "");
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

                List<String> lore = new ArrayList<>();
                for (String line : itemSec.getStringList("lore")) {
                    if (action.startsWith("toggle:world.")) {
                        String key = action.substring("toggle:world.".length());
                        boolean state = getWorldControl(player.getWorld().getName(), key);
                        String stateText = state ? "&aON" : "&cOFF";
                        lore.add(ChatColor.translateAlternateColorCodes('&',
                                line.replace("{state}", stateText).replace("{world}", worldName)));
                    } else {
                        lore.add(ChatColor.translateAlternateColorCodes('&',
                                line.replace("{world}", worldName)));
                    }
                }
                meta.setLore(lore);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                stack.setItemMeta(meta);
                inv.setItem(slot, stack);
            }
        }

        player.openInventory(inv);
    }

    /* ======================
     * Roles menu
     * ====================== */
    private void openRolesMenu(Player player) {
        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        if (plot == null) {
            plugin.getMessagesUtil().send(player, "&cYou must stand inside a claim to manage roles.");
            return;
        }
        if (!plot.isOwner(player.getUniqueId())) {
            plugin.getMessagesUtil().send(player, "&cOnly the claim owner can manage trusted players.");
            return;
        }

        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus.roles");
        String title = ChatColor.translateAlternateColorCodes('&',
                menuSec != null ? menuSec.getString("title", "&bTrusted Players") : "&bTrusted Players");
        int size = menuSec != null ? menuSec.getInt("size", 27) : 27;

        Inventory inv = Bukkit.createInventory(null, size, title);

        ClaimRoleManager rm = plugin.getRoleManager();
        Map<String, String> trusted = rm.getTrusted(plot.getId());
        int[] slots = headFillPattern(size);
        int idx = 0;

        for (Map.Entry<String, String> e : trusted.entrySet()) {
            if (idx >= slots.length) break;
            inv.setItem(slots[idx++], createPlayerHead(e.getKey(), e.getValue(), plot.getId()));
        }

        player.openInventory(inv);
    }

    private ItemStack createPlayerHead(String playerName, String role, UUID plotId) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            OfflinePlayer off = Bukkit.getOfflinePlayer(playerName);
            if (off != null) meta.setOwningPlayer(off);
            meta.setDisplayName(ChatColor.AQUA + playerName + ChatColor.GRAY + " (" + role + ")");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Role: " + ChatColor.YELLOW + role);

            Map<String, Boolean> perms = plugin.getRoleManager().getPermissions(plotId, playerName);
            if (!perms.isEmpty()) {
                lore.add(ChatColor.GRAY + "Overrides:");
                for (String k : PERM_KEYS) {
                    if (perms.containsKey(k)) {
                        lore.add(ChatColor.DARK_GRAY + " - " + k + ": " +
                                (perms.get(k) ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
                    }
                }
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            skull.setItemMeta(meta);
        }
        return skull;
    }

    /* ===============
     * Handle Clicks
     * =============== */
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        String title = event.getView().getTitle();
        String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        ConfigurationSection menus = plugin.getConfig().getConfigurationSection("gui.menus");
        if (menus == null) return;

        ConfigurationSection matchedMenu = null;
        for (String key : menus.getKeys(false)) {
            ConfigurationSection menuSec = menus.getConfigurationSection(key);
            if (menuSec == null) continue;
            String cfgTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                    menuSec.getString("title", ""))).replace("{world}", player.getWorld().getName());
            if (cfgTitle.equalsIgnoreCase(ChatColor.stripColor(title))) {
                matchedMenu = menuSec;
                break;
            }
        }
        if (matchedMenu == null) return;

        ConfigurationSection itemsSec = matchedMenu.getConfigurationSection("items");
        if (itemsSec == null) return;

        String action = null;
        for (String slotStr : itemsSec.getKeys(false)) {
            ConfigurationSection itemSec = itemsSec.getConfigurationSection(slotStr);
            if (itemSec == null) continue;
            String displayName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                    itemSec.getString("name", "")));
            if (displayName.equalsIgnoreCase(itemName)) {
                action = itemSec.getString("action", "");
                break;
            }
        }
        if (action == null) return;

        // Run actions
        if (action.equalsIgnoreCase("close")) {
            player.closeInventory();
            return;
        }

        if (action.startsWith("flag:")) {
            String flagKey = action.substring("flag:".length());
            Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
            if (plot != null) {
                toggleFlag(plot, flagKey, player);
                openFlagsMenu(player);
            } else {
                plugin.getMessagesUtil().send(player, "&cYou must be inside a claim.");
            }
            return;
        }

        if (action.startsWith("menu:")) {
            String menuName = action.substring("menu:".length());
            openMenu(player, menuName);
            return;
        }

        if (action.startsWith("command:")) {
            String cmdToRun = action.substring("command:".length());
            player.performCommand(cmdToRun);
            return;
        }

        if (action.startsWith("toggle:world.")) {
            String key = action.substring("toggle:world.".length());
            toggleWorldControl(player, key);
            openWorldControlsMenu(player);
            return;
        }

        if (action.equalsIgnoreCase("reset:world")) {
            resetWorldControls(player);
            openWorldControlsMenu(player);
            return;
        }

        if (action.startsWith("reason:")) {
            player.performCommand("proshield admin");
        }
    }

    /* ===============
     * Helpers
     * =============== */
    private void toggleFlag(Plot plot, String flag, Player player) {
        boolean current = plot.getFlag(flag,
                plugin.getConfig().getBoolean("claims.default-flags." + flag, false));
        plot.setFlag(flag, !current);
        plugin.getMessagesUtil().send(player,
                !current ? "&a" + flag + " enabled." : "&c" + flag + " disabled.");
    }

    private void toggleWorldControl(Player player, String key) {
        String base = "protection.world-controls";
        String world = player.getWorld().getName();

        boolean current = getWorldControl(world, key);
        boolean next = !current;

        String path = base + ".worlds." + world + "." + key;
        plugin.getConfig().set(path, next);
        plugin.saveConfig();

        plugin.getMessagesUtil().send(player, "&eWorld &7(" + world + ") &e" + key + " &7→ " + (next ? "&aON" : "&cOFF"));
    }

    private void resetWorldControls(Player player) {
        String base = "protection.world-controls.worlds." + player.getWorld().getName();
        plugin.getConfig().set(base, null); // clear overrides
        plugin.saveConfig();
        plugin.getMessagesUtil().send(player, "&eWorld controls reset to defaults for &7" + player.getWorld().getName());
    }

    private boolean getWorldControl(String world, String key) {
        String base = "protection.world-controls";
        if (!plugin.getConfig().getBoolean(base + ".enabled", true)) return true;

        String worldPath = base + ".worlds." + world + "." + key;
        if (plugin.getConfig().isSet(worldPath)) {
            return plugin.getConfig().getBoolean(worldPath);
        }
        return plugin.getConfig().getBoolean(base + ".defaults." + key, true);
    }

    private void fillMenuItems(Inventory inv, ConfigurationSection menuSec, Player player) {
        ConfigurationSection itemsSec = menuSec.getConfigurationSection("items");
        if (itemsSec == null) return;

        for (String slotStr : itemsSec.getKeys(false)) {
            int slot = parseIntSafe(slotStr, -1);
            if (slot < 0) continue;
            ConfigurationSection itemSec = itemsSec.getConfigurationSection(slotStr);
            if (itemSec == null) continue;

            Material mat = Material.matchMaterial(itemSec.getString("material", "STONE"));
            if (mat == null) mat = Material.STONE;

            ItemStack stack = new ItemStack(mat);
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) continue;

            String name = itemSec.getString("name", "");
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    name.replace("{world}", player.getWorld().getName())));

            List<String> lore = new ArrayList<>();
            for (String line : itemSec.getStringList("lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&',
                        line.replace("{world}", player.getWorld().getName())));
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            stack.setItemMeta(meta);
            inv.setItem(slot, stack);
        }
    }

    private int[] headFillPattern(int size) {
        if (size <= 27) return new int[]{10,11,12,13,14,15,16, 19,20,21,22,23,24,25};
        if (size <= 36) return new int[]{10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34};
        return new int[]{10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43};
    }

    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}
