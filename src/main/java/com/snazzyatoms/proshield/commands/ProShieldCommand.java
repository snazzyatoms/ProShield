package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ProShieldCommand implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final PlotManager plots;
    private final GUIManager gui;

    public ProShieldCommand(ProShield plugin, PlotManager plots, GUIManager gui) {
        this.plugin = plugin;
        this.plots = plots;
        this.gui = gui;
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player p) {
                p.sendMessage(prefix() + "Use /proshield help or right-click the ProShield compass.");
            } else {
                sender.sendMessage(prefix() + "Console: /proshield reload | purgeexpired | debug");
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "help" -> {
                if (sender instanceof Player p) {
                    p.sendMessage(prefix() + ChatColor.AQUA + "Commands: claim, unclaim, info, trust, untrust, trusted");
                } else {
                    sender.sendMessage(prefix() + "Console help: reload, purgeexpired <days> [dryrun], debug <on|off|toggle>");
                }
                return true;
            }

            case "compass" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(prefix() + "Players only.");
                    return true;
                }
                boolean admin = p.hasPermission("proshield.admin") || p.hasPermission("proshield.admin.gui");
                gui.giveCompass(p, admin);
                return true;
            }

            case "claim" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + "Players only."); return true; }
                boolean ok = plots.createClaim(p.getUniqueId(), p.getLocation());
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Chunk claimed." : ChatColor.RED + "This chunk is already claimed or you reached your limit."));
                return true;
            }

            case "unclaim" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + "Players only."); return true; }
                boolean ok = plots.removeClaim(p.getUniqueId(), p.getLocation(), false);
                p.sendMessage(prefix() + (ok ? ChatColor.YELLOW + "Chunk unclaimed." : ChatColor.RED + "You are not the owner of this claim."));
                return true;
            }

            case "info" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + "Players only."); return true; }
                var opt = plots.getClaim(p.getLocation());
                if (opt.isEmpty()) { p.sendMessage(prefix() + ChatColor.RED + "This chunk is not claimed."); return true; }
                var c = opt.get();
                p.sendMessage(prefix() + ChatColor.AQUA + "Owner: " + plots.ownerName(c.getOwner()));
                p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + String.join(", ", plots.listTrusted(p.getLocation())));
                return true;
            }

            case "trust" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + "Players only."); return true; }
                if (args.length < 2) { p.sendMessage(prefix() + "Usage: /proshield trust <player> [role]"); return true; }
                var target = plugin.getServer().getPlayerExact(args[1]);
                if (target == null) { p.sendMessage(prefix() + ChatColor.RED + "Player not found."); return true; }
                boolean ok = plots.trust(p.getUniqueId(), p.getLocation(), target.getUniqueId());
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Trusted " + target.getName() : ChatColor.RED + "Failed (not owner?)"));
                return true;
            }

            case "untrust" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + "Players only."); return true; }
                if (args.length < 2) { p.sendMessage(prefix() + "Usage: /proshield untrust <player>"); return true; }
                var target = plugin.getServer().getPlayerExact(args[1]);
                if (target == null) { p.sendMessage(prefix() + ChatColor.RED + "Player not found."); return true; }
                boolean ok = plots.untrust(p.getUniqueId(), p.getLocation(), target.getUniqueId());
                p.sendMessage(prefix() + (ok ? ChatColor.YELLOW + "Untrusted " + target.getName() : ChatColor.RED + "Failed (not owner?)"));
                return true;
            }

            case "trusted" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + "Players only."); return true; }
                p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + String.join(", ", plots.listTrusted(p.getLocation())));
                return true;
            }

            case "reload" -> {
                if (!sender.hasPermission("proshield.admin.reload")) { sender.sendMessage(prefix() + ChatColor.RED + "No permission."); return true; }
                plugin.reloadAllConfigs();
                sender.sendMessage(prefix() + ChatColor.GREEN + "Config reloaded.");
                return true;
            }

            case "purgeexpired" -> {
                if (!sender.hasPermission("proshield.admin.expired.purge")) { sender.sendMessage(prefix() + ChatColor.RED + "No permission."); return true; }
                int days = 30;
                boolean dry = false;
                if (args.length >= 2) {
                    try { days = Integer.parseInt(args[1]); } catch (NumberFormatException ignore) {}
                }
                if (args.length >= 3) dry = "dryrun".equalsIgnoreCase(args[2]);
                int count = plots.cleanupExpiredClaims(days, !dry);
                sender.sendMessage(prefix() + (dry ? "Preview: " : "Purged: ") + count + " expired claims (>" + days + "d).");
                return true;
            }
        }

        sender.sendMessage(prefix() + ChatColor.RED + "Unknown subcommand. Try /proshield help");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.add("help");
            out.add("claim");
            out.add("unclaim");
            out.add("info");
            out.add("trust");
            out.add("untrust");
            out.add("trusted");
            out.add("compass");
            if (sender.hasPermission("proshield.admin.reload")) out.add("reload");
            if (sender.hasPermission("proshield.admin.expired.purge")) out.add("purgeexpired");
        }
        return out;
    }
}
