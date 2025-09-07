// path: src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public ProShieldCommand(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(prefix() + ChatColor.YELLOW + "Use /proshield help for commands.");
            return true;
        }

        String sub = args[0].toLowerCase();

        // ------------------------------------------------------------------
        // Expired claim management (admin only)
        // ------------------------------------------------------------------
        if (sub.equals("expired")) {
            if (!sender.hasPermission("proshield.admin")) {
                sender.sendMessage(prefix() + ChatColor.RED + "You lack permission.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(prefix() + ChatColor.YELLOW + "Usage: /proshield expired <list|restore|purge>");
                return true;
            }

            switch (args[1].toLowerCase()) {
                case "list":
                    Set<String> expiredKeys = plugin.getConfig()
                            .getConfigurationSection("claims_expired") != null
                            ? plugin.getConfig().getConfigurationSection("claims_expired").getKeys(false)
                            : Set.of();
                    if (expiredKeys.isEmpty()) {
                        sender.sendMessage(prefix() + ChatColor.GRAY + "No expired claims stored.");
                    } else {
                        sender.sendMessage(prefix() + ChatColor.AQUA + "Expired claims:");
                        for (String key : expiredKeys) {
                            String ownerName = plugin.getConfig().getString("claims_expired." + key + ".ownerName", "Unknown");
                            sender.sendMessage(ChatColor.GRAY + " - " + key + " (" + ownerName + ")");
                        }
                    }
                    return true;

                case "restore":
                    if (args.length < 3) {
                        sender.sendMessage(prefix() + ChatColor.YELLOW + "Usage: /proshield expired restore <world:chunkX:chunkZ>");
                        return true;
                    }
                    String restoreKey = args[2];
                    if (plotManager.restoreExpiredClaim(restoreKey)) {
                        sender.sendMessage(prefix() + ChatColor.GREEN + "Restored claim: " + restoreKey);
                    } else {
                        sender.sendMessage(prefix() + ChatColor.RED + "Failed to restore claim: " + restoreKey);
                    }
                    return true;

                case "purge":
                    int purged = 0;
                    if (args.length == 3) {
                        try {
                            int days = Integer.parseInt(args[2]);
                            long cutoff = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L);
                            purged = purgeExpired(cutoff);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(prefix() + ChatColor.RED + "Invalid number of days.");
                            return true;
                        }
                    } else {
                        purged = purgeExpired(-1); // purge all
                    }
                    sender.sendMessage(prefix() + ChatColor.GREEN + "Purged " + purged + " expired claim(s).");
                    return true;
            }
        }

        // TODO: Keep your existing subcommands here (claim, unclaim, info, compass, trust, etc.)
        sender.sendMessage(prefix() + ChatColor.RED + "Unknown subcommand.");
        return true;
    }

    private int purgeExpired(long cutoff) {
        if (plugin.getConfig().getConfigurationSection("claims_expired") == null) return 0;

        int purged = 0;
        for (String key : plugin.getConfig().getConfigurationSection("claims_expired").getKeys(false)) {
            long removedAt = plugin.getConfig().getLong("claims_expired." + key + ".removedAt", 0);
            if (cutoff < 0 || removedAt < cutoff) {
                plugin.getConfig().set("claims_expired." + key, null);
                purged++;
            }
        }
        if (purged > 0) plugin.saveConfig();
        return purged;
    }
}
