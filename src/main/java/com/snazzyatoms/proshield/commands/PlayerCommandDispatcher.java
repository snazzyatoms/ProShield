package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerCommandDispatcher implements CommandExecutor, TabCompleter {

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
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.player-only");
            return true;
        }

        switch (cmd.getName().toLowerCase(Locale.ROOT)) {
            case "claim" -> handleClaim(player);
            case "unclaim" -> handleUnclaim(player);
            case "trust" -> handleTrust(player, args);
            case "untrust" -> handleUntrust(player, args);
            case "roles" -> handleRoles(player);
            case "transfer" -> handleTransfer(player, args);
            default -> messages.send(player, "error.unknown-command");
        }
        return true;
    }

    // --- (existing handler methods unchanged) ---

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (!(sender instanceof Player)) return suggestions;

        switch (cmd.getName().toLowerCase(Locale.ROOT)) {
            case "trust", "untrust" -> {
                if (args.length == 1) {
                    Bukkit.getOnlinePlayers().forEach(p -> suggestions.add(p.getName()));
                }
            }
        }

        // filter partial matches
        if (args.length > 0) {
            String current = args[args.length - 1].toLowerCase(Locale.ROOT);
            suggestions.removeIf(s -> !s.toLowerCase(Locale.ROOT).startsWith(current));
        }

        return suggestions;
    }
}
