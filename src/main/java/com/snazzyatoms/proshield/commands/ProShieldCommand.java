// path: src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plots;

    public ProShieldCommand(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots  = plots;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {
        if (a.length == 0) {
            sender.sendMessage(prefix() + ChatColor.AQUA + "ProShield " + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.GRAY + " /" + label + " claim|unclaim|info|trust|untrust|trusted|compass|bypass|purgeexpired|reload");
            return true;
        }

        switch (a[0].toLowerCase()) {
            case "claim": {
                if (!(sender instanceof Player p)) return err(sender, "Players only.");
                Location loc = p.getLocation();
                boolean ok = plots.createClaim(p.getUniqueId(), loc);
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Claimed this chunk."
                                             : ChatColor.RED + "Already claimed or limit reached."));
                return true;
            }
            case "unclaim": {
                if (!(sender instanceof Player p)) return err(sender, "Players only.");
                boolean ok = plots.removeClaim(p.getUniqueId(), p.getLocation(), false);
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Unclaimed."
                                             : ChatColor.RED + "Not your claim or no claim here."));
                return true;
            }
            case "info": {
                if (!(sender instanceof Player p)) return err(sender, "Players only.");
                plots.getClaim(p.getLocation()).ifPresentOrElse(c -> {
                    p.sendMessage(prefix() + ChatColor.AQUA + "Owner: " + plots.ownerName(c.getOwner()));
                    p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + String.join(", ", plots.listTrusted(p.getLocation())));
                }, () -> p.sendMessage(prefix() + ChatColor.GRAY + "No claim in this chunk."));
                return true;
            }
            case "trust": {
                if (!(sender instanceof Player p)) return err(sender, "Players only.");
                if (a.length < 2) return err(sender, "Usage: /" + label + " trust <player> [role]");
                var target = plugin.getServer().getPlayerExact(a[1]);
                if (target == null) return err(sender, "Player not found.");
                boolean ok = plots.trust(p.getUniqueId(), p.getLocation(), target.getUniqueId());
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Trusted " + target.getName() + "."
                                             : ChatColor.RED + "No claim here or you are not the owner."));
                return true;
            }
            case "untrust": {
                if (!(sender instanceof Player p)) return err(sender, "Players only.");
                if (a.length < 2) return err(sender, "Usage: /" + label + " untrust <player>");
                var target = plugin.getServer().getPlayerExact(a[1]);
                if (target == null) return err(sender, "Player not found.");
                boolean ok = plots.untrust(p.getUniqueId(), p.getLocation(), target.getUniqueId());
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Untrusted " + target.getName() + "."
                                             : ChatColor.RED + "No claim here or you are not the owner."));
                return true;
            }
            case "trusted": {
                if (!(sender instanceof Player p)) return err(sender, "Players only.");
                var list = plots.listTrusted(p.getLocation());
                p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + (list.isEmpty() ? "(none)" : String.join(", ", list)));
                return true;
            }
            case "compass": {
                if (!(sender instanceof Player p)) return err(sender, "Players only.");
                if (!p.hasPermission("proshield.compass") && !p.isOp() && !p.hasPermission("proshield.admin"))
                    return err(p, "No permission.");
                p.getInventory().addItem(GUIManager.createAdminCompass());
                p.sendMessage(prefix() + ChatColor.GREEN + "Compass added.");
                return true;
            }
            case "bypass": {
                if (!(sender instanceof Player p)) return err(sender, "Players only.");
                if (!p.hasPermission("proshield.bypass")) return err(p, "No permission.");
                if (a.length == 1 || a[1].equalsIgnoreCase("toggle")) {
                    boolean now = !p.hasMetadata("proshield_bypass");
                    if (now) p.setMetadata("proshield_bypass", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
                    else     p.removeMetadata("proshield_bypass", plugin);
                    p.sendMessage(prefix() + ChatColor.YELLOW + "Bypass: " + (now ? "ON" : "OFF"));
                } else if (a[1].equalsIgnoreCase("on")) {
                    p.setMetadata("proshield_bypass", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
                    p.sendMessage(prefix() + ChatColor.YELLOW + "Bypass: ON");
                } else if (a[1].equalsIgnoreCase("off")) {
                    p.removeMetadata("proshield_bypass", plugin);
                    p.sendMessage(prefix() + ChatColor.YELLOW + "Bypass: OFF");
                }
                return true;
            }
            case "purgeexpired": {
                if (!sender.hasPermission("proshield.admin.expired.purge")) return err(sender, "No permission.");
                if (a.length < 2) return err(sender, "Usage: /" + label + " purgeexpired <days> [dryrun]");
                int days;
                try { days = Integer.parseInt(a[1]); } catch (NumberFormatException ex) { return err(sender, "Invalid days."); }
                boolean dry = a.length >= 3 && a[2].equalsIgnoreCase("dryrun");
                int removed = plots.cleanupExpiredClaims(days, dry);
                sender.sendMessage(prefix() + ChatColor.YELLOW + (dry ? "Would remove " : "Removed ") + removed + " claim(s).");
                return true;
            }
            case "reload": {
                if (!sender.hasPermission("proshield.admin.reload")) return err(sender, "No permission.");
                plugin.reloadAllConfigs();
                sender.sendMessage(prefix() + ChatColor.GREEN + "Config reloaded.");
                return true;
            }
            default:
                return err(sender, "Unknown subcommand.");
        }
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }

    private boolean err(CommandSender s, String m) {
        s.sendMessage(prefix() + ChatColor.RED + m);
        return true;
    }
}
