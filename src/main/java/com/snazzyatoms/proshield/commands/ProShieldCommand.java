package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public ProShieldCommand(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "ProShield " + plugin.getDescription().getVersion() +
                    ChatColor.GRAY + " - Land & plot protection.");
            sender.sendMessage(ChatColor.YELLOW + "Use /proshield help for commands.");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload":
                if (!sender.hasPermission("proshield.admin.reload")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission.");
                    return true;
                }
                plugin.reloadAllConfigs();
                sender.sendMessage(ChatColor.GREEN + "ProShield configuration reloaded.");
                return true;

            case "purgeexpired":
                if (!sender.hasPermission("proshield.admin.expired.purge")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission.");
                    return true;
                }
                if (!plugin.getConfig().getBoolean("expiry.enabled", false)) {
                    sender.sendMessage(ChatColor.RED + "Expiry is not enabled in config.yml.");
                    return true;
                }
                int days = plugin.getConfig().getInt("expiry.days", 30);
                int removed = plotManager.cleanupExpiredClaims(days);
                plotManager.saveAll();
                sender.sendMessage(ChatColor.YELLOW + "Purged " + removed + " expired claim(s).");
                plugin.getLogger().info(sender.getName() + " purged " + removed + " expired claim(s).");
                return true;

            case "help":
                sender.sendMessage(ChatColor.AQUA + "ProShield Commands:");
                sender.sendMessage(ChatColor.YELLOW + "/proshield claim" + ChatColor.GRAY + " - Claim current chunk");
                sender.sendMessage(ChatColor.YELLOW + "/proshield unclaim" + ChatColor.GRAY + " - Unclaim current chunk");
                sender.sendMessage(ChatColor.YELLOW + "/proshield info" + ChatColor.GRAY + " - Info about current claim");
                sender.sendMessage(ChatColor.YELLOW + "/proshield trust <player>" + ChatColor.GRAY + " - Trust a player");
                sender.sendMessage(ChatColor.YELLOW + "/proshield untrust <player>" + ChatColor.GRAY + " - Remove trust");
                sender.sendMessage(ChatColor.YELLOW + "/proshield trusted" + ChatColor.GRAY + " - List trusted players");
                sender.sendMessage(ChatColor.YELLOW + "/proshield compass" + ChatColor.GRAY + " - Get the ProShield compass");
                sender.sendMessage(ChatColor.YELLOW + "/proshield bypass <on|off|toggle>" + ChatColor.GRAY + " - Toggle bypass mode");
                sender.sendMessage(ChatColor.YELLOW + "/proshield reload" + ChatColor.GRAY + " - Reload configs");
                sender.sendMessage(ChatColor.YELLOW + "/proshield purgeexpired" + ChatColor.GRAY + " - Purge expired claims");
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /proshield help");
                return true;
        }
    }
}
