package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    private final NamespacedKey menuKeyTag;
    private final NamespacedKey targetNameTag;

    private static final Map<String, String> FLAG_DESCRIPTIONS = new HashMap<>();
    static {
        FLAG_DESCRIPTIONS.put("explosions", "&7Toggle TNT and creeper damage inside claim");
        FLAG_DESCRIPTIONS.put("buckets", "&7Allow/disallow bucket use (water & lava)");
        FLAG_DESCRIPTIONS.put("item-frames", "&7Protect item frames from breaking/rotation");
        FLAG_DESCRIPTIONS.put("armor-stands", "&7Prevent others moving/destroying armor stands");
        FLAG_DESCRIPTIONS.put("containers", "&7Control access to chests, hoppers, furnaces, shulkers");
        FLAG_DESCRIPTIONS.put("pets", "&7Prevent damage to tamed pets (wolves, cats, etc.)");
        FLAG_DESCRIPTIONS.put("pvp", "&7Enable or disable PvP combat inside claim");
        FLAG_DESCRIPTIONS.put("safezone", "&7Turns your claim into a safe zone (blocks hostile spawns & damage)");
    }

    private static final Map<String, String> ROLE_PERMISSIONS = new LinkedHashMap<>();
    static {
        ROLE_PERMISSIONS.put("build", "&7Allow building & block breaking");
        ROLE_PERMISSIONS.put("interact", "&7Allow doors, buttons, crops, redstone use");
        ROLE_PERMISSIONS.put("containers", "&7Allow access to chests, hoppers, furnaces");
        ROLE_PERMISSIONS.put("vehicles", "&7Allow using minecarts & boats");
        ROLE_PERMISSIONS.put("unclaim", "&7Allow unclaiming this land");
    }

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.roleManager = plugin.getRoleManager();

        this.menuKeyTag = new NamespacedKey(plugin, "menuKey");
        this.targetNameTag = new NamespacedKey(plugin, "targetName");
    }

    public void openMenu(Player player, String menuKey) {
        ConfigurationSection menus = plugin.getConfig().getConfigurationSection("gui.menus");
        if (menus == null) return;

        ConfigurationSection menu = menus.getConfigurationSection(menuKey);
        if (menu == null) return;

        String title = ChatColor.translateAlternateColorCodes('&', menu.getString("title", "&7Menu"));
        int size = Math.max(9, menu.getInt("size", 27));
        if (size % 9 != 0) size = 27;

        Inventory inv = Bukkit.createInventory(null, size, title);

        switch (menuKey.toLowerCase(Locale.ROOT)) {
            case "trust" -> buildTrustMenu(player, inv);
            case "untrust" -> buildUntrustMenu(player, inv);
            default -> buildStaticMenu(player, menu, inv, size);
        }

        player.openInventory(inv);
        playClick(player);
    }

    public void openRoleEditor(Player owner, String targetName) {
        Plot plot = plotManager.getPlot(owner.getLocation());
        if (plot == null) {
            owner.sendMessage(ChatColor.RED + "You are not standing inside one of your claims.");
            return;
        }

        UUID plotId = plot.getId();
        Map<String, Boolean> perms = roleManager.getPermissions(plotId, targetName);
        String role = roleManager.getRole(plotId, targetName);

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_AQUA + "Role Editor: " + targetName);

        int slot = 0;
        for (Map.Entry<String, String> entry : ROLE_PERMISSIONS.entrySet()) {
            String key = entry.getKey();
            String desc = entry.getValue();
            boolean enabled = perms.getOrDefault(key, false);

            ItemStack item = new ItemStack(enabled ? Material.LIME_DYE : Material.GRAY_DYE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + key.toUpperCase());
                meta.setLore(Arrays.asList(
                    ChatColor.translateAlternateColorCodes('&', desc),
                    ChatColor.GRAY + "Current: " + (enabled ? ChatColor.GREEN + "Allowed" : ChatColor.RED + "Denied"),
                    ChatColor.AQUA + "Click to toggle"
                ));
                meta.getPersistentDataContainer().set(targetNameTag, PersistentDataType.STRING, targetName);
                meta.getPersistentDataContainer().set(menuKeyTag, PersistentDataType.STRING, "role-editor");
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        ItemStack roleItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta roleMeta = roleItem.getItemMeta();
        if (roleMeta != null) {
            roleMeta.setDisplayName(ChatColor.GOLD + "Role: " + (role == null ? "None" : role));
            roleMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Trusted player: " + targetName,
                ChatColor.YELLOW + "Role defines defaults",
                ChatColor.AQUA + "Custom toggles override defaults"
            ));
            roleMeta.getPersistentDataContainer().set(targetNameTag, PersistentDataType.STRING, targetName);
            roleMeta.getPersistentDataContainer().set(menuKeyTag, PersistentDataType.STRING, "role-editor");
            roleItem.setItemMeta(roleMeta);
        }
        inv.setItem(22, roleItem);

        // Back + Close
        inv.setItem(25, createNavItem(Material.ARROW, ChatColor.YELLOW + "Back", "Return to Untrust Menu", "role-editor", targetName));
        inv.setItem(26, createNavItem(Material.BARRIER, ChatColor.RED + "Close", "Close this menu", "role-editor", targetName));

        owner.openInventory(inv);
        playClick(owner);
    }

    private void buildTrustMenu(Player player, Inventory inv) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) return;

        int slot = 0;
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby.equals(player)) continue;
            if (nearby.getLocation().distance(player.getLocation()) > 10) continue;

            ItemStack head = createNavItem(Material.PLAYER_HEAD,
                ChatColor.GREEN + nearby.getName(),
                "Click to trust this player",
                "trust",
                nearby.getName()
            );
            inv.setItem(slot++, head);
            if (slot >= inv.getSize() - 2) break;
        }

        // Back button (slot 26 if available)
        inv.setItem(inv.getSize() - 1, createNavItem(Material.BARRIER, ChatColor.RED + "Back", "Return to main menu", "static:trust", null));
    }

    private void buildUntrustMenu(Player player, Inventory inv) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) return;

        Map<String, String> trusted = roleManager.getTrusted(plot.getId());
        int slot = 0;
        for (String name : trusted.keySet()) {
            ItemStack head = createNavItem(Material.PLAYER_HEAD,
                ChatColor.RED + name,
                "Click to manage/untrust",
                "untrust",
                name
            );
            inv.setItem(slot++, head);
            if (slot >= inv.getSize() - 2) break;
        }

        // Back button
        inv.setItem(inv.getSize() - 1, createNavItem(Material.BARRIER, ChatColor.RED + "Back", "Return to main menu", "static:untrust", null));
    }

    private void buildStaticMenu(Player player, ConfigurationSection menu, Inventory inv, int size) {
        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items == null) return;

        for (String slotKey : items.getKeys(false)) {
            try {
                int slot = Integer.parseInt(slotKey);
                if (slot < 0 || slot >= size) continue;

                ConfigurationSection itemSec = items.getConfigurationSection(slotKey);
                if (itemSec == null) continue;

                String perm = itemSec.getString("permission");
                if (perm != null && !perm.isBlank() &&
                    !player.hasPermission(perm) && !player.isOp()) {
                    continue;
                }

                Material mat = Material.matchMaterial(itemSec.getString("material", "BARRIER"));
                if (mat == null) mat = Material.BARRIER;

                String name = ChatColor.translateAlternateColorCodes('&', itemSec.getString("name", ""));
                List<String> lore = formatLore(itemSec.getStringList("lore"), player, itemSec);

                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(name);
                    meta.setLore(lore);
                    meta.getPersistentDataContainer().set(menuKeyTag, PersistentDataType.STRING, "static:" + menu.getName());
                    item.setItemMeta(meta);
                }

                inv.setItem(slot, item);
            } catch (NumberFormatException ignored) {}
        }
    }

    private List<String> formatLore(List<String> input, Player player, ConfigurationSection itemSec) {
        List<String> out = new ArrayList<>();
        String action = itemSec.getString("action", "");
        String state = "";

        if (action.toLowerCase().startsWith("command:proshield flag ")) {
            String[] split = action.split(" ");
            if (split.length >= 3) {
                String flagKey = split[2].toLowerCase();
                Plot plot = plotManager.getPlot(player.getLocation());
                boolean flagValue = plot != null
                    ? plot.getFlag(flagKey, plugin.getConfig().getBoolean("claims.default-flags." + flagKey, false))
                    : plugin.getConfig().getBoolean("claims.default-flags." + flagKey, false);
                state = flagValue ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";
            }
        }

        for (String line : input) {
            out.add(ChatColor.translateAlternateColorCodes('&', line.replace("{state}", state)));
        }
        return out;
    }

    private ItemStack createNavItem(Material mat, String name, String loreLine, String menuTagValue, String targetName) {
        return createNavItem(mat, name, loreLine == null ? null : new String[]{loreLine}, menuTagValue, targetName);
    }

    private ItemStack createNavItem(Material mat, String name, String loreLine1, String loreLine2, String menuTagValue, String targetName) {
        return createNavItem(mat, name, new String[]{loreLine1, loreLine2}, menuTagValue, targetName);
    }

    private ItemStack createNavItem(Material mat, String name, String[] loreLines, String menuTagValue, String targetName) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (loreLines != null) {
                List<String> lore = new ArrayList<>();
                for (String l : loreLines) if (l != null) lore.add(ChatColor.GRAY + l);
                meta.setLore(lore);
            }
            if (menuTagValue != null) {
                meta.getPersistentDataContainer().set(menuKeyTag, PersistentDataType.STRING, menuTagValue);
            }
            if (targetName != null) {
                meta.getPersistentDataContainer().set(targetNameTag, PersistentDataType.STRING, targetName);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private void playClick(Player player) {
        String sound = plugin.getConfig().getString("sounds.button-click", "UI_BUTTON_CLICK");
        try { player.playSound(player.getLocation(), sound, 1f, 1f); } catch (Exception ignored) {}
    }
}
