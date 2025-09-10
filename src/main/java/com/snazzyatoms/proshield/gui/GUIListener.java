package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;
    private final PlotManager plots;

    public GUIListener(ProShield plugin, GUIManager gui, PlotManager plots) {
        this.plugin = plugin;
        this.gui = gui;
        this.plots = plots;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        Inventory inv = e.getInventory();
        ItemStack clicked = e.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String title = e.getView().getTitle();
        e.setCancelled(true); // Prevent item dragging

        Plot plot = plots.getPlotAt(player.getLocation());

        // === Main Menu ===
        if (title.equals("🛡️ ProShield Menu")) {
            switch (clicked.getItemMeta().getDisplayName()) {
                case "§aClaim Chunk" -> plots.claimPlot(player);
                case "§cUnclaim" -> plots.unclaimPlot(player);
                case "§bClaim Info" -> plots.sendClaimInfo(player);
                case "§eTrust Player" -> gui.openTrustMenu(player, plot);
                case "§eUntrust Player" -> gui.openUntrustMenu(player, plot);
                case "§6Role Manager" -> gui.openRoleMenu(player, plot);
                case "§6Flags" -> gui.openFlagMenu(player, plot);
                case "§dTransfer Claim" -> gui.openTransferMenu(player, plot);
                case "§bHelp" -> player.performCommand("proshield help");
                case "§cAdmin Menu" -> gui.openAdmin(player);
            }
            return;
        }

        // === Trust Menu ===
        if (title.equals("👥 Trust Players")) {
            if (clicked.getItemMeta().getDisplayName().equals("§7Back")) {
                gui.openMain(player);
            }
            // Future: player selection GUI integration
            return;
        }

        // === Untrust Menu ===
        if (title.equals("🚫 Untrust Players")) {
            if (clicked.getItemMeta().getDisplayName().equals("§7Back")) {
                gui.openMain(player);
            }
            return;
        }

        // === Role Menu ===
        if (title.equals("⚙️ Role Manager")) {
            switch (clicked.getItemMeta().getDisplayName()) {
                case "§7Visitor" -> plugin.getRoleManager().setRole(player, plot, "Visitor");
                case "§aMember" -> plugin.getRoleManager().setRole(player, plot, "Member");
                case "§6Container" -> plugin.getRoleManager().setRole(player, plot, "Container");
                case "§bBuilder" -> plugin.getRoleManager().setRole(player, plot, "Builder");
                case "§dCo-Owner" -> plugin.getRoleManager().setRole(player, plot, "Co-Owner");
                case "§7Back" -> gui.openMain(player);
            }
            return;
        }

        // === Flag Menu ===
        if (title.equals("🚩 Claim Flags")) {
            switch (clicked.getItemMeta().getDisplayName()) {
                case "§cPvP" -> plots.toggleFlag(plot, "pvp");
                case "§cExplosions" -> plots.toggleFlag(plot, "explosions");
                case "§cFire" -> plots.toggleFlag(plot, "fire");
                case "§cMob Grief" -> plots.toggleFlag(plot, "mob-grief");
                case "§7Back" -> gui.openMain(player);
            }
            return;
        }

        // === Transfer Menu ===
        if (title.equals("📦 Transfer Ownership")) {
            if (clicked.getItemMeta().getDisplayName().equals("§dTransfer")) {
                player.sendMessage("§eUse /proshield transfer <player> to complete ownership transfer.");
            } else if (clicked.getItemMeta().getDisplayName().equals("§7Back")) {
                gui.openMain(player);
            }
            return;
        }

        // === Admin Menu ===
        if (title.equals("⚙️ ProShield Admin")) {
            switch (clicked.getItemMeta().getDisplayName()) {
                case "§cToggle Fire" -> plots.toggleGlobal("fire");
                case "§cToggle Explosions" -> plots.toggleGlobal("explosions");
                case "§cToggle Entity Grief" -> plots.toggleGlobal("entity-grief");
                case "§cToggle Interactions" -> plots.toggleGlobal("interactions");
                case "§cToggle PvP" -> plots.toggleGlobal("pvp");
                case "§6Keep Items" -> plots.toggleGlobal("keep-items");
                case "§cPurge Expired" -> player.performCommand("proshield purgeexpired 30");
                case "§eHelp" -> player.performCommand("proshield help");
                case "§cDebug Mode" -> player.performCommand("proshield debug toggle");
                case "§aCompass Drop" -> plots.toggleGlobal("compass-drop");
                case "§bReload Config" -> player.performCommand("proshield reload");
                case "§dTeleport Tools" -> player.performCommand("proshield tp tools");
                case "§7Back" -> gui.openMain(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        // Optional: cleanup or refresh GUICache per-player
        if (e.getPlayer() instanceof Player player) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                gui.onConfigReload(); // ensures menus refresh after config reloads
            }, 1L);
        }
    }
}
