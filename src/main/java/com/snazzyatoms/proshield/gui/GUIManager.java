package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

/**
 * GUIManager
 * - Opens menus declared in config.yml (gui.menus.*)
 * - Handles click actions:
 *   • command:<cmd>
 *   • menu:<key>
 *   • toggle:<flag>  (per-claim)
 *   • toggle:claim.<flag>
 *   • toggle:world.<flag>
 *   • expansion:approve / expansion:deny
 *   • reason:<key> / reason:manual
 *   • reset:world.confirm
 *   • close
 * - Supports roles GUI (nearby players trust) + role assignment via chat
 */
public class GUIManager {

    private final ProShield plugin;
    private final MessagesUtil messages;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    // Chat-input trackers and context
    private final Map<UUID, Boolean> awaitingReason = new HashMap<>();
    private final Map<UUID, Boolean> awaitingRoleAction = new HashMap<>();
    private final Map<UUID, UUID> pendingRoleTarget = new HashMap<>(); // player -> targetUuid

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
        this.plotManager = plugin.getPlotManager();
        this.roleManager = plugin.getRoleManager();
    }

    /* ======================================================
     * Public API used by listeners
     * ====================================================== */
    public void openMenu(Player player, String menuKey) {
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection menuSec = cfg.getConfigurationSection("gui.menus." + menuKey);
        if (menuSec == null) {
            messages.send(player, "&cMenu not found: &f" + menuKey);
            return;
        }

        String title = color(menuSec.getString("title", "&7Menu"));
        int size = Math.max(9, Math.min(54, menuSec.getInt("size", 27)));
        Inventory inv = Bukkit.createInventory(null, size, title);

        // Fill from config
        if (menuSec.isConfigurationSection("items")) {
            for (String slotKey : menuSec.getConfigurationSection("items").getKeys(false)) {
                int slot;
                try { slot = Integer.parseInt(slotKey); } catch (Exception e) { continue; }
                if (slot < 0 || slot >= size) continue;

                String path = "items." + slotKey + ".";
                String matName = menuSec.getString(path + "material", "STONE");
                Material mat = Material.matchMaterial(matName);
                if (mat == null) mat = Material.STONE;

                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(color(menuSec.getString(path + "name", "&fItem")));
                    List<String> lore = menuSec.getStringList(path + "lore");
                    if (lore != null && !lore.isEmpty()) {
                        List<String> out = new ArrayList<>();
                        for (String line : lore) out.add(color(line));
                        meta.setLore(out);
                    }
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    item.setItemMeta(meta);
                }
                inv.setItem(slot, item);
            }
        }

        // Contextual augmentation for special menus
        if (menuKey.equalsIgnoreCase("roles")) {
            decorateRolesMenu(inv, player);
        } else if (menuKey.equalsIgnoreCase("flags")) {
            decorateFlagsMenu(inv, player);
        } else if (menuKey.equalsIgnoreCase("world-controls")) {
            decorateWorldControls(inv, player);
        }

        player.openInventory(inv);
    }

    public void handleClick(InventoryClickEvent event) {
        HumanEntity who = event.getWhoClicked();
        if (!(who instanceof Player player)) return;

        // Only process clicks in ProShield menus (basic heuristic)
        if (!ChatColor.stripColor(event.getView().getTitle()).toLowerCase(Locale.ROOT).contains("proshield")) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir() || clicked.getItemMeta() == null) return;

        // Look up config item by matching its display name in the active menu def,
        // then read the configured "action" string and dispatch.
        String display = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        String activeMenuKey = resolveMenuKeyFromTitle(event.getView().getTitle());

        String action = findActionForDisplay(activeMenuKey, display);
        if (action == null || action.isBlank()) {
            // Fallback: try well-known buttons (Back/Exit)
            if (display.equalsIgnoreCase("Back") || display.equalsIgnoreCase("Exit Menu") || display.equalsIgnoreCase("Exit")) {
                player.closeInventory();
            }
            return;
        }

        dispatchAction(player, activeMenuKey, action.trim().toLowerCase(Locale.ROOT));
    }

    /* ======================================================
     * Roles chat input flow
     * ====================================================== */
    public boolean isAwaitingReason(Player player) {
        return awaitingReason.getOrDefault(player.getUniqueId(), false);
    }

    public void provideManualReason(Player player, String reason) {
        awaitingReason.remove(player.getUniqueId());
        // Here you would send the deny to your persistence/queue; for now we notify.
        messages.send(player, "&cDenied expansion with reason: &7" + reason);
        // Notify requester if you store selection context elsewhere
    }

    public void setAwaitingReason(Player player, boolean waiting) {
        awaitingReason.put(player.getUniqueId(), waiting);
    }

    public boolean isAwaitingRoleAction(Player player) {
        return awaitingRoleAction.getOrDefault(player.getUniqueId(), false);
    }

    public void handleRoleChatInput(Player player, String message) {
        awaitingRoleAction.remove(player.getUniqueId());
        UUID target = pendingRoleTarget.remove(player.getUniqueId());
        if (target == null) {
            messages.send(player, "&cNo target set for role assignment.");
            return;
        }
        roleManager.assignRoleViaChat(player, target, message);
    }

    public void setAwaitingRoleAction(Player player, UUID targetUuid, boolean waiting) {
        awaitingRoleAction.put(player.getUniqueId(), waiting);
        if (waiting) pendingRoleTarget.put(player.getUniqueId(), targetUuid);
    }

    /* ======================================================
     * Internals – dispatchers & helpers
     * ====================================================== */

    private void dispatchAction(Player player, String activeMenuKey, String action) {
        // command:<cmd>
        if (action.startsWith("command:")) {
            String cmd = action.substring("command:".length());
            executeCommandButton(player, cmd);
            return;
        }

        // menu:<key>
        if (action.startsWith("menu:")) {
            String next = action.substring("menu:".length());
            openMenu(player, next);
            return;
        }

        // close
        if (action.equals("close")) {
            player.closeInventory();
            return;
        }

        // toggle:<...>
        if (action.startsWith("toggle:")) {
            handleToggleAction(player, action.substring("toggle:".length()));
            // refresh flags/world-controls view if we’re in it
            if (activeMenuKey.equalsIgnoreCase("flags") || activeMenuKey.equalsIgnoreCase("world-controls")) {
                Bukkit.getScheduler().runTask(plugin, () -> openMenu(player, activeMenuKey));
            }
            return;
        }

        // expansion:<...>
        if (action.startsWith("expansion:")) {
            String op = action.substring("expansion:".length());
            handleExpansionAction(player, op);
            return;
        }

        // reason:<...>
        if (action.startsWith("reason:")) {
            String key = action.substring("reason:".length());
            handleDenyReason(player, key);
            return;
        }

        // reset:world.confirm
        if (action.equals("reset:world.confirm")) {
            resetWorldControls(player);
            // bounce back
            Bukkit.getScheduler().runTask(plugin, () -> openMenu(player, "world-controls"));
            return;
        }

        // Unrecognized -> ignore
    }

    private void executeCommandButton(Player player, String cmd) {
        // Known built-ins for smoother UX
        switch (cmd.toLowerCase(Locale.ROOT)) {
            case "proshield claim" -> plugin.getPlotManager().claimPlot(player);
            case "proshield unclaim" -> plugin.getPlotManager().unclaimPlot(player);
            case "proshield info" -> plugin.getPlotManager().sendClaimInfo(player);
            default -> player.performCommand(cmd);
        }
    }

    private void handleToggleAction(Player player, String raw) {
        // toggle:<flag>
        // toggle:claim.<flag>
        // toggle:world.<flag>
        if (raw.startsWith("world.")) {
            toggleWorldFlag(player, raw.substring("world.".length()));
            return;
        }
        String flag = raw.startsWith("claim.") ? raw.substring("claim.".length()) : raw;
        toggleClaimFlag(player, flag);
    }

    private void toggleClaimFlag(Player player, String flag) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&cNo claim here.");
            return;
        }
        // flip state: true->false or false->true with smart defaults
        boolean current = plot.getFlag(flag, claimDefault(flag));
        plot.setFlag(flag, !current);

        messages.send(player, "&eFlag &b" + flag + "&e is now &f" + (!current));
    }

    private boolean claimDefault(String flag) {
        FileConfiguration cfg = plugin.getConfig();
        // Default to world-controls defaults when available; otherwise safest options
        ConfigurationSection def = cfg.getConfigurationSection("protection.world-controls.defaults");
        if (def != null && def.contains(flag)) {
            return cfg.getBoolean("protection.world-controls.defaults." + flag, true);
        }
        // safe-ish defaults
        return switch (flag.toLowerCase(Locale.ROOT)) {
            case "pvp" -> false;               // safe by default
            case "explosions" -> false;
            case "fire-burn", "fire-spread" -> false;
            case "mob-damage", "mob-spawn" -> false;
            case "bucket-use" -> false;
            default -> true;
        };
    }

    private void toggleWorldFlag(Player player, String flag) {
        if (!player.hasPermission("proshield.admin.worldcontrols")) {
            messages.send(player, "&cNo permission for world controls.");
            return;
        }
        String world = player.getWorld().getName();
        String base = "protection.world-controls.worlds." + world + "." + flag;
        boolean cur = plugin.getConfig().getBoolean(base, plugin.getConfig().getBoolean("protection.world-controls.defaults." + flag, true));
        plugin.getConfig().set(base, !cur);
        plugin.saveConfig();
        messages.send(player, "&dWorld flag &b" + flag + " &dfor &f" + world + " &dis now &f" + (!cur));
    }

    private void handleExpansionAction(Player player, String op) {
        // You can wire into your queue/persistence here; this is the UI flow.
        switch (op) {
            case "approve" -> {
                if (!player.hasPermission("proshield.admin.expansions")) {
                    messages.send(player, "&cNo permission.");
                    return;
                }
                messages.send(player, "&aApproved selected expansion.");
            }
            case "deny" -> {
                if (!player.hasPermission("proshield.admin.expansions")) {
                    messages.send(player, "&cNo permission.");
                    return;
                }
                openMenu(player, "deny-reasons");
            }
            case "request" -> {
                // Player request — you can record details (size, radius) elsewhere
                String msg = plugin.getConfig().getString("messages.expansion-request",
                        "&eYour expansion request was sent to admins.");
                messages.send(player, msg);
                // TODO: persist request; notify online admins
            }
            default -> {}
        }
    }

    private void handleDenyReason(Player player, String key) {
        if (!player.hasPermission("proshield.admin.expansions")) {
            messages.send(player, "&cNo permission.");
            return;
        }
        switch (key) {
            case "too-large" -> messages.send(player,
                    plugin.getConfig().getString("messages.expansion-denied", "&cExpansion denied: {reason}")
                            .replace("{reason}", "Too Large"));
            case "abusive" -> messages.send(player,
                    plugin.getConfig().getString("messages.expansion-denied", "&cExpansion denied: {reason}")
                            .replace("{reason}", "Abusive"));
            case "manual" -> {
                setAwaitingReason(player, true);
                messages.send(player, "&eType a reason in chat. Your message will not be broadcast.");
            }
            default -> {}
        }
        // back to admin tools for convenience
        Bukkit.getScheduler().runTask(plugin, () -> openMenu(player, "admin-tools"));
    }

    private void resetWorldControls(Player player) {
        if (!player.hasPermission("proshield.admin.worldcontrols")) {
            messages.send(player, "&cNo permission.");
            return;
        }
        String world = player.getWorld().getName();
        String path = "protection.world-controls.worlds." + world;
        plugin.getConfig().set(path, null); // clear overrides
        plugin.saveConfig();
        messages.send(player, "&cWorld overrides for &f" + world + " &creset to defaults.");
    }

    /* ======================================================
     * Dynamic decorators (live state in GUI)
     * ====================================================== */

    private void decorateRolesMenu(Inventory inv, Player owner) {
        // Show nearby players as heads so owner can click and then type role in chat
        double radius = 10.0;
        List<Entity> nearby = owner.getNearbyEntities(radius, radius, radius);

        int slot = 10; // put entries starting here
        Set<UUID> added = new HashSet<>();
        for (Entity e : nearby) {
            if (!(e instanceof Player target)) continue;
            if (target.getUniqueId().equals(owner.getUniqueId())) continue;
            if (!added.add(target.getUniqueId())) continue;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = head.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(color("&b" + target.getName()));
                List<String> lore = new ArrayList<>();
                lore.add(color("&7Click → type role in chat"));
                lore.add(color("&7Examples: &ftrusted&7, &fmember&7, &fbuilder"));
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            if (slot < inv.getSize()) {
                inv.setItem(slot++, head);
            }
        }
        // Back / Exit are already defined in config; we leave them as-is.
    }

    private void decorateFlagsMenu(Inventory inv, Player player) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) return;

        // Common flag keys for claims
        String[] flags = new String[]{
                "pvp", "explosions", "fire-burn", "fire-spread",
                "mob-damage", "mob-spawn", "bucket-use"
        };

        // Render a summary paper at a fixed slot if empty
        int summarySlot = inv.getSize() - 9; // bottom row start
        if (inv.getItem(summarySlot) == null || inv.getItem(summarySlot).getType().isAir()) {
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(color("&eClaim Flags Summary"));
                List<String> lore = new ArrayList<>();
                for (String f : flags) {
                    boolean val = plot.getFlag(f, claimDefault(f));
                    lore.add(color("&7" + f + ": &f" + val));
                }
                meta.setLore(lore);
                paper.setItemMeta(meta);
            }
            inv.setItem(summarySlot, paper);
        }
    }

    private void decorateWorldControls(Inventory inv, Player player) {
        String world = player.getWorld().getName();
        // Optionally annotate entries with current state via lore replacement.
        // The base menu text already shows {world} in your config.
        // If you want live {state}, you'd replace item lore here by reading config,
        // but that requires remembering which slot maps to which flag. Skipping here
        // because your config already shows a {state} placeholder.
    }

    /* ======================================================
     * Utilities
     * ====================================================== */
    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }

    private String resolveMenuKeyFromTitle(String title) {
        // Best-effort: match known titles in config
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection menus = cfg.getConfigurationSection("gui.menus");
        if (menus != null) {
            for (String key : menus.getKeys(false)) {
                String tt = ChatColor.stripColor(color(menus.getString(key + ".title", "")));
                if (!tt.isBlank() && ChatColor.stripColor(title).equalsIgnoreCase(tt)) {
                    return key;
                }
            }
        }
        // fallback to "main" to avoid NPE
        return "main";
    }

    private String findActionForDisplay(String menuKey, String displayName) {
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection menuSec = cfg.getConfigurationSection("gui.menus." + menuKey);
        if (menuSec == null || !menuSec.isConfigurationSection("items")) return null;

        for (String slotKey : menuSec.getConfigurationSection("items").getKeys(false)) {
            String path = "items." + slotKey + ".";
            String name = ChatColor.stripColor(color(menuSec.getString(path + "name", "")));
            if (name.equalsIgnoreCase(displayName)) {
                return menuSec.getString(path + "action", null);
            }
        }
        return null;
    }

    public File getGuiDataFile() {
        return new File(plugin.getDataFolder(), "guis.yml");
    }
}
