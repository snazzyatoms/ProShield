package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class GUIManager {

    private final ProShield plugin;
    private final ClaimRoleManager roles;
    private final Map<UUID, String> pendingTargets = new HashMap<>();

    public GUIManager(ProShield plugin, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.roles = roles;
    }

    /**
     * Opens a menu dynamically from config.yml (gui.menus.<menuKey>).
     */
    public void openMenu(Player player, String menuKey) {
        String path = "gui.menus." + menuKey;
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString(path + ".title", "&cUnknown Menu"));

        Inventory inv = Bukkit.createInventory(null, 27, title);

        // Loop through slots defined in config
        if (plugin.getConfig().isConfigurationSection(path + ".items")) {
            for (String key : plugin.getConfig().getConfigurationSection(path + ".items").getKeys(false)) {
                int slot;
                try {
                    slot = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    continue;
                }

                String itemPath = path + ".items." + key;
                String materialName = plugin.getConfig().getString(itemPath + ".material", "BARRIER");
                Material mat = Material.matchMaterial(materialName.toUpperCase(Locale.ROOT));
                if (mat == null) mat = Material.BARRIER;

                String name = ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString(itemPath + ".name", "&cMissing Name"));

                List<String> lore = new ArrayList<>();
                for (String line : plugin.getConfig().getStringList(itemPath + ".lore")) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
                }

                boolean hideAttrs = plugin.getConfig().getBoolean(itemPath + ".hide-attributes", false);

                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(name);
                    meta.setLore(lore);
                    if (hideAttrs) meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

                    // Handle skull items
                    if (meta instanceof SkullMeta skullMeta) {
                        String owner = plugin.getConfig().getString(itemPath + ".owner");
                        if (owner != null) {
                            OfflinePlayer target = Bukkit.getOfflinePlayer(owner);
                            skullMeta.setOwningPlayer(target);
                        }
                        item.setItemMeta(skullMeta);
                    } else {
                        item.setItemMeta(meta);
                    }
                }

                inv.setItem(slot, item);
            }
        }

        player.openInventory(inv);
    }

    /* ====================================================
     * Role menus (special dynamic GUIs not in config)
     * ==================================================== */
    public void openRolesMenu(Player player, Plot plot) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Manage Roles");

        List<String> trusted = (plot != null)
                ? new ArrayList<>(plot.getTrustedNames())
                : Collections.emptyList();

        int slot = 10;
        if (!trusted.isEmpty()) {
            for (String name : trusted.stream().limit(15).toList()) {
                UUID claimId = plot.getId();
                UUID targetId = Bukkit.getOfflinePlayer(name).getUniqueId();
                ClaimRole role = roles.getRole(claimId, targetId);

                String roleName = (role != null) ? role.getDisplayName() : "Trusted";

                inv.setItem(slot++, makeSkull(name, ChatColor.YELLOW, name,
                        Arrays.asList(ChatColor.GRAY + "Click to manage roles",
                                ChatColor.YELLOW + "Current Role: " + roleName)));
                if (slot == 17) slot = 19;
            }
        } else {
            inv.setItem(13, makeMenuItem(Material.BARRIER, ChatColor.GRAY, "No Trusted Players",
                    Collections.singletonList(ChatColor.DARK_GRAY + "Trust someone first to manage roles"), false));
        }

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));

        player.openInventory(inv);
    }

    /* ====================================================
     * Helpers
     * ==================================================== */
    private ItemStack makeMenuItem(Material mat, ChatColor color, String name, List<String> lore, boolean hideAttrs) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color + name);
            meta.setLore(lore);
            if (hideAttrs) meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack makeSkull(String playerName, ChatColor color, String display, List<String> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color + display);
            meta.setLore(lore);
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
            meta.setOwningPlayer(target);
            skull.setItemMeta(meta);
        }
        return skull;
    }

    /* ====================================================
     * Utils
     * ==================================================== */
    public void rememberTarget(Player player, String targetName) {
        pendingTargets.put(player.getUniqueId(), targetName);
    }

    public String getRememberedTarget(Player player) {
        return pendingTargets.get(player.getUniqueId());
    }
}
