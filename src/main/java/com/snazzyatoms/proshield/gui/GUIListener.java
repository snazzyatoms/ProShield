package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

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
                ProShield.getInstance().getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }

    // Compass use: right-click opens player GUI; sneak+right-click opens admin GUI (if permitted)
    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        ItemStack inHand = event.getItem();
        if (inHand == null || inHand.getType() != Material.COMPASS) return;
        if (inHand.getItemMeta() == null ||
            !ChatColor.stripColor(inHand.getItemMeta().getDisplayName()).equalsIgnoreCase("ProShield Compass")) return;

        event.setCancelled(true);

        Player p = event.getPlayer();
        if (p.isSneaking() && p.hasPermission("proshield.admin")) {
            guiManager.openAdminMenu(p);
        } else {
            guiManager.openMain(p);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        final String title = ChatColor.stripColor(event.getView().getTitle());
        final Player p = (Player) event.getWhoClicked();
        final Material clicked = event.getCurrentItem().getType();

        // lock items
        event.setCancelled(true);

        if (title.equals(ChatColor.stripColor(GUIManager.MAIN_TITLE))) {
            Location loc = p.getLocation();
            switch (clicked) {
                case GRASS_BLOCK -> {
                    if (plotManager.createClaim(p.getUniqueId(), loc)) {
                        p.sendMessage(prefix() + ChatColor.GREEN + "Chunk claimed.");
                    } else {
                        p.sendMessage(prefix() + ChatColor.RED + "This chunk is already claimed or you reached your limit.");
                    }
                    p.closeInventory();
                }
                case PAPER -> {
                    plotManager.getClaim(loc).ifPresentOrElse(c -> {
                        p.sendMessage(prefix() + ChatColor.AQUA + "Owner: " + plotManager.ownerName(c.getOwner()));
                        p.sendMessage(prefix() + ChatColor.AQUA + "Chunk: " + c.getWorld() + " " + c.getChunkX() + "," + c.getChunkZ());
                        var trusted = plotManager.listTrusted(loc);
                        p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + (trusted.isEmpty() ? "None" : String.join(", ", trusted)));
                    }, () -> p.sendMessage(prefix() + ChatColor.GRAY + "This chunk is unclaimed."));
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
                default -> {}
            }
            return;
        }

        if (title.equals(ChatColor.stripColor(GUIManager.ADMIN_TITLE))) {
            if (!p.hasPermission("proshield.admin")) {
                p.sendMessage(prefix() + ChatColor.RED + "No permission.");
                p.closeInventory();
                return;
            }
            switch (clicked) {
                case LEVER -> { // bypass toggle
                    boolean newBypass = !p.hasMetadata("proshield_bypass");
                    if (newBypass) p.setMetadata("proshield_bypass", new FixedMetadataValue(ProShield.getInstance(), true));
                    else p.removeMetadata("proshield_bypass", ProShield.getInstance());
                    p.sendMessage(prefix() + (newBypass ? ChatColor.GREEN + "Bypass enabled." : ChatColor.RED + "Bypass disabled."));
                    p.closeInventory();
                }
                case COMPASS -> {
                    p.getInventory().addItem(GUIManager.createAdminCompass());
                    p.sendMessage(prefix() + ChatColor.GREEN + "Admin compass added.");
                    p.closeInventory();
                }
                case ENDER_PEARL -> {
                    if (!p.hasPermission("proshield.admin.tp")) {
                        p.sendMessage(prefix() + ChatColor.RED + "No permission.");
                        p.closeInventory();
                        return;
                    }
                    List<String> keys = new ArrayList<>(plotManager.getAllClaimKeys());
                    Location ploc = p.getLocation();
                    keys.sort(Comparator.comparingDouble(k -> plotManager.keyToCenter(k).distanceSquared(ploc)));
                    if (keys.isEmpty()) {
                        p.sendMessage(prefix() + ChatColor.GRAY + "No claims found.");
                        p.closeInventory();
                    } else {
                        int max = Math.min(54, keys.size());
                        guiManager.openAdminClaims(p, keys.subList(0, max));
                    }
                }
                case BARRIER -> { // Force unclaim here
                    Location here = p.getLocation();
                    boolean ok = plotManager.removeClaim(p.getUniqueId(), here, true);
                    p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Force-unclaimed this chunk." : ChatColor.GRAY + "No claim here."));
                    p.closeInventory();
                }
                case REPEATER -> {
                    if (!p.hasPermission("proshield.admin.reload")) {
                        p.sendMessage(prefix() + ChatColor.RED + "No permission.");
                        p.closeInventory();
                        return;
                    }
                    ProShield.getInstance().reloadAllConfigs();
                    p.sendMessage(prefix() + ChatColor.GREEN + "Configs reloaded.");
                    p.closeInventory();
                }
                default -> {}
            }
            return;
        }

        if (title.equals(ChatColor.stripColor(GUIManager.CLAIMS_TITLE))) {
            if (!p.hasPermission("proshield.admin.tp")) return;
            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;
            String key = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            var center = plotManager.keyToCenter(key);
            if (center != null) p.teleport(center.add(0.5, 1, 0.5));
            p.closeInventory();
        }
    }
}
