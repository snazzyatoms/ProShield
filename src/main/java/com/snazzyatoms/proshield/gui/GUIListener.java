// path: src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GUIListener implements Listener {

    private final PlotManager plotManager;
    private final GUIManager guiManager;

    public GUIListener(PlotManager plotManager, GUIManager guiManager) {
        this.plotManager = plotManager;
        this.guiManager = guiManager;
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                ProShield.getInstance().getAdminConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }

    // Right-click compass: open player menu, or admin menu if sneaking and allowed
    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        ItemStack inHand = event.getItem();
        if (inHand == null || inHand.getType() != Material.COMPASS) return;
        if (inHand.getItemMeta() == null || !ChatColor.stripColor(inHand.getItemMeta().getDisplayName()).equalsIgnoreCase("ProShield Compass"))
            return;

        event.setCancelled(true);

        Player p = event.getPlayer();
        FileConfiguration ac = ProShield.getInstance().getAdminConfig();
        boolean sneakingAdmin = ac.getBoolean("admin-menu.open-when-sneaking-with-compass", true);

        if (sneakingAdmin && p.isSneaking() && p.hasPermission("proshield.admin")) {
            guiManager.openAdminMenu(p);
        } else {
            guiManager.openMain(p);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (inv == null || event.getCurrentItem() == null) return;

        String title = ChatColor.stripColor(inv.getTitle());
        Player p = (Player) event.getWhoClicked();
        Material clicked = event.getCurrentItem().getType();
        event.setCancelled(true);

        // Player main menu
        if (title.equals(ChatColor.stripColor(GUIManager.MAIN_TITLE))) {
            Location loc = p.getLocation();
            switch (clicked) {
                case GRASS_BLOCK -> {
                    if (plotManager.createClaim(p.getUniqueId(), loc)) {
                        p.sendMessage(prefix() + ChatColor.GREEN + "Chunk claimed.");
                    } else {
                        p.sendMessage(prefix() + ChatColor.RED + "This chunk is already claimed.");
                    }
                    p.closeInventory();
                }
                case BARRIER -> {
                    if (plotManager.removeClaim(p.getUniqueId(), loc)) {
                        p.sendMessage(prefix() + ChatColor.GREEN + "Chunk unclaimed.");
                    } else {
                        p.sendMessage(prefix() + ChatColor.RED + "You do not own this chunk.");
                    }
                    p.closeInventory();
                }
                default -> { /* ignore */ }
            }
            return;
        }

        // Admin menu
        if (title.equals(ChatColor.stripColor(GUIManager.ADMIN_TITLE))) {
            if (!p.hasPermission("proshield.admin")) {
                p.sendMessage(prefix() + ChatColor.RED + ProShield.getInstance().getAdminConfig().getString("messages.no-permission", "No permission."));
                p.closeInventory();
                return;
            }
            switch (clicked) {
                case LEVER -> {
                    // simple bypass toggle via metadata on player (lightweight)
                    boolean newBypass = !p.hasMetadata("proshield_bypass");
                    if (newBypass) p.setMetadata("proshield_bypass", new org.bukkit.metadata.FixedMetadataValue(ProShield.getInstance(), true));
                    else p.removeMetadata("proshield_bypass", ProShield.getInstance());
                    String msgKey = newBypass ? "messages.bypass-on" : "messages.bypass-off";
                    p.sendMessage(prefix() + ChatColor.translateAlternateColorCodes('&', ProShield.getInstance().getAdminConfig().getString(msgKey, newBypass ? "&aBypass enabled" : "&cBypass disabled")));
                    p.closeInventory();
                }
                case COMPASS -> {
                    p.getInventory().addItem(GUIManager.createAdminCompass());
                    p.sendMessage(prefix() + ChatColor.GREEN + ProShield.getInstance().getAdminConfig().getString("messages.compass-given", "Admin compass added."));
                    p.closeInventory();
                }
                case ENDER_PEARL -> {
                    if (!p.hasPermission("proshield.admin.tp")) {
                        p.sendMessage(prefix() + ChatColor.RED + ProShield.getInstance().getAdminConfig().getString("messages.no-permission", "No permission."));
                        p.closeInventory();
                        return;
                    }
                    if (!ProShield.getInstance().getAdminConfig().getBoolean("teleport.enabled", true)) {
                        p.sendMessage(prefix() + ChatColor.RED + ProShield.getInstance().getAdminConfig().getString("messages.tp-disabled", "TP disabled."));
                        p.closeInventory();
                        return;
                    }
                    // sort nearby claims by distance to player
                    List<String> keys = new ArrayList<>(plotManager.getAllClaimKeys());
                    Location ploc = p.getLocation();
                    keys.sort(Comparator.comparingDouble(k -> plotManager.keyToCenter(k).distanceSquared(ploc)));
                    int max = ProShield.getInstance().getAdminConfig().getInt("teleport.max-list", 27);
                    if (keys.isEmpty()) {
                        p.sendMessage(prefix() + ChatColor.GRAY + ProShield.getInstance().getAdminConfig().getString("messages.no-claims", "No claims found."));
                        p.closeInventory();
                    } else {
                        guiManager.openAdminClaims(p, keys.subList(0, Math.min(max, keys.size())));
                    }
                }
                case REPEATER -> {
                    if (!p.hasPermission("proshield.admin.reload")) {
                        p.sendMessage(prefix() + ChatColor.RED + ProShield.getInstance().getAdminConfig().getString("messages.no-permission", "No permission."));
                        p.closeInventory();
                        return;
                    }
                    ProShield.getInstance().reloadAllConfigs();
                    p.sendMessage(prefix() + ChatColor.GREEN + ProShield.getInstance().getAdminConfig().getString("messages.reloaded", "Configs reloaded."));
                    p.closeInventory();
                }
                default -> { /* ignore */ }
            }
            return;
        }

        // Admin claims list
        if (title.equals(ChatColor.stripColor(GUIManager.CLAIMS_TITLE))) {
            if (!p.hasPermission("proshield.admin.tp")) return;
            String key = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            Location center = plotManager.keyToCenter(key);
            if (center != null) p.teleport(center.add(0.5, 1, 0.5));
            p.closeInventory();
        }
    }
}
