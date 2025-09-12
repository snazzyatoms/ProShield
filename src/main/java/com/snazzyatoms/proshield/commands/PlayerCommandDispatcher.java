// src/main/java/com/snazzyatoms/proshield/commands/PlayerCommandDispatcher.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerCommandDispatcher implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public PlayerCommandDispatcher(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.player-only");
            return true;
        }

        switch (cmd.getName().toLowerCase(Locale.ROOT)) {
            case "claim" -> handleClaim(player);
            case "unclaim" -> handleUnclaim(player);
            case "trust" -> handleTrust(player, args);
            case "untrust" -> handleUntrust(player, args);
            case "roles" -> handleRoles(player);
            case "transfer" -> handleTransfer(player, args);
            default -> messages.send(player, "error.unknown-command");
        }
        return true;
    }

    private void handleClaim(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        if (plotManager.getPlot(chunk) != null) {
            messages.send(player, "claim.already-owned");
            return;
        }
        Plot plot = plotManager.createPlot(player.getUniqueId(), chunk);
        messages.send(player, "claim.success", Map.of("claim", plot.getDisplayNameSafe()));
    }

    private void handleUnclaim(Player player) {
        Location loc = player.getLocation();
        Plot plot = plotManager.getPlot(loc);
        if (plot == null) { messages.send(player, "error.no-claim"); return; }
        if (!plot.isOwner(player.getUniqueId()) && !roleManager.canUnclaim(player.getUniqueId(), plot.getId())) {
            messages.send(player, "error.no-permission"); return;
        }
        plotManager.removePlot(plot);
        messages.send(player, "claim.unclaimed", Map.of("claim", plot.getDisplayNameSafe()));
    }

    private void handleTrust(Player player, String[] args) {
        if (args.length < 1) { messages.send(player, "trust.usage"); return; }
        Location loc = player.getLocation();
        Plot plot = plotManager.getPlot(loc);
        if (plot == null) { messages.send(player, "error.no-claim"); return; }
        if (!plot.isOwner(player.getUniqueId())) { messages.send(player, "error.not-owner"); return; }
        String targetName = args[0];
        String role = args.length > 1 ? args[1] : "trusted";
        boolean success = roleManager.trustPlayer(plot, targetName, role);
        if (success) messages.send(player, "trust.added", Map.of("player", targetName, "role", role, "claim", plot.getDisplayNameSafe()));
        else messages.send(player, "trust.failed", Map.of("player", targetName));
    }

    private void handleUntrust(Player player, String[] args) {
        if (args.length < 1) { messages.send(player, "untrust.usage"); return; }
        Location loc = player.getLocation();
        Plot plot = plotManager.getPlot(loc);
        if (plot == null) { messages.send(player, "error.no-claim"); return; }
        if (!plot.isOwner(player.getUniqueId())) { messages.send(player, "error.not-owner"); return; }
        String targetName = args[0];
        boolean success = roleManager.untrustPlayer(plot, targetName);
        if (success) messages.send(player, "untrust.removed", Map.of("player", targetName, "claim", plot.getDisplayNameSafe()));
        else messages.send(player, "untrust.not-trusted", Map.of("player", targetName));
    }

    private void handleRoles(Player player) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) { messages.send(player, "error.no-claim"); return; }
        if (!plot.isOwner(player.getUniqueId())) { messages.send(player, "error.not-owner"); return; }
        plugin.getGuiManager().openMenu(player, "roles");
    }

    private void handleTransfer(Player player, String[] args) {
        if (args.length < 1) { messages.send(player, "transfer.usage"); return; }
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) { messages.send(player, "error.no-claim"); return; }
        if (!plot.isOwner(player.getUniqueId())) { messages.send(player, "transfer.not-owner"); return; }
        String targetName = args[0];
        boolean success = roleManager.transferOwnership(plot, targetName);
        if (success) messages.send(player, "transfer.success", Map.of("player", targetName, "claim", plot.getDisplayNameSafe()));
        else messages.send(player, "transfer.failed", Map.of("player", targetName));
    }

    // Tab completion for trust/untrust player names and roles
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        String name = cmd.getName().toLowerCase(Locale.ROOT);
        if (name.equals("trust")) {
            if (args.length == 1) {
                return org.bukkit.Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT)))
                        .toList();
            } else if (args.length == 2) {
                List<String> roles = new ArrayList<>(List.of("visitor", "trusted", "builder", "container", "moderator", "manager"));
                if (sender.hasPermission("proshield.admin")) roles.add("owner");
                return roles.stream()
                        .filter(r -> r.startsWith(args[1].toLowerCase(Locale.ROOT)))
                        .toList();
            }
        } else if (name.equals("untrust") && args.length == 1) {
            return org.bukkit.Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .toList();
        }
        return Collections.emptyList();
        }
}
