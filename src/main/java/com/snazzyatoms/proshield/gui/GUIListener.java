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

import java.util.Map;
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

        String menu = guiManager.isInMenu(player, "trusted") ? "trusted"
                    : guiManager.isInMenu(player, "flags") ? "flags"
                    : guiManager.isInMenu(player, "main") ? "main"
                    : null;

        if (menu == null) return;

        event.setCancelled(true); // Always block item dragging in GUIs
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
         * MAIN MENU ACTIONS
         * ========================= */
        if (menu.equals("main")) {
            switch (name) {
                case "§aClaim Land" -> plotManager.claimPlot(player);
                case "§cUnclaim Land" -> plotManager.unclaimPlot(player);
                case "§eClaim Info" -> plotManager.sendClaimInfo(player);
                case "§bTrusted Players" -> guiManager.openTrustedMenu(player);
                case "§eClaim Flags" -> guiManager.openFlagsMenu(player);
            }
        }

        /* =========================
         * FLAGS MENU ACTIONS
         * ========================= */
        if (menu.equals("flags")) {
            Plot plot = plotManager.getPlot(player.getLocation());
            if (plot == null) {
                player.sendMessage("§cNo claim here.");
                return;
            }

            String[] parts = name.replace("§f", "").split(":");
            if (parts.length == 2) {
                String flag = parts[0].trim();
                boolean state = name.contains("§aON");
                plot.setFlag(flag, !state);
                guiManager.openFlagsMenu(player); // Refresh
            }
        }

        /* =========================
         * TRUST MENU ACTIONS
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
                    player.sendMessage("§aTrusted " + targetName + " in your claim.");
                }
                guiManager.openTrustedMenu(player); // Refresh
            } else if (name.startsWith("§e")) {
                String targetName = name.substring(2).split(" ")[0];
                OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                if (target != null) {
                    plot.untrust(target.getUniqueId());
                    player.sendMessage("§cRemoved trust for " + target.getName());
                }
                guiManager.openTrustedMenu(player); // Refresh
            }
        }
    }
}
