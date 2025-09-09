// path: src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final PlotManager plotManager;
    private final GUIManager guiManager;

    public GUIListener(PlotManager plotManager, GUIManager guiManager) {
        this.plotManager = plotManager;
        this.guiManager  = guiManager;
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        ItemStack it = e.getItem();
        if (it == null || it.getType() != Material.COMPASS) return;
        if (!it.hasItemMeta() || !it.getItemMeta().hasDisplayName()) return;
        String name = ChatColor.stripColor(it.getItemMeta().getDisplayName());
        if (!"ProShield Compass".equalsIgnoreCase(name)) return;

        e.setCancelled(true);
        guiManager.openMain(e.getPlayer());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getView() == null || e.getCurrentItem() == null) return;

        String title = e.getView().getTitle();
        ItemStack clicked = e.getCurrentItem();

        // --- Player Main GUI ---
        if (GUIManager.TITLE.equals(title)) {
            e.setCancelled(true);
            int slot = e.getRawSlot();
            Location loc = p.getLocation();

            if (slot == 11) { // Claim
                boolean ok = plotManager.createClaim(p.getUniqueId(), loc);
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Claim created for this chunk."
                        : ChatColor.RED + "This chunk is already claimed or you reached your limit."));
            } else if (slot == 13) { // Info
                plotManager.getClaim(loc).ifPresentOrElse(c -> {
                    String owner = plotManager.ownerName(c.getOwner());
                    var trusted = plotManager.listTrusted(loc);
                    p.sendMessage(prefix() + ChatColor.AQUA + "Owner: " + owner);
                    p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + (trusted.isEmpty() ? "(none)" : String.join(", ", trusted)));
                }, () -> p.sendMessage(prefix() + ChatColor.GRAY + "No claim in this chunk."));
            } else if (slot == 15) { // Unclaim
                boolean ok = plotManager.removeClaim(p.getUniqueId(), loc, false);
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Claim removed."
                        : ChatColor.RED + "No claim here or you are not the owner."));
            } else if (slot == (31 - 27)) { // Help
                // show context-aware help (simplified)
                p.sendMessage(prefix() + ChatColor.GOLD + "Commands you can use:");
                if (p.hasPermission("proshield.use")) {
                    p.sendMessage(ChatColor.GRAY + " /proshield claim, /proshield unclaim, /proshield info");
                    p.sendMessage(ChatColor.GRAY + " /proshield trust <player> [role], /proshield untrust <player>");
                    p.sendMessage(ChatColor.GRAY + " /proshield trusted, /proshield compass");
                }
                if (p.hasPermission("proshield.admin")) {
                    p.sendMessage(ChatColor.DARK_GRAY + " Admin: /proshield reload, /proshield purgeexpired");
                }
            } else if (slot == (33 - 27)) { // Admin button
                if (p.hasPermission("proshield.admin")) {
                    guiManager.openAdmin(p);
                } else {
                    p.sendMessage(prefix() + ChatColor.RED + "You do not have permission.");
                }
            }
            return;
        }

        // --- Admin GUI ---
        if (GUIManager.ADMIN_TITLE.equals(title)) {
            e.setCancelled(true);
            int slot = e.getRawSlot();

            switch (slot) {
                case 11 -> { // Keep Items toggle
                    if (p.hasPermission("proshield.admin.keepitems")) guiManager.toggleKeepItems(p); else deny(p);
                }
                case 12 -> { // Fire
                    if (p.hasPermission("proshield.admin")) guiManager.toggleFire(p); else deny(p);
                }
                case 13 -> { // Explosions
                    if (p.hasPermission("proshield.admin")) guiManager.toggleExplosions(p); else deny(p);
                }
                case 14 -> { // Mob grief
                    if (p.hasPermission("proshield.admin")) guiManager.toggleMobGrief(p); else deny(p);
                }
                case 15 -> { // Interactions / Redstone
                    if (p.hasPermission("proshield.admin")) guiManager.toggleRedstone(p); else deny(p);
                }
                case 16 -> { // PvP allowed
                    if (p.hasPermission("proshield.admin")) guiManager.togglePvPAllowed(p); else deny(p);
                }
                case 23 -> { // Expiry preview
                    if (p.hasPermission("proshield.admin")) guiManager.runExpiryPreview(p); else deny(p);
                }
                case 24 -> { // Reload
                    if (p.hasPermission("proshield.admin.reload") || p.hasPermission("proshield.admin")) guiManager.reloadPlugin(p); else deny(p);
                }
                case 30 -> { // Debug
                    if (p.hasPermission("proshield.admin")) guiManager.toggleDebug(p); else deny(p);
                }
                case 48 -> { // Back
                    guiManager.openMain(p);
                }
                case 50 -> { // Admin help
                    guiManager.showAdminHelp(p);
                }
                default -> {
                    // “coming soon” placeholders for other buttons
                    if (slot == 19 || slot == 20 || slot == 21 || slot == 22 || slot == 28 || slot == 29 || slot == 31 || slot == 32 || slot == 33) {
                        p.sendMessage(prefix() + ChatColor.GRAY + "This tool is coming soon.");
                    }
                }
            }
        }
    }

    private void deny(Player p) {
        p.sendMessage(prefix() + ChatColor.RED + "You do not have permission.");
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                com.snazzyatoms.proshield.ProShield.getInstance().getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }
}
