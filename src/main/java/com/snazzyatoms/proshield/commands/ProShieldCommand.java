// src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ProShieldCommand implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final GUIManager gui;
    private final CompassManager compass;
    private final MessagesUtil messages;

    public ProShieldCommand(ProShield plugin,
                            PlotManager plots,
                            ClaimRoleManager roles,
                            GUIManager gui,
                            CompassManager compass,
                            MessagesUtil messages) {
        this.plugin = plugin;
        this.plots = plots;
        this.roles = roles;
        this.gui = gui;
        this.compass = compass;
        this.messages = messages;
    }

    // -----------------------------------------------------
    // Command Routing
    // -----------------------------------------------------
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // /proshield → open main menu for players
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                messages.send(sender, "error.player-only");
                return true;
            }
            gui.openMenu(player, "main");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "menu":
                return handleMenu(sender, args);
            case "claim":
                return handleClaim(sender);
            case "unclaim":
                return handleUnclaim(sender);
            case "info":
                return handleInfo(sender);
            case "trust":
                return handleTrust(sender, args);
            case "untrust":
                return handleUntrust(sender, args);
            case "roles":
                return handleRoles(sender, args);
            case "flag":
                return handleFlags(sender, args);
            case "compass":
                return handleCompass(sender);
            case "reload":
                return handleReload(sender);
            default:
                // Fallback: show main menu (players) or basic help (console)
                if (sender instanceof Player p) {
                    gui.openMenu(p, "main");
                } else {
                    sender.sendMessage("ProShield: /" + label + " [menu|claim|unclaim|info|trust|untrust|roles|flag|compass|reload]");
                }
                return true;
        }
    }

    // -----------------------------------------------------
    // Handlers
    // -----------------------------------------------------
    private boolean handleMenu(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.player-only");
            return true;
        }
        String key = (args.length >= 2) ? args[1].toLowerCase(Locale.ROOT) : "main";
        gui.openMenu(player, key);
        return true;
    }

    private boolean handleClaim(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.player-only");
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();
        Plot existing = plots.getPlot(chunk);
        if (existing != null) {
            Map<String,String> ph = Map.of(
                    "owner", ownerName(existing.getOwner())
            );
            messages.send(player, "claim.already-owned", ph);
            return true;
        }

        Plot created = plots.createPlot(player.getUniqueId(), chunk);
        // Display name default is handled in Plot; no-op here
        messages.send(player, "claim.success");
        return true;
    }

    private boolean handleUnclaim(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.player-only");
            return true;
        }

        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "error.no-claim");
            return true;
        }

        UUID uid = player.getUniqueId();
        boolean owner = plot.isOwner(uid);
        boolean allowed = owner || roles.canUnclaim(uid, plot.getId());

        if (!allowed) {
            messages.send(player, "roles.not-allowed", Map.of("claim", plot.getDisplayNameSafe()));
            return true;
        }

        plots.removePlot(plot);
        messages.send(player, "claim.unclaimed");
        return true;
    }

    private boolean handleInfo(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.player-only");
            return true;
        }

        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "error.no-claim");
            return true;
        }

        String ownerName = ownerName(plot.getOwner());
        String trusted = String.join(", ", new ArrayList<>(plot.getTrustedNames()));

        Map<String,String> ph = new HashMap<>();
        ph.put("owner", ownerName);
        ph.put("trusted", trusted.isEmpty() ? "None" : trusted);

        messages.send(player, "claim.info", ph);
        return true;
    }

    private boolean handleTrust(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.player-only");
            return true;
        }

        if (args.length < 2) {
            messages.send(player, "error.player-not-found", Map.of("player", "unknown"));
            return true;
        }

        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "error.no-claim");
            return true;
        }

        // must be owner or manager-ish role with manage perms
        UUID actor = player.getUniqueId();
        boolean canManage = plot.isOwner(actor) || roles.canManageTrust(actor, plot.getId());
        if (!canManage) {
            messages.send(player, "roles.not-allowed", Map.of("claim", plot.getDisplayNameSafe()));
            return true;
        }

        String targetName = args[1];
        if (targetName.equalsIgnoreCase(player.getName())) {
            messages.send(player, "trust.cannot-self");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID targetId = target.getUniqueId();

        ClaimRole role = ClaimRole.TRUSTED;
        if (args.length >= 3) {
            try {
                role = ClaimRole.valueOf(args[2].toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                messages.send(player, "trust.invalid-role", Map.of("role", args[2]));
                return true;
            }
        }

        roles.assignRole(plot.getId(), targetId, role);

        Map<String,String> ph = new HashMap<>();
        ph.put("player", targetName);
        ph.put("claim", plot.getDisplayNameSafe());
        ph.put("role", role.getDisplayName());

        messages.send(player, "trust.added", ph);
        plots.saveAsync(plot);
        return true;
    }

    private boolean handleUntrust(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.player-only");
            return true;
        }

        if (args.length < 2) {
            messages.send(player, "error.player-not-found", Map.of("player", "unknown"));
            return true;
        }

        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "error.no-claim");
            return true;
        }

        UUID actor = player.getUniqueId();
        boolean canManage = plot.isOwner(actor) || roles.canManageTrust(actor, plot.getId());
        if (!canManage) {
            messages.send(player, "roles.not-allowed", Map.of("claim", plot.getDisplayNameSafe()));
            return true;
        }

        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID targetId = target.getUniqueId();

        roles.clearRole(plot.getId(), targetId);

        Map<String,String> ph = new HashMap<>();
        ph.put("player", targetName);
        ph.put("claim", plot.getDisplayNameSafe());
        messages.send(player, "untrust.removed", ph);

        plots.saveAsync(plot);
        return true;
    }

    private boolean handleRoles(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.player-only");
            return true;
        }

        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "roles.no-claim");
            return true;
        }

        if (args.length >= 2) {
            // remember target name for the GUI flow if you're still using it elsewhere
            gui.rememberTarget(player, args[1]);
        }
        gui.openMenu(player, "roles"); // this opens the roles GUI shell from config
        return true;
    }

    private boolean handleFlags(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.player-only");
            return true;
        }

        // For now open the flags menu; when we finalize PlotSettings toggles,
        // we’ll parse args and flip the setting here.
        gui.openMenu(player, "flags");
        return true;
    }

    private boolean handleCompass(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.player-only");
            return true;
        }
        boolean force = player.isOp();
        compass.giveCompass(player, force);
        messages.send(player, "compass.given"); // covered by your messages.yml
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("proshield.admin")) {
            messages.send(sender, "error.no-permission");
            return true;
        }

        plugin.reloadConfig();
        messages.reload();
        messages.send(sender, "admin.reload");
        return true;
    }

    // -----------------------------------------------------
    // Tab Completion
    // -----------------------------------------------------
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return prefixFilter(Arrays.asList(
                    "menu", "claim", "unclaim", "info",
                    "trust", "untrust", "roles", "flag",
                    "compass", "reload"
            ), args[0]);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "menu":
                if (args.length == 2) {
                    return prefixFilter(Arrays.asList("main", "trust", "untrust", "flags", "roles", "admin"), args[1]);
                }
                break;
            case "trust":
            case "untrust":
            case "roles":
                if (args.length == 2) {
                    return prefixFilter(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), args[1]);
                } else if (args.length == 3 && sub.equals("trust")) {
                    return prefixFilter(Arrays.stream(ClaimRole.values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.toList()), args[2]);
                }
                break;
            case "flag":
                if (args.length == 2) {
                    return prefixFilter(Arrays.asList("explosions","buckets","item-frames","armor-stands","containers","pets","pvp"), args[1]);
                } else if (args.length == 3) {
                    return prefixFilter(Arrays.asList("on","off","toggle"), args[2]);
                }
                break;
        }
        return Collections.emptyList();
    }

    private List<String> prefixFilter(List<String> base, String token) {
        String t = token.toLowerCase(Locale.ROOT);
        return base.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(t)).collect(Collectors.toList());
    }

    // -----------------------------------------------------
    // Helpers
    // -----------------------------------------------------
    private String ownerName(UUID owner) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(owner);
        return (p != null && p.getName() != null) ? p.getName() : "Unknown";
    }
}
