package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerCommandDispatcher implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    // Role suggestions (customize these to match your ClaimRoleManager roles)
    private static final List<String> ROLE_SUGGESTIONS =
            Arrays.asList("trusted", "builder", "moderator", "manager");

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

    // --------------------------
    // Command handlers (unchanged)
    // --------------------------

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

        if (plot == null) {
            messages.send(player, "error.no-claim");
            return;
        }

        if (!plot.isOwner(player.getUniqueId()) &&
            !roleManager.canUnclaim(player.getUniqueId(), plot.getId())) {
            messages.send(player, "error.no-permission");
            return;
        }

        plotManager.removePlot(plot);
        messages.send(player, "claim.unclaimed", Map.of("claim", plot.getDisplayNameSafe()));
    }

    private void handleTrust(Player player, String[] args) {
        if (args.length < 1) {
            messages.send(player, "trust.usage");
            return;
        }

        Location loc = player.getLocation();
        Plot plot = plotManager.getPlot(loc);

        if (plot == null) {
            messages.send(player, "error.no-claim");
            return;
        }

        if (!plot.isOwner(player.getUniqueId())) {
            messages.send(player, "error.not-owner");
            return;
        }

        String targetName = args[0];
        String role = args.length > 1 ? args[1] : "trusted";

        boolean success = roleManager.trustPlayer(plot, targetName, role);
        if (success) {
            messages.send(player, "trust.added",
                    Map.of("player", targetName, "role", role, "claim", plot.getDisplayNameSafe()));
        } else {
            messages.send(player, "trust.failed", Map.of("player", targetName));
        }
    }

    private void handleUntrust(Player player, String[] args) {
        if (args.length < 1) {
            messages.send(player, "untrust.usage");
            return;
        }

        Location loc = player.getLocation();
        Plot plot = plotManager.getPlot(loc);

        if (plot == null) {
            messages.send(player, "error.no-claim");
            return;
        }

        if (!plot.isOwner(player.getUniqueId())) {
            messages.send(player, "error.not-owner");
            return;
        }

        String targetName = args[0];
        boolean success = roleManager.untrustPlayer(plot, targetName);
        if (success) {
            messages.send(player, "untrust.removed",
                    Map.of("player", targetName, "claim", plot.getDisplayNameSafe()));
        } else {
            messages.send(player, "untrust.not-trusted", Map.of("player", targetName));
        }
    }

    private void handleRoles(Player player) {
        Location loc = player.getLocation();
        Plot plot = plotManager.getPlot(loc);

        if (plot == null) {
            messages.send(player, "error.no-claim");
            return;
        }

        if (!plot.isOwner(player.getUniqueId())) {
            messages.send(player, "error.not-owner");
            return;
        }

        plugin.getGuiManager().openMenu(player, "roles");
    }

    private void handleTransfer(Player player, String[] args) {
        if (args.length < 1) {
            messages.send(player, "transfer.usage");
            return;
        }

        Location loc = player.getLocation();
        Plot plot = plotManager.getPlot(loc);

        if (plot == null) {
            messages.send(player, "error.no-claim");
            return;
        }

        if (!plot.isOwner(player.getUniqueId())) {
            messages.send(player, "transfer.not-owner");
            return;
        }

        String targetName = args[0];
        boolean success = roleManager.transferOwnership(plot, targetName);
        if (success) {
            messages.send(player, "transfer.success",
                    Map.of("player", targetName, "claim", plot.getDisplayNameSafe()));
        } else {
            messages.send(player, "transfer.failed", Map.of("player", targetName));
        }
    }

    // --------------------------
    // Tab Completion
    // --------------------------
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (!(sender instanceof Player)) return suggestions;

        switch (cmd.getName().toLowerCase(Locale.ROOT)) {
            case "trust" -> {
                if (args.length == 1) {
                    Bukkit.getOnlinePlayers().forEach(p -> suggestions.add(p.getName()));
                } else if (args.length == 2) {
                    suggestions.addAll(ROLE_SUGGESTIONS);
                }
            }
            case "untrust" -> {
                if (args.length == 1) {
                    Bukkit.getOnlinePlayers().forEach(p -> suggestions.add(p.getName()));
                }
            }
        }

        // filter partial matches
        if (args.length > 0) {
            String current = args[args.length - 1].toLowerCase(Locale.ROOT);
            suggestions.removeIf(s -> !s.toLowerCase(Locale.ROOT).startsWith(current));
        }

        return suggestions;
    }
}
