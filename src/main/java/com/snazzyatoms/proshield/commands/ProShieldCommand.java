package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public ProShieldCommand(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r ")) + ChatColor.RESET;
    }

    private void sendUsage(CommandSender s) {
        s.sendMessage(prefix() + ChatColor.AQUA + "Usage:");
        s.sendMessage(ChatColor.GRAY + " /proshield");
        s.sendMessage(ChatColor.GRAY + " /proshield claim");
        s.sendMessage(ChatColor.GRAY + " /proshield unclaim");
        s.sendMessage(ChatColor.GRAY + " /proshield info");
        s.sendMessage(ChatColor.GRAY + " /proshield compass");
        s.sendMessage(ChatColor.GRAY + " /proshield trust <player>");
        s.sendMessage(ChatColor.GRAY + " /proshield untrust <player>");
        s.sendMessage(ChatColor.GRAY + " /proshield trusted");
        s.sendMessage(ChatColor.GRAY + " /proshield bypass <on|off|toggle>");
        s.sendMessage(ChatColor.GRAY + " /proshield expired review [days]");
        s.sendMessage(ChatColor.GRAY + " /proshield expired purge [days]");
        s.sendMessage(ChatColor.GRAY + " /proshield reload");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(prefix() + ChatColor.AQUA + "ProShield is running. Claims: " + plotManager.getClaimCount());
            sendUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {

            case "claim": {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(prefix() + ChatColor.RED + "Players only.");
                    return true;
                }
                if (!p.hasPermission("proshield.use")) {
                    p.sendMessage(prefix() + ChatColor.RED + "No permission.");
                    return true;
                }
                Location loc = p.getLocation();
                boolean ok = plotManager.createClaim(p.getUniqueId(), loc);
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Claim created for this chunk."
                        : ChatColor.RED + "Already claimed here or you reached your claim limit."));
                return true;
            }

            case "unclaim": {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(prefix() + ChatColor.RED + "Players only.");
                    return true;
                }
                if (!p.hasPermission("proshield.use")) {
                    p.sendMessage(prefix() + ChatColor.RED + "No permission.");
                    return true;
                }
                boolean admin = p.hasPermission("proshield.admin");
                boolean ok = plotManager.removeClaim(p.getUniqueId(), p.getLocation(), admin);
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Claim removed."
                        : ChatColor.RED + "No claim here or you are not the owner."));
                return true;
            }

            case "info": {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(prefix() + ChatColor.RED + "Players only.");
                    return true;
                }
                plotManager.getClaim(p.getLocation()).ifPresentOrElse(c -> {
                    String owner = plotManager.ownerName(c.getOwner());
                    var trusted = plotManager.listTrusted(p.getLocation());
                    p.sendMessage(prefix() + ChatColor.AQUA + "Owner: " + owner);
                    p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " +
                            (trusted.isEmpty() ? ChatColor.GRAY + "(none)" : String.join(", ", trusted)));
                }, () -> p.sendMessage(prefix() + ChatColor.GRAY + "This chunk is unclaimed."));
                return true;
            }

            case "compass": {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(prefix() + ChatColor.RED + "Players only.");
                    return true;
                }
                if (!(p.isOp() || p.hasPermission("proshield.compass") || p.hasPermission("proshield.admin"))) {
                    p.sendMessage(prefix() + ChatColor.RED + "No permission.");
                    return true;
                }
                p.getInventory().addItem(GUIManager.createAdminCompass());
                p.sendMessage(prefix() + ChatColor.GREEN + "ProShield compass added to your inventory.");
                return true;
            }

            case "trust": {
                if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + ChatColor.RED + "Players only."); return true; }
                if (args.length < 2) { p.sendMessage(prefix() + ChatColor.RED + "Usage: /proshield trust <player>"); return true; }

                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { p.sendMessage(prefix() + ChatColor.RED + "Player not found."); return true; }

                boolean ok = plotManager.trust(p.getUniqueId(), p.getLocation(), target.getUniqueId());
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Trusted " + target.getName() + "."
                        : ChatColor.RED + "No claim here or you are not the owner."));
                return true;
            }

            case "untrust": {
                if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + ChatColor.RED + "Players only."); return true; }
                if (args.length < 2) { p.sendMessage(prefix() + ChatColor.RED + "Usage: /proshield untrust <player>"); return true; }

                Player target = Bukkit.getPlayerExact(args[1]);
                UUID tuid = (target != null) ? target.getUniqueId() : null;
                if (tuid == null) { p.sendMessage(prefix() + ChatColor.RED + "Player not found (must be online)."); return true; }

                boolean ok = plotManager.untrust(p.getUniqueId(), p.getLocation(), tuid);
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Untrusted " + target.getName() + "."
                        : ChatColor.RED + "No claim here or you are not the owner."));
                return true;
            }

            case "trusted": {
                if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + ChatColor.RED + "Players only."); return true; }
                var list = plotManager.listTrusted(p.getLocation());
                p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " +
                        (list.isEmpty() ? ChatColor.GRAY + "(none)" : String.join(", ", list)));
                return true;
            }

            case "bypass": {
                if (!(sender instanceof Player p)) { sender.sendMessage(prefix() + ChatColor.RED + "Players only."); return true; }
                if (!p.hasPermission("proshield.bypass")) { p.sendMessage(prefix() + ChatColor.RED + "No permission."); return true; }

                if (args.length < 2 || args[1].equalsIgnoreCase("toggle")) {
                    boolean now = !p.hasMetadata("proshield_bypass");
                    setBypass(p, now);
                    p.sendMessage(prefix() + ChatColor.YELLOW + "Bypass " + (now ? "enabled" : "disabled") + ".");
                    return true;
                }
                if (args[1].equalsIgnoreCase("on"))  { setBypass(p, true);  p.sendMessage(prefix() + ChatColor.YELLOW + "Bypass enabled.");  return true; }
                if (args[1].equalsIgnoreCase("off")) { setBypass(p, false); p.sendMessage(prefix() + ChatColor.YELLOW + "Bypass disabled."); return true; }
                p.sendMessage(prefix() + ChatColor.RED + "Usage: /proshield bypass <on|off|toggle>");
                return true;
            }

            case "expired": {
                // /proshield expired review [days]
                // /proshield expired purge  [days]
                if (!sender.hasPermission("proshield.admin")) {
                    sender.sendMessage(prefix() + ChatColor.RED + "No permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(prefix() + ChatColor.RED + "Usage: /proshield expired <review|purge> [days]");
                    return true;
                }
                String mode = args[1].toLowerCase();
                int days = plugin.getConfig().getInt("expiry.days", 30);
                if (args.length >= 3) {
                    try { days = Math.max(1, Integer.parseInt(args[2])); } catch (NumberFormatException ignored) {}
                }

                if (mode.equals("review")) {
                    if (!sender.hasPermission("proshield.admin.expired.review")) {
                        sender.sendMessage(prefix() + ChatColor.RED + "Missing: proshield.admin.expired.review");
                        return true;
                    }
                    // reviewOnly = true (counts, does not delete)
                    int removed = plotManager.cleanupExpiredClaims(days, true);
                    sender.sendMessage(prefix() + ChatColor.AQUA +
                            "Review done for " + days + " day(s). No claims were deleted. (See console for any logs.)");
                    return true;
                } else if (mode.equals("purge")) {
                    if (!sender.hasPermission("proshield.admin.expired.purge")) {
                        sender.sendMessage(prefix() + ChatColor.RED + "Missing: proshield.admin.expired.purge");
                        return true;
                    }
                    // reviewOnly = false (actually delete)
                    int removed = plotManager.cleanupExpiredClaims(days, false);
                    sender.sendMessage(prefix() + ChatColor.YELLOW +
                            "Purged " + removed + " expired claim(s) older than " + days + " day(s).");
                    return true;
                } else {
                    sender.sendMessage(prefix() + ChatColor.RED + "Usage: /proshield expired <review|purge> [days]");
                    return true;
                }
            }

            case "reload": {
                if (!sender.hasPermission("proshield.admin.reload")) {
                    sender.sendMessage(prefix() + ChatColor.RED + "No permission.");
                    return true;
                }
                plugin.reloadAllConfigs();
                sender.sendMessage(prefix() + ChatColor.GREEN + "Configs reloaded.");
                return true;
            }

            default:
                sendUsage(sender);
                return true;
        }
    }

    private void setBypass(Player p, boolean on) {
        if (on) {
            if (!p.hasMetadata("proshield_bypass")) {
                p.setMetadata("proshield_bypass", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
            }
        } else {
            p.removeMetadata("proshield_bypass", plugin);
        }
    }
}
