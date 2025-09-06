package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.UUID;

public class ProShieldCommand implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public ProShieldCommand(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        plugin.getCommand("proshield").setTabCompleter(this);
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(prefix() + ChatColor.AQUA + "ProShield is running. Claims: " + plotManager.getClaimCount());
            sender.sendMessage(ChatColor.GRAY + "Try: /proshield claim, /proshield unclaim, /proshield info");
            return true;
        }

        if (!(sender instanceof Player p)) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadAllConfigs();
                sender.sendMessage("[ProShield] Reloaded.");
                return true;
            }
            sender.sendMessage("[ProShield] Console: use /proshield reload");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "claim" -> {
                boolean ok = plotManager.createClaim(p.getUniqueId(), p.getLocation());
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Chunk claimed." :
                        ChatColor.RED + "Already claimed or you reached your limit."));
                return true;
            }
            case "unclaim" -> {
                boolean ok = plotManager.removeClaim(p.getUniqueId(), p.getLocation());
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Chunk unclaimed." :
                        ChatColor.RED + "You do not own this chunk."));
                return true;
            }
            case "info" -> {
                plotManager.getClaim(p.getLocation()).ifPresentOrElse(c -> {
                    p.sendMessage(prefix() + ChatColor.AQUA + "Owner: " + plotManager.ownerName(c.getOwner()));
                    p.sendMessage(prefix() + ChatColor.AQUA + "Chunk: " + c.getWorld() + " " + c.getChunkX() + "," + c.getChunkZ());
                    var trusted = plotManager.listTrusted(p.getLocation());
                    p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + (trusted.isEmpty() ? "None" : String.join(", ", trusted)));
                }, () -> p.sendMessage(prefix() + ChatColor.GRAY + "This chunk is unclaimed."));
                return true;
            }
            case "compass" -> {
                if (!p.hasPermission("proshield.compass") && !p.isOp()) {
                    p.sendMessage(prefix() + ChatColor.RED + "No permission.");
                    return true;
                }
                p.getInventory().addItem(GUIManager.createAdminCompass());
                p.sendMessage(prefix() + ChatColor.GREEN + "Compass added.");
                return true;
            }
            case "trust" -> {
                if (args.length < 2) { p.sendMessage(prefix() + ChatColor.RED + "Usage: /proshield trust <player>"); return true; }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (target == null || (target.getName() == null && !target.hasPlayedBefore())) {
                    p.sendMessage(prefix() + ChatColor.RED + "Player not found.");
                    return true;
                }
                boolean ok = plotManager.trust(p.getUniqueId(), p.getLocation(), target.getUniqueId());
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Trusted " + args[1] + "." :
                        ChatColor.RED + "You must own this chunk."));
                return true;
            }
            case "untrust" -> {
                if (args.length < 2) { p.sendMessage(prefix() + ChatColor.RED + "Usage: /proshield untrust <player>"); return true; }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (target == null || (target.getName() == null && !target.hasPlayedBefore())) {
                    p.sendMessage(prefix() + ChatColor.RED + "Player not found.");
                    return true;
                }
                boolean ok = plotManager.untrust(p.getUniqueId(), p.getLocation(), target.getUniqueId());
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Untrusted " + args[1] + "." :
                        ChatColor.RED + "You must own this chunk."));
                return true;
            }
            case "trusted" -> {
                var list = plotManager.listTrusted(p.getLocation());
                if (list.isEmpty()) p.sendMessage(prefix() + ChatColor.GRAY + "No trusted players here.");
                else p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + String.join(", ", list));
                return true;
            }
            case "bypass" -> {
                if (!p.hasPermission("proshield.bypass")) { p.sendMessage(prefix() + ChatColor.RED + "No permission."); return true; }
                String opt = (args.length >= 2 ? args[1].toLowerCase() : "toggle");
                boolean now = p.hasMetadata("proshield_bypass");
                boolean set = switch (opt) {
                    case "on", "enable", "true" -> true;
                    case "off", "disable", "false" -> false;
                    default -> !now;
                };
                if (set && !now) p.setMetadata("proshield_bypass", new FixedMetadataValue(plugin, true));
                if (!set && now) p.removeMetadata("proshield_bypass", plugin);
                p.sendMessage(prefix() + (set ? ChatColor.GREEN + "Bypass enabled." : ChatColor.RED + "Bypass disabled."));
                return true;
            }
            case "reload" -> {
                if (!p.hasPermission("proshield.admin.reload")) { p.sendMessage(prefix() + ChatColor.RED + "No permission."); return true; }
                plugin.reloadAllConfigs();
                p.sendMessage(prefix() + ChatColor.GREEN + "Configs reloaded.");
                return true;
            }
            default -> {
                p.sendMessage(prefix() + ChatColor.GRAY + "Unknown subcommand. Try /proshield help");
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if (args.length == 1) {
            return List.of("claim","unclaim","info","compass","trust","untrust","trusted","bypass","reload");
        }
        if (args.length == 2 && ("trust".equalsIgnoreCase(args[0]) || "untrust".equalsIgnoreCase(args[0]))) {
            return null; // let Bukkit suggest player names
        }
        if (args.length == 2 && "bypass".equalsIgnoreCase(args[0])) {
            return List.of("on","off","toggle");
        }
        return List.of();
    }
}
