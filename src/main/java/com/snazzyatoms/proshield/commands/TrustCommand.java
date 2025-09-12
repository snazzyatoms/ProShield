package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TrustCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roles;

    public TrustCommand(ProShield plugin, PlotManager plots, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.plots = plots;
        this.roles = roles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "⛔ You are not standing inside a claim.");
            return true;
        }

        if (!plot.isOwner(player.getUniqueId())) {
            if (!roles.canManageTrust(plot.getId(), player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "⛔ You do not have permission to manage trust here.");
                return true;
            }
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /trust <player> [role]");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || target.getUniqueId() == null) {
            player.sendMessage(ChatColor.RED + "⛔ Player not found.");
            return true;
        }

        String roleName = (args.length >= 2) ? args[1].toLowerCase() : "trusted";
        ClaimRole role = ClaimRole.fromString(roleName);
        if (role == null) role = ClaimRole.TRUSTED;

        plot.getTrusted().put(target.getUniqueId(), role);
        plugin.getPlotManager().saveAsync(plot);

        // Role manager also tracks this
        roles.assignRole(plot.getId(), target.getUniqueId(), roleName);

        player.sendMessage(ChatColor.GREEN + "✅ " + target.getName() + " is now trusted as " + role.name() + ".");
        if (target.isOnline()) {
            target.getPlayer().sendMessage(ChatColor.YELLOW + "You have been trusted in " + plot.getDisplayNameSafe() +
                    " as " + role.name() + ".");
        }

        return true;
    }
}
