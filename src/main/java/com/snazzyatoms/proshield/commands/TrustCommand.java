package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TrustCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public TrustCommand(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "general.no-console");
            return true;
        }

        Plot plot = plotManager.getPlot(player.getLocation().getChunk());
        if (plot == null) {
            messages.send(player, "trust.no-claim");
            return true;
        }

        if (!plot.isOwner(player.getUniqueId())) {
            messages.send(player, "trust.not-owner");
            return true;
        }

        if (args.length < 1) {
            messages.send(player, "trust.usage");
            return true;
        }

        String targetName = args[0];
        ClaimRole role = ClaimRole.MEMBER;
        if (args.length > 1) {
            role = ClaimRole.fromString(args[1], ClaimRole.MEMBER);
        }

        roleManager.trustPlayer(plot, targetName, role);
        messages.send(player, "trust.success", targetName, role.name());
        return true;
    }
}
