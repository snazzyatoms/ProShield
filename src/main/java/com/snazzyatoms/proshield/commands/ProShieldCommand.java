package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.ClaimPreviewTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
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

    private String px() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }

    private boolean needPlayer(CommandSender s) {
        if (!(s instanceof Player)) {
            s.sendMessage(px() + ChatColor.RED + plugin.getConfig().getString("messages.not-player", "Players only."));
            return true;
        }
        return false;
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] a) {
        if (a.length == 0) {
            s.sendMessage(px() + ChatColor.AQUA + "ProShield " + plugin.getDescription().getVersion());
            s.sendMessage(ChatColor.GRAY + "Use /proshield help");
            return true;
        }

        String sub = a[0].toLowerCase();
        switch (sub) {
            case "help":
                s.sendMessage(px() + ChatColor.AQUA + "Commands:");
                s.sendMessage(ChatColor.GRAY + "/proshield claim, unclaim, info");
                s.sendMessage(ChatColor.GRAY + "/proshield trust <player> [role], untrust <player>, trusted");
                s.sendMessage(ChatColor.GRAY + "/proshield preview [seconds], transfer <player>");
                if (s.hasPermission("proshield.admin")) {
                    s.sendMessage(ChatColor.DARK_GRAY + "/proshield purgeexpired <days> [dryrun], debug <on|off|toggle>, reload");
                }
                return true;

            case "claim": {
                if (needPlayer(s)) return true;
                Player p = (Player) s;
                boolean ok = plots.createClaim(p.getUniqueId(), p.getLocation());
                p.sendMessage(px() + (ok ?
                        ChatColor.GREEN + plugin.getConfig().getString("messages.claim-created") :
                        ChatColor.RED + plugin.getConfig().getString("messages.claim-exists")));
                return true;
            }

            case "unclaim": {
                if (needPlayer(s)) return true;
                Player p = (Player) s;
                boolean ok = plots.removeClaim(p.getUniqueId(), p.getLocation(), false);
                p.sendMessage(px() + (ok ?
                        ChatColor.GREEN + plugin.getConfig().getString("messages.claim-removed") :
                        ChatColor.RED + plugin.getConfig().getString("messages.no-claim-here")));
                return true;
            }

            case "info": {
                if (needPlayer(s)) return true;
                Player p = (Player) s;
                plots.getClaim(p.getLocation()).ifPresentOrElse(c -> {
                    p.sendMessage(px() + ChatColor.AQUA + "Owner: " + plots.ownerName(c.getOwner()));
                    p.sendMessage(px() + ChatColor.AQUA + "Trusted: " + plots.listTrusted(p.getLocation()));
                }, () -> p.sendMessage(px() + ChatColor.GRAY + plugin.getConfig().getString("messages.no-claim-here")));
                return true;
            }

            case "trust": {
                if (a.length < 2 || needPlayer(s)) return true;
                Player p = (Player) s;
                OfflinePlayer target = Bukkit.getOfflinePlayer(a[1]);
                String role = (a.length >= 3 ? a[2] : "member");
                boolean ok = plots.trustWithRole(p.getUniqueId(), p.getLocation(), target.getUniqueId(), role);
                p.sendMessage(px() + (ok ? ChatColor.GREEN + plugin.getConfig().getString("messages.trusted-added").replace("%player%", target.getName() == null ? a[1] : target.getName())
                        : ChatColor.RED + "Could not trust player here."));
                return true;
            }

            case "untrust": {
                if (a.length < 2 || needPlayer(s)) return true;
                Player p = (Player) s;
                OfflinePlayer target = Bukkit.getOfflinePlayer(a[1]);
                boolean ok = plots.untrust(p.getUniqueId(), p.getLocation(), target.getUniqueId());
                p.sendMessage(px() + (ok ? ChatColor.GREEN + plugin.getConfig().getString("messages.trusted-removed").replace("%player%", target.getName() == null ? a[1] : target.getName())
                        : ChatColor.RED + "Could not untrust here."));
                return true;
            }

            case "trusted": {
                if (needPlayer(s)) return true;
                Player p = (Player) s;
                p.sendMessage(px() + String.join(", ", plots.listTrusted(p.getLocation())));
                return true;
            }

            case "preview": {
                if (needPlayer(s)) return true;
                Player p = (Player) s;
                int seconds = 6;
                if (a.length >= 2) {
                    try { seconds = Math.max(2, Math.min(20, Integer.parseInt(a[1]))); } catch (Exception ignored) {}
                }
                new ClaimPreviewTask(p, plots).run(seconds);
                p.sendMessage(px() + ChatColor.GREEN + plugin.getConfig().getString("messages.preview-start").replace("%seconds%", String.valueOf(seconds)));
                p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.5f);
                return true;
            }

            case "transfer": {
                if (a.length < 2 || needPlayer(s)) return true;
                Player p = (Player) s;
                OfflinePlayer target = Bukkit.getOfflinePlayer(a[1]);
                boolean ok = plots.transferOwnership(p.getUniqueId(), p.getLocation(), target.getUniqueId());
                p.sendMessage(px() + (ok ? ChatColor.GREEN + plugin.getConfig().getString("messages.transfer-success").replace("%player%", target.getName() == null ? a[1] : target.getName())
                        : ChatColor.RED + plugin.getConfig().getString("messages.transfer-fail")));
                return true;
            }

            case "purgeexpired": {
                if (!s.hasPermission("proshield.admin.expired.purge")) {
                    s.sendMessage(px() + ChatColor.RED + plugin.getConfig().getString("messages.no-permission"));
                    return true;
                }
                if (a.length < 2) {
                    s.sendMessage(px() + ChatColor.GRAY + "Usage: /proshield purgeexpired <days> [dryrun]");
                    return true;
                }
                int days;
                try { days = Integer.parseInt(a[1]); } catch (Exception e) { s.sendMessage("Not a number."); return true; }
                boolean dry = (a.length >= 3 && a[2].equalsIgnoreCase("dryrun"));
                int removed = plots.cleanupExpiredClaims(days, !dry);
                s.sendMessage(px() + ChatColor.YELLOW + (dry ? "Preview: " : "Removed: ") + removed + " claim(s).");
                return true;
            }

            case "debug": {
                if (!s.hasPermission("proshield.admin.debug")) {
                    s.sendMessage(px() + ChatColor.RED + plugin.getConfig().getString("messages.no-permission"));
                    return true;
                }
                if (a.length < 2) {
                    plugin.setDebug(!plugin.isDebug());
                } else {
                    plugin.setDebug(a[1].equalsIgnoreCase("on") || a[1].equalsIgnoreCase("true"));
                }
                plugin.getConfig().set("debug.enabled", plugin.isDebug());
                plugin.saveConfig();
                s.sendMessage(px() + (plugin.isDebug() ? ChatColor.GREEN + plugin.getConfig().getString("messages.debug-on") : ChatColor.GREEN + plugin.getConfig().getString("messages.debug-off")));
                return true;
            }

            case "reload": {
                if (!s.hasPermission("proshield.admin.reload")) {
                    s.sendMessage(px() + ChatColor.RED + plugin.getConfig().getString("messages.no-permission"));
                    return true;
                }
                plugin.reloadAllConfigs();
                s.sendMessage(px() + ChatColor.GREEN + plugin.getConfig().getString("messages.reloaded"));
                return true;
            }
        }

        s.sendMessage(px() + ChatColor.RED + "Unknown subcommand. /proshield help");
        return true;
    }
}
