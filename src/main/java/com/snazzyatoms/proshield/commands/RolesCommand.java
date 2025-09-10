package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RolesCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final GUIManager guiManager;
    private final MessagesUtil messages;

    public RolesCommand(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager, GUIManager guiManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.guiManager = guiManager;
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
            messages.send(player, "roles.no-claim");
            return true;
        }

        if (!plot.isOwner(player.getUniqueId())) {
            messages.send(player, "roles.not-owner");
            return true;
        }

        guiManager.openRolesMenu(player);
        return true;
    }
}
