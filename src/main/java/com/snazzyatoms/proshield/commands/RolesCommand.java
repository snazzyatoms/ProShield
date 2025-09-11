// src/main/java/com/snazzyatoms/proshield/commands/RolesCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
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

/**
 * /roles command — opens the role management GUI for the current claim.
 *
 * ✅ Fixed:
 * - Uses correct messages.yml keys (error.*, not errors.*).
 * - Constructor matches ProShield.java registration.
 * - Consistent plot lookup (Chunk-based).
 */
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players may use this command.");
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            messages.send(player, "error.not-in-claim");
            return true;
        }

        // Only owners/co-owners may open role GUI
        ClaimRole role = roleManager.getRole(plot, player.getUniqueId());
        if (!roleManager.isOwnerOrCoOwner(role)) {
            messages.send(player, "error.not-owner");
            return true;
        }

        guiManager.openRolesGUI(player, plot);
        return true;
    }
}
