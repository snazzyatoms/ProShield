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

public class TrustCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    public TrustCommand(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length < 1) {
            MessagesUtil.sendMessage(player, "command-trust-usage");
            return true;
        }

        String targetName = args[0];
        String roleName = args.length > 1 ? args[1].toUpperCase() : "MEMBER";

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null || !plot.isOwner(player.getUniqueId())) {
            MessagesUtil.sendMessage(player, "no-permission");
            return true;
        }

        ClaimRole role = ClaimRole.fromString(roleName);
        if (role == null) {
            MessagesUtil.sendMessage(player, "command-trust-invalid-role", "{role}", roleName);
            return true;
        }

        if (plot.isTrusted(targetName)) {
            MessagesUtil.sendMessage(player, "command-trust-already", "{player}", targetName);
            return true;
        }

        roleManager.trustPlayer(plot, targetName, role);
        MessagesUtil.sendMessage(player, "command-trust-success", "{player}", targetName, "{role}", role.name());
        return true;
    }
}
