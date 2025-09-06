package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public ProShieldCommand(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    private String px() { return plugin.getConfig().getString("messages.prefix", ""); }

    /** Treat OP as having any permission (fallback for raw servers). */
    private boolean has(Player p, String node) {
        return p.isOp() || p.hasPermission(node);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("Players only."); return true; }
        Player p = (Player) sender;

        // Base gate: allow if proshield.use OR OP
        if (!has(p, "proshield.use")) {
            p.sendMessage(px() + ChatColor.RED + "You don't have permission.");
            return true;
        }

        Location loc = p.getLocation();

        if (args.length == 0) {
            p.sendMessage(px() + ChatColor.AQUA + "ProShield is running. Use /" + label + " help");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                p.sendMessage(ChatColor.AQUA + "---- ProShield ----");
                p.sendMessage("/" + label + " claim | unclaim | info");
                p.sendMessage("/" + label + " trust <player> | untrust <player> | trusted");
                p.sendMessage("/" + label + " show | compass");
                if (has(p, "proshield.admin")) {
                    p.sendMessage("/" + label + " bypass");
                    p.sendMessage("/" + label + " admin claim <player> | admin unclaim");
                }
                return true;

            case "claim": {
                boolean ok = plotManager.createClaim(p.getUniqueId(), loc);
                if (ok) {
                    p.sendMessage(px() + ChatColor.GREEN + "Chunk claimed successfully!");
                    plugin.getBorderVisualizer().showChunkBorder(p, loc);
                } else {
                    p.sendMessage(px() + ChatColor.RED + "Cannot claim here (already claimed or limit reached).");
                }
                return true;
            }

            case "unclaim": {
                boolean ok = plotManager.removeClaim(p.getUniqueId(), loc, false);
                if (ok) {
                    p.sendMessage(px() + ChatColor.YELLOW + "Chunk unclaimed.");
                } else {
                    p.sendMessage(px() + ChatColor.RED + "You don't own this claim.");
                }
                return true;
            }

            case "info": {
                var cOpt = plotManager.getClaim(loc);
                if (cOpt.isEmpty()) { p.sendMessage(px() + ChatColor.GRAY + "This chunk is not claimed."); return true; }
                var c = cOpt.get();
                String owner = plotManager.ownerName(c.getOwner());
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(c.getCreatedAt()));
                p.sendMessage(px() + ChatColor.GOLD + "Owner: " + owner);
                p.sendMessage(px() + ChatColor.GOLD + "World: " + c.getWorld() + "  Chunk: " + c.getChunkX() + "," + c.getChunkZ());
                var trusted = plotManager.listTrusted(loc);
                p.sendMessage(px() + ChatColor.GOLD + "Trusted: " + (trusted.isEmpty() ? "(none)" : String.join(", ", trusted)));
                p.sendMessage(px() + ChatColor.GOLD + "Created: " + date);
                return true;
            }

            case "trust": {
                if (args.length < 2) { p.sendMessage(px() + "Usage: /" + label + " trust <player>"); return true; }
                OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[1]);
                UUID tuid = target.getUniqueId();
                boolean ok = plotManager.trust(p.getUniqueId(), loc, tuid);
                p.sendMessage(px() + (ok ? ChatColor.GREEN + "Trusted " + args[1] + "." : ChatColor.RED + "Failed. Are you the owner?"));
                return true;
            }

            case "untrust": {
                if (args.length < 2) { p.sendMessage(px() + "Usage: /" + label + " untrust <player>"); return true; }
                OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[1]);
                boolean ok = plotManager.untrust(p.getUniqueId(), loc, target.getUniqueId());
                p.sendMessage(px() + (ok ? ChatColor.YELLOW + "Removed trust for " + args[1] + "." : ChatColor.RED + "Failed. Are you the owner?"));
                return true;
            }

            case "trusted": {
                var list = plotManager.listTrusted(loc);
                p.sendMessage(px() + ChatColor.AQUA + "Trusted: " + (list.isEmpty() ? "(none)" : String.join(", ", list)));
                return true;
            }

            case "show": {
                plugin.getBorderVisualizer().showChunkBorder(p, loc);
                p.sendMessage(px() + ChatColor.AQUA + "Showing chunk borders.");
                return true;
            }

            case "compass": { // ✅ OP fallback here too
                if (!(has(p, "proshield.compass") || has(p, "proshield.admin"))) {
                    p.sendMessage(px() + ChatColor.RED + "No permission.");
                    return true;
                }
                if (!GUIManager.hasProShieldCompass(p)) {
                    p.getInventory().addItem(GUIManager.createAdminCompass());
                    p.sendMessage(px() + ChatColor.GREEN + "Given ProShield Compass.");
                } else {
                    p.sendMessage(px() + ChatColor.YELLOW + "You already have a ProShield Compass.");
                }
                return true;
            }

            case "bypass": {
                if (!has(p, "proshield.bypass")) { p.sendMessage(px() + ChatColor.RED + "No permission."); return true; }
                plugin.toggleBypass(p.getUniqueId());
                p.sendMessage(px() + (plugin.isBypassing(p.getUniqueId()) ? ChatColor.YELLOW + "Bypass: ON" : ChatColor.YELLOW + "Bypass: OFF"));
                return true;
            }

            case "admin": {
                if (!has(p, "proshield.admin")) { p.sendMessage(px() + ChatColor.RED + "No permission."); return true; }
                if (args.length < 2) { p.sendMessage(px() + "Usage: /" + label + " admin <claim|unclaim> [player]"); return true; }

                if (args[1].equalsIgnoreCase("claim")) {
                    if (args.length < 3) { p.sendMessage(px() + "Usage: /" + label + " admin claim <player>"); return true; }
                    OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[2]);
                    boolean ok = plotManager.createClaim(target.getUniqueId(), loc);
                    p.sendMessage(px() + (ok ? ChatColor.GREEN + "Claimed for " + args[2] + "." : ChatColor.RED + "Failed to claim here."));
                    return true;
                }
                if (args[1].equalsIgnoreCase("unclaim")) {
                    boolean ok = plotManager.removeClaim(p.getUniqueId(), loc, true);
                    p.sendMessage(px() + (ok ? ChatColor.YELLOW + "Force-unclaimed." : ChatColor.RED + "Nothing to unclaim."));
                    return true;
                }
                p.sendMessage(px() + "Usage: /" + label + " admin <claim|unclaim> [player]");
                return true;
            }

            default:
                p.sendMessage(px() + "Unknown subcommand. Try /" + label + " help");
                return true;
        }
    }
}
