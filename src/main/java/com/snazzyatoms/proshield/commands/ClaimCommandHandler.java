package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ClaimCommandHandler implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public ClaimCommandHandler(ProShield plugin, PlotManager plotManager,
                               ClaimRoleManager roleManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cPlayers only.");
            return true;
        }

        String name = cmd.getName().toLowerCase();
        switch (name) {
            case "claim" -> handleClaim(player);
            case "unclaim" -> handleUnclaim(player);
            case "trust" -> handleTrust(player, args);
            case "untrust" -> handleUntrust(player, args);
            case "roles" -> player.performCommand("proshield roles"); // unify under GUI
            case "transfer" -> handleTransfer(player, args);
            default -> sender.sendMessage("§cUnknown command.");
        }
        return true;
    }

    private void handleClaim(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        if (plotManager.getPlot(chunk) != null) {
            messages.send(player, "claim.already-owned",
                    "owner", plotManager.getClaimName(player.getLocation()));
            return;
        }
        plotManager.createPlot(player.getUniqueId(), chunk);
        messages.send(player, "claim.success");
    }

    private void handleUnclaim(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) {
            messages.send(player, "claim.not-owned");
            return;
        }
        if (!plot.isOwner(player.getUniqueId())) {
            messages.send(player, "claim.not-owner");
            return;
        }
        plotManager.removePlot(plot);
        messages.send(player, "claim.unclaimed");
    }

    private void handleTrust(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: /trust <player> [role]");
            return;
        }
        String target = args[0];
        String role = args.length > 1 ? args[1] : "trusted";
        messages.send(player, "trust.added", "player", target, "role", role, "claim", "current");
    }

    private void handleUntrust(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: /untrust <player>");
            return;
        }
        String target = args[0];
        messages.send(player, "untrust.removed", "player", target, "claim", "current");
    }

    private void handleTransfer(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: /transfer <player>");
            return;
        }
        String target = args[0];
        messages.send(player, "transfer.success", "claim", "current", "player", target);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        String name = cmd.getName().toLowerCase();
        if (name.equals("trust") || name.equals("untrust") || name.equals("transfer")) {
            if (args.length == 1) {
                // Tab-complete online players
                List<String> players = new ArrayList<>();
                for (Player p : sender.getServer().getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        players.add(p.getName());
                    }
                }
                return players;
            }
        }
        return List.of();
    }
}
