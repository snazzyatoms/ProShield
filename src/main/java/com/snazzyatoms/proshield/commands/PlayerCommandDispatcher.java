package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerCommandDispatcher implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public PlayerCommandDispatcher(ProShield plugin,
                                   PlotManager plotManager,
                                   ClaimRoleManager roleManager,
                                   MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        String name = cmd.getName().toLowerCase();
        switch (name) {
            case "claim" -> handleClaim(player);
            case "unclaim" -> handleUnclaim(player);
            case "trust" -> handleTrust(player, args);
            case "untrust" -> handleUntrust(player, args);
            case "roles" -> {
                // Hand off to GUI; keep behavior minimal here
                plugin.getGuiManager().openMenu(player, "roles");
            }
            case "transfer" -> handleTransfer(player, args);
            default -> messages.send(player, "&cUnknown command.");
        }
        return true;
    }

    private void handleClaim(Player player) {
        Location loc = player.getLocation();
        Plot existing = plotManager.getPlot(loc);
        if (existing != null) {
            messages.send(player, "&cThis chunk is already claimed.");
            return;
        }
        Plot created = plotManager.createPlot(player, loc);
        messages.send(player, "&aClaimed this chunk! &7(" + created.getWorldName() + " "
                + created.getX() + "," + created.getZ() + ")");
    }

    private void handleUnclaim(Player player) {
        Location loc = player.getLocation();
        Plot plot = plotManager.getPlot(loc);
        if (plot == null) {
            messages.send(player, "&cNo claim here.");
            return;
        }
        if (!plot.isOwner(player.getUniqueId())) {
            messages.send(player, "&cOnly the owner can unclaim this chunk.");
            return;
        }
        plotManager.removePlot(loc);
        messages.send(player, "&aUnclaimed this chunk.");
    }

    private void handleTrust(Player player, String[] args) {
        if (args.length < 1) {
            messages.send(player, "&cUsage: /trust <player> [role]");
            return;
        }
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&cNo claim here.");
            return;
        }
        if (!plot.isOwner(player.getUniqueId())) {
            messages.send(player, "&cOnly the owner can manage trust.");
            return;
        }
        String targetName = args[0];
        String role = (args.length >= 2) ? args[1] : "trusted";

        // Keep both role cache & simple UUID trust list consistent
        roleManager.trustPlayer(plot, targetName, role);
        // (Optional) If you still use Plot's UUID trust set elsewhere, keep it in sync in later pass

        messages.send(player, "&aTrusted &e" + targetName + " &aas " + role + ".");
    }

    private void handleUntrust(Player player, String[] args) {
        if (args.length < 1) {
            messages.send(player, "&cUsage: /untrust <player>");
            return;
        }
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&cNo claim here.");
            return;
        }
        if (!plot.isOwner(player.getUniqueId())) {
            messages.send(player, "&cOnly the owner can manage trust.");
            return;
        }
        String targetName = args[0];
        roleManager.untrustPlayer(plot, targetName);
        messages.send(player, "&aRemoved trust from &e" + targetName + "&a.");
    }

    private void handleTransfer(Player player, String[] args) {
        if (args.length < 1) {
            messages.send(player, "&cUsage: /transfer <player>");
            return;
        }
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&cNo claim here.");
            return;
        }
        if (!plot.isOwner(player.getUniqueId())) {
            messages.send(player, "&cOnly the owner can transfer ownership.");
            return;
        }
        String newOwnerName = args[0];
        boolean ok = roleManager.transferOwnership(plot, newOwnerName);
        if (ok) {
            messages.send(player, "&aTransferred ownership to &e" + newOwnerName + "&a.");
        } else {
            messages.send(player, "&cCould not transfer ownership to &e" + newOwnerName + "&c.");
        }
    }
}
