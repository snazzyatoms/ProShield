// path: src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plots;

    public ProShieldCommand(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots  = plots;
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // /proshield
        if (args.length == 0) {
            sender.sendMessage(prefix() + ChatColor.AQUA + "ProShield v" + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.GRAY + "Usage: /" + label + " <claim|unclaim|info|compass|trust|untrust|trusted|reload|expire>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "claim": {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(prefix() + ChatColor.RED + "Players only.");
                    return true;
                }
                boolean ok = plots.createClaim(p.getUniqueId(), p.getLocation());
                if (ok) {
                    sender.sendMessage(prefix() + ChatColor.GREEN + "Claim created for this chunk.");
                } else {
                    sender.sendMessage(prefix() + ChatColor.RED + "This chunk is already claimed or you reached your limit.");
                }
                return true;
            }

            case "unclaim": {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(prefix() + ChatColor.RED + "Players only.");
                    return true;
                }
                boolean ok = plots.removeClaim(p.getUniqueId(), p.getLocation(), false);
                if (ok) {
                    sender.sendMessage(prefix() + ChatColor.GREEN + "Claim removed.");
                } else {
                    sender.sendMessage(prefix() + ChatColor.RED + "No claim here or you are not the owner.");
                }
                return true;
            }

            case "info": {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(prefix() + ChatColor.RED + "Players only.");
                    return true;
                }
                plots.getClaim(p.getLocation()).ifPresentOrElse(c -> {
                    String owner = plots.ownerName(c.getOwner());
                    List<String> trusted = plots.listTrusted(p.getLocation());
                    sender.sendMessage(prefix() + ChatColor.AQUA + "Owner: " + owner);
                    sender.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " +
                            (trusted.isEmpty() ? "(none)" : String.join(", ", trusted)));
                }, () -> sender.sendMessage(prefix() + ChatColor.GRAY + "No claim in this chunk."));
                return true;
            }

            case "compass": {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(prefix() + ChatColor.RED + "Players only.");
                    return true;
                }
                if (!p.hasPermission("proshield.compass") && !p.hasPermission("proshield.admin")) {
                    sender.sendMessage(prefix() + ChatColor.RED + "No permission.");
                    return true;
                }
                ItemStack compass = GUIManager.createAdminCompass();
                p.getInventory().addItem(compass);
                sender.sendMessage(prefix() + ChatColor.GREEN + "Admin compass added to your inventory.");
                return true;
            }

            case "trust": {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(prefix() + ChatColor.RED + "Players only.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(prefix() + ChatColor.YELLOW + "Usage: /" + label + " trust <player>");
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                UUID targetId = target.getUniqueId();
                boolean ok = plots.trust(p.getUniqueId(), p.getLocation(), targetId);
                if (ok) {
                    sender.sendMessage(prefix() + ChatColor.GREEN + "Trusted " +
                            (target.getName() != null ? target.getName() : targetId.toString()) + ".");
                } else {
                    sender.sendMessage(prefix() + ChatColor.RED + "You must own this claim to trust players.");
                }
                return true;
            }

            case "untrust": {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(prefix() + ChatColor.RED + "Players only.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(prefix() + ChatColor.YELLOW + "Usage: /" + label + " untrust <player>");
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                UUID targetId = target.getUniqueId();
                boolean ok = plots.untrust(p.getUniqueId(), p.getLocation(), targetId);
                if (ok) {
                    sender.sendMessage(prefix() + ChatColor.GREEN + "Removed trust for " +
                            (target.getName() != null ? target.getName() : targetId.toString()) + ".");
                } else {
                    sender.sendMessage(prefix() + ChatColor.RED + "You must own this claim to untrust players.");
                }
                return true;
            }

            case "trusted": {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(prefix() + ChatColor.RED + "Players only.");
                    return true;
                }
                List<String> list = plots.listTrusted(p.getLocation());
                if (list.isEmpty()) {
                    sender.sendMessage(prefix() + ChatColor.GRAY + "No trusted players here.");
                } else {
                    sender.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + String.join(", ", list));
                }
                return true;
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

            case "expire":
            case "cleanup":
            case "purgeexpired": {
                // /proshield expire <days> [force]
                if (!sender.hasPermission("proshield.admin.expired.purge")) {
                    sender.sendMessage(prefix() + ChatColor.RED + "No permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(prefix() + ChatColor.YELLOW + "Usage: /" + label + " " + sub + " <days> [force]");
                    return true;
                }
                int days;
                try {
                    days = Integer.parseInt(args[1]);
                    if (days < 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    sender.sendMessage(prefix() + ChatColor.RED + "Days must be a non-negative number.");
                    return true;
                }
                boolean force = args.length >= 3 && args[2].equalsIgnoreCase("force");

                int removed = plots.cleanupExpiredClaims(days, force);
                sender.sendMessage(prefix() + ChatColor.GREEN + "Expired cleanup removed " + removed + " claim(s).");
                return true;
            }

            default:
                sender.sendMessage(prefix() + ChatColor.GRAY + "Unknown subcommand. Use /" + label + " for help.");
                return true;
        }
    }
}
