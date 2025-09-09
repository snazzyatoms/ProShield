package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PlotManager.ClaimResult;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plots;

    public ProShieldCommand(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }

    private boolean needPlayer(CommandSender s) {
        if (!(s instanceof Player)) {
            s.sendMessage(prefix() + ChatColor.RED + "Players only.");
            return false;
        }
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(prefix() + ChatColor.AQUA + "Use /" + label + " claim|unclaim|info|trust|untrust|trusted|compass|reload");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("claim")) {
            if (!needPlayer(sender)) return true;
            Player p = (Player) sender;
            Location loc = p.getLocation();
            ClaimResult res = plots.createClaimDetailed(p.getUniqueId(), loc);
            switch (res) {
                case SUCCESS:
                    p.sendMessage(prefix() + ChatColor.GREEN + "Claim created for this chunk.");
                    break;
                case ALREADY_CLAIMED:
                    p.sendMessage(prefix() + ChatColor.RED + "This chunk is already claimed.");
                    break;
                case LIMIT_REACHED:
                    p.sendMessage(prefix() + ChatColor.RED + "You reached your claim limit.");
                    break;
                case SPAWN_PROTECTED:
                    p.sendMessage(prefix() + ChatColor.RED + "Claiming is not allowed near spawn.");
                    if (p.hasPermission("proshield.admin.spawnoverride")) {
                        p.sendMessage(prefix() + ChatColor.GRAY + "(You have admin override to bypass if intended.)");
                    }
                    break;
                default:
                    p.sendMessage(prefix() + ChatColor.RED + "Unable to create claim here.");
            }
            return true;
        }

        if (sub.equals("unclaim")) {
            if (!needPlayer(sender)) return true;
            Player p = (Player) sender;
            boolean ok = plots.removeClaim(p.getUniqueId(), p.getLocation(), false);
            p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Claim removed."
                                         : ChatColor.RED + "No claim here or you are not the owner."));
            return true;
        }

        if (sub.equals("info")) {
            if (!needPlayer(sender)) return true;
            Player p = (Player) sender;
            plots.getClaim(p.getLocation()).ifPresentOrElse(c -> {
                String owner = plots.ownerName(c.getOwner());
                var trusted = plots.listTrusted(p.getLocation());
                p.sendMessage(prefix() + ChatColor.AQUA + "Owner: " + owner);
                p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + (trusted.isEmpty() ? "(none)" : String.join(", ", trusted)));
            }, () -> p.sendMessage(prefix() + ChatColor.GRAY + "No claim in this chunk."));
            return true;
        }

        if (sub.equals("trust")) {
            if (!needPlayer(sender)) return true;
            Player p = (Player) sender;
            if (args.length < 2) {
                p.sendMessage(prefix() + ChatColor.RED + "Usage: /" + label + " trust <player>");
                return true;
            }
            Player target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                p.sendMessage(prefix() + ChatColor.RED + "Player not found.");
                return true;
            }
            boolean ok = plots.trust(p.getUniqueId(), p.getLocation(), target.getUniqueId());
            p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Trusted " + target.getName() + "."
                                         : ChatColor.RED + "No claim here or you are not the owner."));
            return true;
        }

        if (sub.equals("untrust")) {
            if (!needPlayer(sender)) return true;
            Player p = (Player) sender;
            if (args.length < 2) {
                p.sendMessage(prefix() + ChatColor.RED + "Usage: /" + label + " untrust <player>");
                return true;
            }
            UUID target = null;
            Player tp = plugin.getServer().getPlayer(args[1]);
            if (tp != null) target = tp.getUniqueId();
            if (target == null) {
                p.sendMessage(prefix() + ChatColor.RED + "Player not found.");
                return true;
            }
            boolean ok = plots.untrust(p.getUniqueId(), p.getLocation(), target);
            p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Untrusted " + args[1] + "."
                                         : ChatColor.RED + "No claim here or you are not the owner."));
            return true;
        }

        if (sub.equals("trusted")) {
            if (!needPlayer(sender)) return true;
            Player p = (Player) sender;
            var list = plots.listTrusted(p.getLocation());
            p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + (list.isEmpty() ? "(none)" : String.join(", ", list)));
            return true;
        }

        if (sub.equals("reload")) {
            plugin.reloadAllConfigs();
            sender.sendMessage(prefix() + ChatColor.GREEN + "Configuration reloaded.");
            return true;
        }

        if (sub.equals("compass")) {
            if (!needPlayer(sender)) return true;
            Player p = (Player) sender;
            plugin.getGuiManager().giveCompass(p, true);
            return true;
        }

        // Unknown subcommand -> fall back to help
        sender.sendMessage(prefix() + ChatColor.AQUA + "Use /" + label + " claim|unclaim|info|trust|untrust|trusted|compass|reload");
        return true;
    }
}
