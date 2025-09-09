package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIListener implements Listener {

    private final PlotManager plots;
    private final GUIManager gui;

    // anti-spam: 400ms between GUI opens from compass
    private final Map<UUID, Long> lastOpen = new HashMap<>();

    public GUIListener(PlotManager plots, GUIManager gui) {
        this.plots = plots;
        this.gui = gui;
    }

    /* -------------------- Compass right-click -> open GUIs -------------------- */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        ItemStack it = e.getItem();
        if (it.getType() != Material.COMPASS) return;
        ItemMeta meta = it.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        Player p = e.getPlayer();
        long now = System.currentTimeMillis();
        Long last = lastOpen.get(p.getUniqueId());
        if (last != null && (now - last) < 400) return; // tiny cooldown
        lastOpen.put(p.getUniqueId(), now);

        String name = meta.getDisplayName();
        if (name.equals(gui.getAdminCompassName())) {
            gui.openAdminGUI(p);
            e.setCancelled(true);
            return;
        }
        if (name.equals(gui.getPlayerCompassName())) {
            gui.openMainGUI(p);
            e.setCancelled(true);
        }
    }

    /* -------------------- Inventory buttons -------------------- */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getView() == null || e.getView().getTitle() == null) return;

        String title = ChatColor.stripColor(e.getView().getTitle());
        ItemStack current = e.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) return;

        String display = (current.hasItemMeta() && current.getItemMeta().hasDisplayName())
                ? ChatColor.stripColor(current.getItemMeta().getDisplayName()) : "";

        // MAIN
        if (ChatColor.stripColor(GUIManager.TITLE_MAIN).equals(title)) {
            e.setCancelled(true);
            switch (display) {
                case "Claim Chunk" -> player.performCommand("proshield claim");
                case "Claim Info" -> player.performCommand("proshield info");
                case "Unclaim" -> {
                    if (!plots.isOwner(player.getUniqueId(), player.getLocation())) {
                        player.sendMessage(ChatColor.RED + "Owner-only: you don't own this claim.");
                        return;
                    }
                    player.performCommand("proshield unclaim");
                }
                case "Trust Nearby" -> {
                    if (!plots.isOwner(player.getUniqueId(), player.getLocation())) {
                        player.sendMessage(ChatColor.RED + "Owner-only: you don't own this claim.");
                        return;
                    }
                    player.performCommand("proshield trustnear"); // your radius-based trust command
                }
                case "Untrust Player" -> {
                    if (!plots.isOwner(player.getUniqueId(), player.getLocation())) {
                        player.sendMessage(ChatColor.RED + "Owner-only: you don't own this claim.");
                        return;
                    }
                    player.performCommand("proshield untrust"); // expect follow-up by chat/anvil input
                }
                case "View Trusted" -> player.performCommand("proshield trusted");
                case "Manage Roles" -> {
                    if (!plots.isOwner(player.getUniqueId(), player.getLocation())) {
                        player.sendMessage(ChatColor.RED + "Owner-only: you don't own this claim.");
                        return;
                    }
                    player.performCommand("proshield roles"); // hook to your role GUI/flow if present
                }
                case "Transfer Ownership" -> {
                    if (!plots.isOwner(player.getUniqueId(), player.getLocation())) {
                        player.sendMessage(ChatColor.RED + "Owner-only: you don't own this claim.");
                        return;
                    }
                    player.performCommand("proshield transfer"); // expect target via next step
                }
                case "Borders Preview" -> player.performCommand("proshield preview 15");
                case "Keep Items" -> {
                    if (!plots.isOwner(player.getUniqueId(), player.getLocation())) {
                        player.sendMessage(ChatColor.RED + "Owner-only: you don't own this claim.");
                        return;
                    }
                    player.performCommand("proshield keepitems toggle");
                }
                case "Help" -> gui.openHelp(player);
                case "Admin Tools" -> gui.openAdminGUI(player);
                case "Back" -> player.closeInventory(); // from main, back just closes
            }
            return;
        }

        // ADMIN
        if (ChatColor.stripColor(GUIManager.TITLE_ADMIN).equals(title)) {
            e.setCancelled(true);
            switch (display) {
                case "Global Toggles" -> player.performCommand("proshield settings");
                case "Claim Expiry" -> player.performCommand("proshield purgeexpired 30 dryrun");
                case "Teleport Tools" -> player.performCommand("proshield admintp");
                case "Item Keep / Drops" -> player.performCommand("proshield keepitems toggle");
                case "Borders Preview" -> player.performCommand("proshield preview 15");
                case "Messages" -> player.performCommand("proshield messages");
                case "Admin Help" -> player.sendMessage(ChatColor.GOLD + "Admin: /proshield reload, purgeexpired, settings, admintp");
                case "Help" -> gui.openHelp(player);
                case "Back" -> gui.openMainGUI(player);
            }
            return;
        }

        // HELP
        if (ChatColor.stripColor(GUIManager.TITLE_HELP).equals(title)) {
            e.setCancelled(true);
            if ("Back".equals(display)) {
                gui.openMainGUI(player);
            }
        }
    }
}
