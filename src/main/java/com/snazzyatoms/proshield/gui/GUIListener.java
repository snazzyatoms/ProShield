package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final PlotManager plotManager;

    public GUIListener(ProShield plugin, GUIManager guiManager, PlotManager plotManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        // Which menu are we in?
        String menu = guiManager.getOpenMenu(player);
        if (menu == null) return;

        event.setCancelled(true); // Always block item movement
        ItemStack clicked = event.getCurrentItem();
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;
        String name = meta.getDisplayName();

        /* =========================
         * GLOBAL BUTTONS
         * ========================= */
        if (name.equalsIgnoreCase("§cExit")) {
            guiManager.close(player);
            return;
        }
        if (name.equalsIgnoreCase("§cBack")) {
            guiManager.openMenu(player, "main");
            return;
        }

        /* =========================
         * MAIN MENU
         * ========================= */
        if (menu.equals("main")) {
            switch (name) {
                case "§aClaim Land" -> plotManager.claimPlot(player);
                case "§cUnclaim Land" -> plotManager.unclaimPlot(player);
                case "§eClaim Info" -> plotManager.sendClaimInfo(player);
                case "§bTrusted Players" -> guiManager.openTrustedMenu(player);
                case "§eClaim Flags" -> guiManager.openFlagsMenu(player);
                case "§cAdmin Tools" -> {
                    if (player.isOp() || player.hasPermission("proshield.admin"))
                        guiManager.openMenu(player, "admin-tools");
                }
            }
        }

        /* =========================
         * FLAGS MENU
         * ========================= */
        if (menu.equals("flags")) {
            Plot plot = plotManager.getPlot(player.getLocation());
            if (plot == null) {
                player.sendMessage("§cNo claim here.");
                return;
            }

            String flagKey = guiManager.resolveFlagKey(name);
            if (flagKey != null) {
                boolean current = plot.getFlag(flagKey, false);
                plot.setFlag(flagKey, !current);
                player.sendMessage("§eFlag " + flagKey + " set to " + !current);
                guiManager.openFlagsMenu(player); // Refresh
            }
        }

        /* =========================
         * TRUSTED MENU
         * ========================= */
        if (menu.equals("trusted")) {
            Plot plot = plotManager.getPlot(player.getLocation());
            if (plot == null) {
                player.sendMessage("§cNo claim here.");
                return;
            }

            if (name.startsWith("§aTrust ")) {
                String targetName = name.replace("§aTrust ", "");
                Player target = Bukkit.getPlayerExact(targetName);
                if (target != null) {
                    plot.trust(target.getUniqueId(), "trusted");
                    player.sendMessage("§aTrusted " + targetName);
                }
                guiManager.openTrustedMenu(player);
            } else if (name.startsWith("§e")) {
                String targetName = name.substring(2).split(" ")[0];
                OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                if (target != null) {
                    plot.untrust(target.getUniqueId());
                    player.sendMessage("§cRemoved trust for " + target.getName());
                }
                guiManager.openTrustedMenu(player);
            }
        }

        /* =========================
         * ADMIN TOOLS
         * ========================= */
        if (menu.equals("admin-tools") && (player.isOp() || player.hasPermission("proshield.admin"))) {
            switch (name) {
                case "§bReload Config" -> {
                    plugin.reloadConfig();
                    player.sendMessage("§aConfig reloaded.");
                }
                case "§dWorld Controls" -> guiManager.openMenu(player, "world-controls");
                case "§eToggle Debug" -> {
                    plugin.setDebugEnabled(!plugin.isDebugEnabled());
                    player.sendMessage("§aDebug toggled: " + plugin.isDebugEnabled());
                }
                case "§6Toggle Bypass (You)" -> {
                    boolean now = plugin.toggleBypass(player.getUniqueId());
                    player.sendMessage("§eBypass " + (now ? "enabled" : "disabled"));
                }
                case "§ePending Requests" -> guiManager.openMenu(player, "expansion-requests");
                case "§aApprove Selected" -> guiManager.handleExpansionApproval(player, true);
                case "§cDeny Selected" -> guiManager.handleExpansionApproval(player, false);
            }
        }

        /* =========================
         * WORLD CONTROLS
         * ========================= */
        if (menu.equals("world-controls") && (player.isOp() || player.hasPermission("proshield.admin.worldcontrols"))) {
            String flagKey = guiManager.resolveWorldFlagKey(name);
            if (flagKey != null) {
                boolean state = plugin.getWorldControlManager().toggleFlag(player.getWorld().getName(), flagKey);
                player.sendMessage("§eWorld flag " + flagKey + " set to " + state);
                guiManager.openMenu(player, "world-controls");
            }
        }
    }
}
