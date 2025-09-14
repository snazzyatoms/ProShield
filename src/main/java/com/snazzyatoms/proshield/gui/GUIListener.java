package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.expansion.ExpansionRequestManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;
import java.util.UUID;

public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.plotManager = plugin.getPlotManager();
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        event.setCancelled(true); // prevent taking items out of GUI

        String name = ChatColor.stripColor(meta.getDisplayName());

        // ------------------------
        // MAIN MENU
        // ------------------------
        if (title.contains("ProShield Menu")) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case "claim land" -> player.performCommand("claim");
                case "claim info" -> player.performCommand("proshield info");
                case "unclaim land" -> player.performCommand("unclaim");
                case "trusted players" -> guiManager.openMenu(player, "roles");
                case "claim flags" -> guiManager.openMenu(player, "flags");
                case "admin tools" -> {
                    if (player.isOp()) {
                        guiManager.openMenu(player, "admin-expansions");
                    } else {
                        messages.send(player, "&cYou don’t have permission.");
                    }
                }
            }
        }

        // ------------------------
        // FLAGS MENU
        // ------------------------
        if (title.contains("Claim Flags")) {
            Plot plot = plotManager.getPlot(player.getLocation());
            if (plot == null) {
                messages.send(player, "&cYou must stand inside a claim.");
                return;
            }

            switch (name.toLowerCase(Locale.ROOT)) {
                case "explosions" -> toggleFlag(plot, "explosions", player);
                case "buckets" -> toggleFlag(plot, "buckets", player);
                case "item frames" -> toggleFlag(plot, "item-frames", player);
                case "armor stands" -> toggleFlag(plot, "armor-stands", player);
                case "containers" -> toggleFlag(plot, "containers", player);
                case "pets" -> toggleFlag(plot, "pets", player);
                case "pvp" -> toggleFlag(plot, "pvp", player);
                case "safe zone" -> toggleFlag(plot, "safezone", player);
                case "back" -> guiManager.openMenu(player, "main");
            }

            // Refresh GUI so player sees updated toggle immediately
            if (!name.equalsIgnoreCase("back")) {
                guiManager.openMenu(player, "flags");
            }
        }

        // ------------------------
        // ROLES MENU
        // ------------------------
        if (title.contains("Trusted Players")) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case "back" -> guiManager.openMenu(player, "main");
                default -> messages.send(player, "&7This feature is still being developed.");
            }
        }

        // ------------------------
        // ADMIN EXPANSIONS MENU
        // ------------------------
        if (title.contains("Expansion Requests")) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case "pending requests" -> {
                    if (ExpansionRequestManager.hasRequests()) {
                        guiManager.openMenu(player, "admin-expansions");
                    } else {
                        messages.send(player, "&7No pending requests.");
                    }
                }
                case "approve selected" -> {
                    if (!ExpansionRequestManager.hasRequests()) {
                        messages.send(player, "&7No requests to approve.");
                        return;
                    }
                    ExpansionRequest req = ExpansionRequestManager.getRequests().get(0); // demo: first request
                    UUID pid = req.getPlayerId();
                    int extra = req.getExtraRadius();
                    plotManager.expandPlot(pid, extra, player);
                    ExpansionRequestManager.removeRequest(req);
                    guiManager.openMenu(player, "admin-expansions");
                }
                case "deny selected" -> guiManager.openMenu(player, "deny-reasons");
                case "back" -> guiManager.openMenu(player, "main");
            }
        }

        // ------------------------
        // DENY REASONS MENU
        // ------------------------
        if (title.contains("Deny Reasons")) {
            if (!ExpansionRequestManager.hasRequests()) {
                messages.send(player, "&7No requests to deny.");
                guiManager.openMenu(player, "admin-expansions");
                return;
            }

            ExpansionRequest req = ExpansionRequestManager.getRequests().get(0); // demo: first request
            UUID pid = req.getPlayerId();

            switch (name.toLowerCase(Locale.ROOT)) {
                case "too large" -> plotManager.denyExpansion(pid, "too large");
                case "abusive" -> plotManager.denyExpansion(pid, "abusive");
                case "other" -> plotManager.denyExpansion(pid, "other reason");
                case "back" -> {
                    guiManager.openMenu(player, "admin-expansions");
                    return;
                }
            }
            ExpansionRequestManager.removeRequest(req);
            guiManager.openMenu(player, "admin-expansions");
        }
    }

    private void toggleFlag(Plot plot, String flag, Player player) {
        boolean current = plot.getFlag(flag, false);
        plot.setFlag(flag, !current);

        if (current) {
            messages.send(player, "&c" + flag + " disabled.");
        } else {
            messages.send(player, "&a" + flag + " enabled.");
        }
    }
}
