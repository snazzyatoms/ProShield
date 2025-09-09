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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GUIListener implements Listener {

    private final PlotManager plots;
    private final GUIManager gui;
    private final Map<UUID, Long> cooldown = new ConcurrentHashMap<>();

    public GUIListener(PlotManager plots, GUIManager gui) {
        this.plots = plots;
        this.gui = gui;
    }

    private boolean antiSpam(Player p) {
        long now = System.currentTimeMillis();
        long last = cooldown.getOrDefault(p.getUniqueId(), 0L);
        if (now - last < 250) return true; // 250ms GUI click cooldown
        cooldown.put(p.getUniqueId(), now);
        return false;
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
        if (antiSpam(e.getPlayer())) return;
        gui.openMain(e.getPlayer());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getView() == null || e.getCurrentItem() == null) return;
        String title = e.getView().getTitle();
        ItemStack it = e.getCurrentItem();

        // Main
        if (ChatColor.stripColor(title).equalsIgnoreCase(ChatColor.stripColor(GUIManager.TITLE))) {
            e.setCancelled(true);
            if (antiSpam(p)) return;

            int slot = e.getRawSlot();
            Location loc = p.getLocation();

            if (slot == 11) { // create
                boolean ok = plots.createClaim(p.getUniqueId(), loc);
                p.sendMessage(px() + (ok ?
                        ChatColor.GREEN + ProShield.getInstance().getConfig().getString("messages.claim-created") :
                        ChatColor.RED + ProShield.getInstance().getConfig().getString("messages.claim-exists")));
            } else if (slot == 13) { // info
                plots.getClaim(loc).ifPresentOrElse(c -> {
                    String owner = plots.ownerName(c.getOwner());
                    var trusted = plots.listTrusted(loc);
                    p.sendMessage(px() + ChatColor.AQUA + "Owner: " + owner);
                    p.sendMessage(px() + ChatColor.AQUA + "Trusted: " + (trusted.isEmpty() ? "(none)" : String.join(", ", trusted)));
                }, () -> p.sendMessage(px() + ChatColor.GRAY + ProShield.getInstance().getConfig().getString("messages.no-claim-here")));
            } else if (slot == 15) { // remove
                boolean ok = plots.removeClaim(p.getUniqueId(), loc, false);
                p.sendMessage(px() + (ok ?
                        ChatColor.GREEN + ProShield.getInstance().getConfig().getString("messages.claim-removed") :
                        ChatColor.RED + ProShield.getInstance().getConfig().getString("messages.no-claim-here")));
            } else if (slot == 22) { // help
                gui.openHelp(p);
            } else if (slot == 26) { // admin
                if (p.hasPermission("proshield.admin")) gui.openAdmin(p);
                else p.sendMessage(px() + ChatColor.RED + ProShield.getInstance().getConfig().getString("messages.no-permission"));
            }
            return;
        }

        // Help
        if (ChatColor.stripColor(title).equalsIgnoreCase("ProShield Help")) {
            e.setCancelled(true);
            if (antiSpam(p)) return;

            int slot = e.getRawSlot();
            if (slot == 26) { // Back
                gui.openMain(p);
            }
            return;
        }

        // Admin
        if (ChatColor.stripColor(title).equalsIgnoreCase(ChatColor.stripColor(GUIManager.TITLE_ADMIN))) {
            e.setCancelled(true);
            if (antiSpam(p)) return;

            int slot = e.getRawSlot();
            if (slot == 10) {
                // Preview via command to reuse logic
                p.performCommand("proshield preview 6");
            } else if (slot == 12) {
                p.sendMessage(px() + ChatColor.YELLOW + "Use /proshield transfer <player>");
            } else if (slot == 14) {
                p.sendMessage(px() + ChatColor.YELLOW + "Use /proshield purgeexpired <days> [dryrun]");
            } else if (slot == 16) {
                p.performCommand("proshield debug toggle");
            } else if (slot == 33) { // Back
                gui.openMain(p);
            }
            return;
        }
    }

    private String px() {
        return ChatColor.translateAlternateColorCodes('&',
                ProShield.getInstance().getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }
}
