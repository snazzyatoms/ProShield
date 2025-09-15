package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionQueue;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
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

    // For deny-reason via chat
    private static final Map<UUID, ExpansionRequest> awaitingReason = new HashMap<>();
    // For roles via chat (e.g., "role:add")
    private static final Map<UUID, String> awaitingRoleAction = new HashMap<>();
    private static final Map<UUID, UUID> awaitingRolePlot = new HashMap<>();

    private static final List<String> PERM_KEYS = List.of("build", "interact", "containers", "unclaim");

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /* ========================
     * Awaiting helper methods
     * ======================== */
    public static boolean isAwaitingReason(Player player) {
        return awaitingReason.containsKey(player.getUniqueId());
    }

    public static boolean isAwaitingRoleAction(Player player) {
        return awaitingRoleAction.containsKey(player.getUniqueId());
    }

    public static void setRoleAction(Player player, String action) {
        awaitingRoleAction.put(player.getUniqueId(), action);
    }

    public static void cancelAwaiting(Player player) {
        awaitingReason.remove(player.getUniqueId());
        awaitingRoleAction.remove(player.getUniqueId());
        awaitingRolePlot.remove(player.getUniqueId());
    }

    /** Chat hook: handle "role:add <playerName>" style input */
    public static void handleRoleChatInput(Player actor, String message, ProShield plugin) {
        String action = awaitingRoleAction.remove(actor.getUniqueId());
        UUID plotId = awaitingRolePlot.remove(actor.getUniqueId());
        if (action == null || plotId == null) {
            plugin.getMessagesUtil().send(actor, "&7No role action pending.");
            return;
        }

        Plot plot = plugin.getPlotManager().getPlotById(plotId);
        if (plot == null) {
            plugin.getMessagesUtil().send(actor, "&cThat claim no longer exists.");
            return;
        }

        if (action.equalsIgnoreCase("add")) {
            String targetName = message.trim();
            if (targetName.isEmpty()) {
                plugin.getMessagesUtil().send(actor, "&cNo player name supplied.");
                return;
            }

            ClaimRoleManager roles = plugin.getRoleManager();
            OfflinePlayer off = Bukkit.getOfflinePlayer(targetName);

            if (off != null && plot.isOwner(off.getUniqueId())) {
                plugin.getMessagesUtil().send(actor, "&cThat player already owns this claim.");
                return;
            }

            boolean ok = roles.trustPlayer(plot, targetName, "trusted");
            if (ok) {
                plugin.getMessagesUtil().send(actor, "&aTrusted &e" + targetName + " &aas &6trusted&a.");
            } else {
                plugin.getMessagesUtil().send(actor, "&e" + targetName + " &7was already trusted.");
            }
        } else {
            plugin.getMessagesUtil().send(actor, "&7Unhandled role action: " + action);
        }
    }

    /** Chat hook: manual deny reason for expansion */
    public static void provideManualReason(Player admin, String reason, ProShield plugin) {
        ExpansionRequest req = awaitingReason.remove(admin.getUniqueId());
        if (req == null) {
            plugin.getMessagesUtil().send(admin, "&7No pending request to deny.");
            return;
        }
        ExpansionQueue.denyRequest(req, reason);
        Player target = Bukkit.getPlayer(req.getPlayerId());
        if (target != null) target.sendMessage(ChatColor.RED + "Your expansion request was denied: " + reason);
        plugin.getMessagesUtil().send(admin, "&cRequest denied (" + reason + ").");
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

        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus." + menuName);
        if (menuSec == null) {
            plugin.getLogger().warning("Menu not found in config: " + menuName);
            return;
        }

        String title = ChatColor.translateAlternateColorCodes('&', menuSec.getString("title", "Menu"));
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

        String title = ChatColor.translateAlternateColorCodes('&', menuSec.getString("title", "&bClaim Flags"));
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

        // Optional: static buttons from config (add/remove/back) if present
        if (menuSec != null) {
            fillMenuItems(inv, menuSec, player);
        }

        ClaimRoleManager rm = plugin.getRoleManager();
        Map<String, String> trusted = rm.getTrusted(plot.getId());
        int[] slots = headFillPattern(size);
        int idx = 0;

        for (Map.Entry<String, String> e : trusted.entrySet()) {
            if (idx >= slots.length) break;
            inv.setItem(slots[idx++], createPlayerHead(e.getKey(), e.getValue(), plot.getId()));
        }

        awaitingRolePlot.put(player.getUniqueId(), plot.getId());
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

        // Look up the menu items in config based on the title
        ConfigurationSection menus = plugin.getConfig().getConfigurationSection("gui.menus");
        if (menus == null) return;

        ConfigurationSection matchedMenu = null;
        for (String key : menus.getKeys(false)) {
            ConfigurationSection menuSec = menus.getConfigurationSection(key);
            if (menuSec != null && ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                    menuSec.getString("title", ""))).equalsIgnoreCase(ChatColor.stripColor(title))) {
                matchedMenu = menuSec;
                break;
            }
        }
        if (matchedMenu == null) return;

        // Find which item matches
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

        // Handle the action
        if (action.startsWith("flag:")) {
            String flagKey = action.substring("flag:".length());
            Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
            if (plot != null) {
                toggleFlag(plot, flagKey, player);
                openFlagsMenu(player); // refresh ON/OFF state text
            }
        } else if (action.startsWith("menu:")) {
            String menuName = action.substring("menu:".length());
            openMenu(player, menuName);
        } else if (action.startsWith("command:")) {
            String cmdToRun = action.substring("command:".length());
            player.performCommand(cmdToRun);
        } else if (action.startsWith("reason:")) {
            String reasonKey = action.substring("reason:".length());
            List<ExpansionRequest> pending = ExpansionQueue.getPendingRequests();
            if (!pending.isEmpty()) {
                ExpansionRequest req = pending.get(0);
                if (reasonKey.equalsIgnoreCase("other")) {
                    awaitingReason.put(player.getUniqueId(), req);
                    plugin.getMessagesUtil().send(player, "&eType your denial reason in chat...");
                    player.closeInventory();
                } else {
                    ExpansionQueue.denyRequest(req, reasonKey);
                    plugin.getMessagesUtil().send(player, "&cRequest denied (" + reasonKey + ").");
                    openMenu(player, "admin-expansions");
                }
            }
        } else if (action.startsWith("role:")) {
            // role:add kicks to chat input (keeps previous behavior, no features lost)
            String sub = action.substring("role:".length());
            if (sub.equalsIgnoreCase("add")) {
                Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
                if (plot == null) {
                    plugin.getMessagesUtil().send(player, "&cStand inside your claim to add trusted players.");
                    return;
                }
                awaitingRolePlot.put(player.getUniqueId(), plot.getId());
                awaitingRoleAction.put(player.getUniqueId(), "add");
                plugin.getMessagesUtil().send(player, "&eType the player name to trust...");
                player.closeInventory();
            }
            // role:remove is still shift-click on head in roles list in earlier builds;
            // we preserve previous behavior to avoid feature loss.
        }
    }

    /* ===============
     * Helpers
     * =============== */
    private void toggleFlag(Plot plot, String flag, Player player) {
        boolean current = plot.getFlag(flag,
                plugin.getConfig().getBoolean("claims.default-flags." + flag, false));
        plot.setFlag(flag, !current);

        // Optional sound feedback
        String sound = plugin.getConfig().getString("sounds.flag-toggle", "BLOCK_NOTE_BLOCK_PLING");
        try { player.playSound(player.getLocation(), sound, 1f, 1f); } catch (Exception ignored) {}

        MessagesUtil messages = plugin.getMessagesUtil();
        messages.send(player, !current ? "&a" + flag + " enabled." : "&c" + flag + " disabled.");
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

            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    itemSec.getString("name", "")));

            List<String> lore = itemSec.getStringList("lore");
            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s));
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
