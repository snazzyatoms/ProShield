// path: src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.ClaimPreview;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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

    private void sendHelp(CommandSender s) {
        s.sendMessage(prefix() + ChatColor.AQUA + "Commands:");
        s.sendMessage(ChatColor.GRAY + "/proshield" + ChatColor.DARK_GRAY + " — help");
        s.sendMessage(ChatColor.GRAY + "/proshield claim" + ChatColor.DARK_GRAY + " — claim current chunk");
        s.sendMessage(ChatColor.GRAY + "/proshield unclaim" + ChatColor.DARK_GRAY + " — remove your claim here");
        s.sendMessage(ChatColor.GRAY + "/proshield info" + ChatColor.DARK_GRAY + " — info about this claim");
        s.sendMessage(ChatColor.GRAY + "/proshield trust <player> [role]" + ChatColor.DARK_GRAY + " — trust (optionally with role)");
        s.sendMessage(ChatColor.GRAY + "/proshield untrust <player>" + ChatColor.DARK_GRAY + " — revoke access");
        s.sendMessage(ChatColor.GRAY + "/proshield trusted" + ChatColor.DARK_GRAY + " — list trusted players");
        s.sendMessage(ChatColor.GRAY + "/proshield compass" + ChatColor.DARK_GRAY + " — give yourself the compass");
        s.sendMessage(ChatColor.GRAY + "/proshield bypass <on|off|toggle>" + ChatColor.DARK_GRAY + " — admin bypass");
        s.sendMessage(ChatColor.GRAY + "/proshield purgeexpired <days> [dryrun]" + ChatColor.DARK_GRAY + " — admin expiry cleanup");
        s.sendMessage(ChatColor.GRAY + "/proshield preview [seconds]" + ChatColor.DARK_GRAY + " — show claim border preview");
        s.sendMessage(ChatColor.GRAY + "/proshield reload" + ChatColor.DARK_GRAY + " — reload config");
        s.sendMessage(ChatColor.GRAY + "/proshield debug <on|off>" + ChatColor.DARK_GRAY + " — toggle debug logging");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("claim")) {
            if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + "Players only."); return true; }
            boolean ok = plots.createClaim(p.getUniqueId(), p.getLocation());
            p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Claim created."
                                         : ChatColor.RED + "Already claimed or you reached your limit."));
            return true;
        }

        if (sub.equals("unclaim")) {
            if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + "Players only."); return true; }
            boolean ok = plots.removeClaim(p.getUniqueId(), p.getLocation(), false);
            p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Claim removed."
                                         : ChatColor.RED + "No claim here or you are not the owner."));
            return true;
        }

        if (sub.equals("info")) {
            if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + "Players only."); return true; }
            plots.getClaim(p.getLocation()).ifPresentOrElse(c -> {
                String owner = plots.ownerName(c.getOwner());
                var trusted = plots.listTrusted(p.getLocation());
                p.sendMessage(prefix() + ChatColor.AQUA + "Owner: " + owner);
                p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + (trusted.isEmpty() ? "(none)" : String.join(", ", trusted)));
            }, () -> sender.sendMessage(prefix() + ChatColor.GRAY + "No claim in this chunk."));
            return true;
        }

        if (sub.equals("trust")) {
            if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + "Players only."); return true; }
            if (args.length < 2) { p.sendMessage(prefix() + ChatColor.RED + "Usage: /proshield trust <player> [role]"); return true; }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (target == null || (target.getName() == null && !target.hasPlayedBefore())) {
                p.sendMessage(prefix() + ChatColor.RED + "Unknown player: " + args[1]);
                return true;
            }

            boolean ok;
            if (args.length >= 3) {
                String roleName = args[2]; // Visitor/Member/Container/Builder/Co-Owner etc.
                ok = plots.trustWithRole(p.getUniqueId(), p.getLocation(), target.getUniqueId(), roleName);
            } else {
                ok = plots.trust(p.getUniqueId(), p.getLocation(), target.getUniqueId());
            }

            p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Trusted " + target.getName() + "."
                                         : ChatColor.RED + "Failed to trust here."));
            return true;
        }

        if (sub.equals("untrust")) {
            if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + "Players only."); return true; }
            if (args.length < 2) { p.sendMessage(prefix() + ChatColor.RED + "Usage: /proshield untrust <player>"); return true; }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (target == null || target.getUniqueId() == null) {
                p.sendMessage(prefix() + ChatColor.RED + "Unknown player: " + args[1]);
                return true;
            }
            boolean ok = plots.untrust(p.getUniqueId(), p.getLocation(), target.getUniqueId());
            p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Untrusted " + target.getName() + "."
                                         : ChatColor.RED + "Failed to untrust here."));
            return true;
        }

        if (sub.equals("trusted")) {
            if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + "Players only."); return true; }
            var list = plots.listTrusted(p.getLocation());
            p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + (list.isEmpty() ? "(none)" : String.join(", ", list)));
            return true;
        }

        if (sub.equals("compass")) {
            if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + "Players only."); return true; }
            p.getInventory().addItem(GUIManager.createAdminCompass());
            p.sendMessage(prefix() + ChatColor.GREEN + "Compass added.");
            return true;
        }

        if (sub.equals("bypass")) {
            if (!sender.hasPermission("proshield.bypass")) { sender.sendMessage(prefix() + ChatColor.RED + "No permission."); return true; }
            if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + "Players only."); return true; }
            if (args.length < 2 || args[1].equalsIgnoreCase("toggle")) {
                boolean now = !p.hasMetadata("proshield_bypass");
                if (now) p.setMetadata("proshield_bypass", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
                else     p.removeMetadata("proshield_bypass", plugin);
                p.sendMessage(prefix() + (now ? ChatColor.YELLOW + "Bypass enabled." : ChatColor.YELLOW + "Bypass disabled."));
            } else {
                boolean on = args[1].equalsIgnoreCase("on");
                if (on) p.setMetadata("proshield_bypass", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
                else    p.removeMetadata("proshield_bypass", plugin);
                p.sendMessage(prefix() + (on ? ChatColor.YELLOW + "Bypass enabled." : ChatColor.YELLOW + "Bypass disabled."));
            }
            return true;
        }

        if (sub.equals("purgeexpired")) {
            if (!sender.hasPermission("proshield.admin.expired.purge")) {
                sender.sendMessage(prefix() + ChatColor.RED + "No permission.");
                return true;
            }
            if (args.length < 2) { sender.sendMessage(prefix() + ChatColor.RED + "Usage: /proshield purgeexpired <days> [dryrun]"); return true; }
            int days;
            try { days = Integer.parseInt(args[1]); } catch (Exception ex) {
                sender.sendMessage(prefix() + ChatColor.RED + "Invalid number: " + args[1]); return true;
            }
            boolean dry = args.length >= 3 && args[2].equalsIgnoreCase("dryrun");
            int count = plots.cleanupExpiredClaims(days, !dry);
            sender.sendMessage(prefix() + ChatColor.YELLOW + (dry ? "Preview: " : "Removed: ") + count + " claim(s).");
            return true;
        }

        if (sub.equals("preview")) {
            if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + "Players only."); return true; }
            long seconds = 6L;
            if (args.length >= 2) {
                try { seconds = Math.max(2L, Math.min(30L, Long.parseLong(args[1]))); } catch (Exception ignored) {}
            }
            ClaimPreview.start(p, seconds * 20L);
            p.sendMessage(prefix() + ChatColor.AQUA + "Showing claim border for " + seconds + "s.");
            return true;
        }

        if (sub.equals("reload")) {
            if (!sender.hasPermission("proshield.admin.reload")) { sender.sendMessage(prefix() + ChatColor.RED + "No permission."); return true; }
            plugin.reloadAllConfigs();
            sender.sendMessage(prefix() + ChatColor.GREEN + "Config reloaded.");
            return true;
        }

        if (sub.equals("debug")) {
            if (!sender.hasPermission("proshield.admin")) { sender.sendMessage(prefix() + ChatColor.RED + "No permission."); return true; }
            if (args.length < 2) { sender.sendMessage(prefix() + ChatColor.YELLOW + "Debug is " + (plugin.isDebug() ? "ON" : "OFF")); return true; }
            boolean on = args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true");
            plugin.setDebug(on);
            sender.sendMessage(prefix() + ChatColor.YELLOW + "Debug is now " + (on ? "ON" : "OFF"));
            return true;
        }

        sendHelp(sender);
        return true;
    }
}
