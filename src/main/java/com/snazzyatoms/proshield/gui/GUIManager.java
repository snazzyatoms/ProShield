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

/**
 * GUIManager
 * - Opens menus from config
 * - Handles dynamic state rendering (flags, world-controls)
 * - Wires Admin Tools (approve/deny requests, world controls)
 * - Restores chat-driven flows for:
 *     • Expansion deny (manual reason)
 *     • Roles add/remove via chat input
 * - Adds reset confirmation submenu with dynamic world title
 */
public class GUIManager {

    private final ProShield plugin;

    private static final List<String> PERM_KEYS = List.of("build", "interact", "containers", "unclaim");

    // ===== Chat-driven state =====
    private final Set<UUID> awaitingDenyReason = new HashSet<>();

    private static final class RoleActionCtx {
        enum Type { ADD, REMOVE }
        final Type type;
        final UUID plotId;
        RoleActionCtx(Type type, UUID plotId) {
            this.type = type;
            this.plotId = plotId;
        }
    }
    private final Map<UUID, RoleActionCtx> awaitingRoleAction = new HashMap<>();

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
        if (menuName.equalsIgnoreCase("world-reset-confirm")) {
            openWorldResetConfirmMenu(player);
            return;
        }

        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus." + menuName);
        if (menuSec == null) {
            plugin.getLogger().warning("Menu not found in config: " + menuName);
            return;
        }

        String rawTitle = menuSec.getString("title", "Menu");
        String title = ChatColor.translateAlternateColorCodes('&',
                rawTitle.replace("{world}", player.getWorld().getName()));
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
     * World Reset Confirm (dynamic world in title)
     * ====================== */
    private void openWorldResetConfirmMenu(Player player) {
        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus.world-reset-confirm");
        if (menuSec == null) {
            plugin.getMessagesUtil().send(player, "&cReset confirmation menu missing in config.");
            return;
        }
        String worldName = player.getWorld().getName();
        String title = ChatColor.translateAlternateColorCodes('&',
                menuSec.getString("title", "&cConfirm Reset").replace("{world}", worldName));
        int size = menuSec.getInt("size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);

        fillMenuItems(inv, menuSec, player);
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

        // ===== Admin Tools: Approve / Deny flow =====
        if (action.equalsIgnoreCase("expansion:approve")) {
            var mgr = safeExpansionManager();
            if (mgr != null) {
                try {
                    mgr.approveSelected(player);
                } catch (Throwable t) {
                    plugin.getMessagesUtil().send(player, "&cApprove failed: " + t.getMessage());
                }
            } else {
                plugin.getMessagesUtil().send(player, "&7(Approve selected) &cNo expansion manager is present.");
            }
            return;
        }

        if (action.equalsIgnoreCase("expansion:deny")) {
            // Open deny reasons submenu
            openMenu(player, "deny-reasons");
            return;
        }

        if (action.startsWith("reason:")) {
            String key = action.substring("reason:".length());
            if (key.equalsIgnoreCase("other")) {
                // Enter chat capture mode
                awaitingDenyReason.add(player.getUniqueId());
                player.closeInventory();
                plugin.getMessagesUtil().send(player,
                        "&ePlease type the &fdeny reason &ein chat. &7(Your next message will be used.)");
                return;
            } else {
                // Preset reason path
                String reason = plugin.getConfig().getString("expansion.deny-reasons." + key, key);
                var mgr = safeExpansionManager();
                if (mgr != null) {
                    try {
                        mgr.denySelected(player, reason);
                        plugin.getMessagesUtil().send(player, "&cDenied request: &7" + reason);
                    } catch (Throwable t) {
                        plugin.getMessagesUtil().send(player, "&cDeny failed: " + t.getMessage());
                    }
                } else {
                    plugin.getMessagesUtil().send(player, "&7(Deny " + key + ") &cNo expansion manager is present.");
                }
                return;
            }
        }

        // ===== Roles add/remove (enter chat flow) =====
        if (action.equalsIgnoreCase("role:add") || action.equalsIgnoreCase("role:remove")) {
            Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
            if (plot == null) {
                plugin.getMessagesUtil().send(player, "&cYou must stand inside a claim to manage roles.");
                return;
            }
            if (!plot.isOwner(player.getUniqueId())) {
                plugin.getMessagesUtil().send(player, "&cOnly the claim owner can modify roles.");
                return;
            }
            RoleActionCtx.Type type = action.endsWith("add") ? RoleActionCtx.Type.ADD : RoleActionCtx.Type.REMOVE;
            awaitingRoleAction.put(player.getUniqueId(), new RoleActionCtx(type, plot.getId()));
            player.closeInventory();
            if (type == RoleActionCtx.Type.ADD) {
                plugin.getMessagesUtil().send(player, "&eType a player name to &aTRUST&7 in chat.");
            } else {
                plugin.getMessagesUtil().send(player, "&eType a player name to &cUNTRUST&7 in chat.");
            }
            return;
        }

        // ===== World controls toggles =====
        if (action.startsWith("toggle:world.")) {
            String key = action.substring("toggle:world.".length());
            toggleWorldControl(player, key);
            openWorldControlsMenu(player);
            return;
        }

        if (action.equalsIgnoreCase("reset:world")) {
            // Open confirm submenu
            openWorldResetConfirmMenu(player);
            return;
        }

        if (action.equalsIgnoreCase("reset:world.confirm")) {
            resetWorldControls(player);
            openWorldControlsMenu(player);
            return;
        }
    }

    /* ==============================
     * Chat-listener integration API
     * ============================== */

    // ----- Expansion deny reason (manual) -----
    public boolean isAwaitingReason(Player player) {
        return awaitingDenyReason.contains(player.getUniqueId());
    }

    public void provideManualReason(Player player, String input) {
        if (!isAwaitingReason(player)) return;

        awaitingDenyReason.remove(player.getUniqueId());
        String reason = ChatColor.stripColor(input == null ? "" : input.trim());
        if (reason.isEmpty()) {
            plugin.getMessagesUtil().send(player, "&cCancelled: empty reason.");
            return;
        }

        var mgr = safeExpansionManager();
        if (mgr != null) {
            try {
                mgr.denySelected(player, reason);
                plugin.getMessagesUtil().send(player, "&cDenied request: &7" + reason);
            } catch (Throwable t) {
                plugin.getMessagesUtil().send(player, "&cDeny failed: " + t.getMessage());
            }
        } else {
            plugin.getMessagesUtil().send(player, "&7(Deny manual) &cNo expansion manager is present.");
        }
    }

    // ----- Roles add/remove via chat -----
    public boolean isAwaitingRoleAction(Player player) {
        return awaitingRoleAction.containsKey(player.getUniqueId
