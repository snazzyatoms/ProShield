package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /roles
 *
 * Opens the roles management menu for the current claim.
 */
public class RolesCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final GUIManager gui;
    private final MessagesUtil msg;

    public RolesCommand(ProShield plugin, PlotManager plots, ClaimRoleManager roles, GUIManager gui) {
        this.plugin = plugin;
        this.plots = plots;
        this.roles = roles;
        this.gui = gui;
        this.msg = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            msg.send(sender, "error.player-only");
            return true;
        }

        // Must be inside a claim
        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            msg.send(player, "roles.no-claim");
            return true;
        }

        // Must be owner or co-owner
        ClaimRole role = roles.getRole(plot, player.getUniqueId());
        if (role != ClaimRole.OWNER && role != ClaimRole.CO_OWNER) {
            msg.send(player, "roles.not-allowed", plot.getDisplayNameSafe());
            return true;
        }

        // Open the GUI
        gui.openRolesMenu(player);
        msg.debug("Opened roles menu for " + player.getName() + " in claim " + plot.getDisplayNameSafe());
        return true;
    }
}
