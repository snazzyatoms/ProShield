package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UntrustCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public UntrustCommand(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
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
            messages.send(player, "untrust.no-claim");
            return true;
        }

        if (!plot.isOwner(player.getUniqueId())) {
            messages.send(player, "untrust.not-owner");
            return true;
        }

        if (args.length < 1) {
            messages.send(player, "untrust.usage");
            return true;
        }

        String targetName = args[0];
        if (!plot.removeTrusted(targetName)) {
            messages.send(player, "untrust.not-trusted", targetName);
            return true;
        }

        messages.send(player, "untrust.success", targetName);
        return true;
    }
}
