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

import java.util.Set;
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
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(prefix() + ChatColor.AQUA + "ProShield " + plugin.getDescription().getVersion());
            sender.sendMessage(prefix() + ChatColor.YELLOW + "Use /proshield help for commands.");
            return true;
        }

        String sub = args[0].toLowerCase();

        // --- help ---
        if (sub.equals("help")) {
            sender.sendMessage(ChatColor.AQUA + "ProShield Commands:");
            sender.sendMessage(ChatColor.GRAY + "/proshield claim | unclaim | info | compass");
            sender.sendMessage(ChatColor.GRAY + "/proshield trust <player> | untrust <player> | trusted");
            sender.sendMessage(ChatColor.GRAY + "/proshield bypass <on|off|toggle>  (admin)");
            sender.sendMessage(ChatColor.GRAY + "/proshield reload                  (admin)");
            sender.sendMessage(ChatColor.GRAY + "/proshield expired list|restore|purge  (admin)");
            sender.sendMessage(ChatColor.GRAY + "/proshield settings adminUnlimited <on|off|toggle>  (owner/console)");
            sender.sendMessage(ChatColor.GRAY + "/proshield api givecompass|expand|grant  (console/automation)");
            return true;
        }

        // ===== player commands =====

        if (sub.equals("claim")) {
            if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
            if (!p.hasPermission("proshield.use")) { p.sendMessage(prefix() + ChatColor.RED + "No permission."); return true; }
            boolean ok = plotManager.createClaim(p.getUniqueId(), p.getLocation());
            p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Claim created." : ChatColor.RED + "Already claimed or limit reached."));
            return true;
        }

        if (sub.equals("unclaim")) {
            if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
            if (!p.hasPermission("proshield.use")) { p.sendMessage(prefix() + ChatColor.RED + "No permission."); return true; }
            boolean ok = plotManager.removeClaim(p.getUniqueId(), p.getLocation(), false);
            p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Claim removed." : ChatColor.RED + "No claim here or not owner."));
            return true;
        }

        if (sub.equals("info")) {
            if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
            plotManager.getClaim(p.getLocation()).ifPresentOrElse(c -> {
                String owner = plotManager.ownerName(c.getOwner());
                var trusted = plotManager.listTrusted(p.getLocation());
                p.sendMessage(prefix() + ChatColor.AQUA + "Owner: " + owner);
                p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + (trusted.isEmpty() ? "(none)" : String.join(", ", trusted)));
            }, () -> p.sendMessage(prefix() + ChatColor.GRAY + "No claim in this chunk."));
            return true;
        }

        if (sub.equals("compass")) {
            if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
            if (!p.hasPermission("proshield.compass") && !p.hasPermission("proshield.admin")) {
                p.sendMessage(prefix() + ChatColor.RED + "No permission."); return true;
            }
            p.getInventory().addItem(GUIManager.createAdminCompass());
            p.sendMessage(prefix() + ChatColor.GREEN + "Compass given.");
            return true;
        }

        if (sub.equals("trust") || sub.equals("untrust")) {
            if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
            if (!p.hasPermission("proshield.use")) { p.sendMessage(prefix() + ChatColor.RED + "No permission."); return true; }
            if (args.length < 2) { p.sendMessage(prefix() + ChatColor.YELLOW + "Usage: /proshield " + sub + " <player>"); return true; }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (target == null || (target.getName() == null && !target.hasPlayedBefore())) {
                p.sendMessage(prefix() + ChatColor.RED + "Unknown player."); return true;
            }
            boolean ok = sub.equals("trust")
                    ? plotManager.trust(p.getUniqueId(), p.getLocation(), target.getUniqueId())
                    : plotManager.untrust(p.getUniqueId(), p.getLocation(), target.getUniqueId());
            p.sendMessage(prefix() + (ok ? ChatColor.GREEN + (sub.equals("trust") ? "Trusted " : "Untrusted ") + target.getName()
                                         : ChatColor.RED + "Failed. Are you the owner of this claim?"));
            return true;
        }

        if (sub.equals("trusted")) {
            if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
            var list = plotManager.listTrusted(p.getLocation());
            p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + (list.isEmpty() ? "(none)" : String.join(", ", list)));
            return true;
        }

        // ===== admin / owner commands =====

        if (sub.equals("bypass")) {
            if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
            if (!p.hasPermission("proshield.bypass")) { p.sendMessage(prefix() + ChatColor.RED + "No permission."); return true; }

            boolean on;
            if (args.length == 1 || args[1].equalsIgnoreCase("toggle")) {
                on = !p.hasMetadata("proshield_bypass");
            } else {
                on = args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true");
            }

            if (on) {
                p.setMetadata("proshield_bypass", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
                p.sendMessage(prefix() + ChatColor.GREEN + "Bypass enabled.");
            } else {
                p.removeMetadata("proshield_bypass", plugin);
                p.sendMessage(prefix() + ChatColor.YELLOW + "Bypass disabled.");
            }
            return true;
        }

        if (sub.equals("reload")) {
            if (!sender.hasPermission("proshield.admin.reload")) { sender.sendMessage(prefix() + ChatColor.RED + "No permission."); return true; }
            plugin.reloadAllConfigs();
            sender.sendMessage(prefix() + ChatColor.GREEN + "Reloaded.");
            return true;
        }

        if (sub.equals("expired")) {
            if (!sender.hasPermission("proshield.admin")) { sender.sendMessage(prefix() + ChatColor.RED + "No permission."); return true; }
            if (args.length < 2) { sender.sendMessage(prefix() + ChatColor.YELLOW + "Usage: /proshield expired <list|restore|purge>"); return true; }

            switch (args[1].toLowerCase()) {
                case "list" -> {
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
                }
                case "restore" -> {
                    if (args.length < 3) { sender.sendMessage(prefix() + ChatColor.YELLOW + "Usage: /proshield expired restore <world:chunkX:chunkZ>"); return true; }
                    String restoreKey = args[2];
                    boolean ok = plotManager.restoreExpiredClaim(restoreKey);
                    sender.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Restored " + restoreKey
                                                      : ChatColor.RED + "Failed to restore " + restoreKey));
                    return true;
                }
                case "purge" -> {
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
                        purged = purgeExpired(-1);
                    }
                    sender.sendMessage(prefix() + ChatColor.GREEN + "Purged " + purged + " expired claim(s).");
                    return true;
                }
            }
        }

        if (sub.equals("settings")) {
            if (args.length >= 2 && args[1].equalsIgnoreCase("adminUnlimited")) {
                boolean allowed = !(sender instanceof Player) || sender.hasPermission("proshield.owner");
                if (!allowed) { sender.sendMessage(prefix() + ChatColor.RED + "Only the server owner (or console) can change this."); return true; }

                boolean current = plugin.getConfig().getBoolean("permissions.admin-includes-unlimited", false);

                if (args.length == 2 || args[2].equalsIgnoreCase("toggle")) {
                    current = !current;
                } else if (args[2].equalsIgnoreCase("on") || args[2].equalsIgnoreCase("true")) {
                    current = true;
                } else if (args[2].equalsIgnoreCase("off") || args[2].equalsIgnoreCase("false")) {
                    current = false;
                } else {
                    sender.sendMessage(prefix() + ChatColor.YELLOW +
                            "Usage: /proshield settings adminUnlimited <on|off|toggle>");
                    sender.sendMessage(prefix() + ChatColor.GRAY +
                            "Currently: " + (plugin.getConfig().getBoolean("permissions.admin-includes-unlimited", false) ? "ON" : "OFF"));
                    return true;
                }

                plugin.getConfig().set("permissions.admin-includes-unlimited", current);
                plugin.saveConfig();
                sender.sendMessage(prefix() + ChatColor.GREEN +
                        "Admin-includes-unlimited is now " + (current ? "ON" : "OFF") + ".");
                plugin.getLogger().info("[ProShield] permissions.admin-includes-unlimited set to " + current + " by " + sender.getName());
                return true;
            }
            sender.sendMessage(prefix() + ChatColor.YELLOW + "Usage: /proshield settings adminUnlimited <on|off|toggle>");
            return true;
        }

        // ===== API hooks for shops (console/automation, perm-guarded, optional) =====

        if (sub.equals("api")) {
            if (!plugin.getConfig().getBoolean("api.enable-command-hooks", true)) {
                sender.sendMessage(prefix() + ChatColor.RED + "API command hooks are disabled in config.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(prefix() + ChatColor.YELLOW + "Usage: /proshield api <givecompass|expand|grant> ...");
                return true;
            }
            String api = args[1].toLowerCase();

            if (api.equals("givecompass")) {
                if (!sender.hasPermission("proshield.api.givecompass")) { sender.sendMessage(prefix() + ChatColor.RED + "No permission."); return true; }
                if (args.length < 3) { sender.sendMessage(prefix() + ChatColor.YELLOW + "Usage: /proshield api givecompass <player>"); return true; }
                Player target = Bukkit.getPlayerExact(args[2]);
                if (target == null) { sender.sendMessage(prefix() + ChatColor.RED + "Player not online."); return true; }
                target.getInventory().addItem(GUIManager.createAdminCompass());
                sender.sendMessage(prefix() + ChatColor.GREEN + "Compass given to " + target.getName() + ".");
                return true;
            }

            if (api.equals("expand")) {
                if (!sender.hasPermission("proshield.api.expand")) { sender.sendMessage(prefix() + ChatColor.RED + "No permission."); return true; }
                if (args.length < 4) { sender.sendMessage(prefix() + ChatColor.YELLOW + "Usage: /proshield api expand <player> <amount>"); return true; }
                Player target = Bukkit.getPlayerExact(args[2]);
                if (target == null) { sender.sendMessage(prefix() + ChatColor.RED + "Player not online."); return true; }
                int amount;
                try { amount = Integer.parseInt(args[3]); } catch (NumberFormatException ex) {
                    sender.sendMessage(prefix() + ChatColor.RED + "Amount must be a number."); return true;
                }
                // Placeholder: you can implement chunk radius expansion logic later.
                sender.sendMessage(prefix() + ChatColor.GREEN + "Queued expansion of " + amount + " (not yet implemented).");
                return true;
            }

            if (api.equals("grant")) {
                if (!sender.hasPermission("proshield.api.grant")) { sender.sendMessage(prefix() + ChatColor.RED + "No permission."); return true; }
                if (args.length < 4) { sender.sendMessage(prefix() + ChatColor.YELLOW + "Usage: /proshield api grant <player> <permission>"); return true; }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
                String perm = args[3];
                // We do not alter permissions ourselves (that’s the job of a perms plugin).
                // Instead, run server command for compatibility with common perms plugins:
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target.getName() + " permission set " + perm + " true");
                sender.sendMessage(prefix() + ChatColor.GREEN + "Attempted to grant " + perm + " to " + target.getName() + " via LuckPerms.");
                return true;
            }

            sender.sendMessage(prefix() + ChatColor.RED + "Unknown api subcommand.");
            return true;
        }

        sender.sendMessage(prefix() + ChatColor.RED + "Unknown subcommand. Use /proshield help");
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
