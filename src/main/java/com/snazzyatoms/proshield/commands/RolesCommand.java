package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RolesCommand implements CommandExecutor {

    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    public RolesCommand(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plotManager = plotManager;
        this.roleManager = roleManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null || !plot.isOwner(player.getUniqueId())) {
            MessagesUtil.sendMessage(player, "no-permission");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            MessagesUtil.sendMessage(player, "command-roles-list-header");
            plot.getTrusted().forEach((name, role) -> {
                MessagesUtil.sendMessage(player, "command-roles-list-entry", "{player}", name, "{role}", role.name());
            });
            return true;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            String targetName = args[1];
            ClaimRole role = ClaimRole.fromString(args[2]);

            if (role == null) {
                MessagesUtil.sendMessage(player, "command-trust-invalid-role", "{role}", args[2]);
                return true;
            }

            if (!plot.isTrusted(targetName)) {
                MessagesUtil.sendMessage(player, "command-untrust-not-found", "{player}", targetName);
                return true;
            }

            roleManager.setRole(plot, targetName, role);
            MessagesUtil.sendMessage(player, "command-roles-change-success", "{player}", targetName, "{role}", role.name());
            return true;
        }

        MessagesUtil.sendMessage(player, "command-roles-usage");
        return true;
    }
}
