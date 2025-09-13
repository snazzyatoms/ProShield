// src/main/java/com/snazzyatoms/proshield/commands/PlayerCommandDispatcher.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.UUID;

public class PlayerCommandDispatcher implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public PlayerCommandDispatcher(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("proshield") && args.length >= 2 && args[0].equalsIgnoreCase("flag")) {
            Plot plot = plotManager.getPlot(player.getLocation());
            if (plot == null) {
                player.sendMessage("§cYou must be inside your claim to toggle flags.");
                return true;
            }

            UUID pid = player.getUniqueId();
            if (!plot.isOwner(pid) && !player.isOp()) {
                player.sendMessage("§cYou do not own this claim.");
                return true;
            }

            String flag = args[1].toLowerCase(Locale.ROOT);
            boolean current = plot.getFlag(flag, plugin.getConfig().getBoolean("claims.default-flags." + flag, false));
            plot.setFlag(flag, !current);

            player.sendMessage("§aFlag '" + flag + "' is now " + (!current ? "§aEnabled" : "§cDisabled"));
            return true;
        }

        return false;
    }
}
