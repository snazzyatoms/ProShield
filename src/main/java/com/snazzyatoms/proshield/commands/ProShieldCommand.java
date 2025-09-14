package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionQueue;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class ProShieldCommand implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public ProShieldCommand(ProShield plugin, GUIManager guiManager, PlotManager plotManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.plotManager = plotManager;
        this.messages = messages;
    }

    private void giveCompass(Player player) {
        ItemStack compass = new ItemStack(org.bukkit.Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Right-click to open",
                    ChatColor.GRAY + "the ProShield menu"
            ));
            compass.setItemMeta(meta);
        }
        player.getInventory().addItem(compass);
        messages.send(player, "&aGiven ProShield Compass.");
    }

    private void toggleFlag(Player player, String flagKey) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&cYou are not standing in a claim.");
            return;
        }
        if (!plot.isOwner(player.getUniqueId())) {
            messages.send(player, "&cOnly the claim owner can change flags.");
            return;
        }

        boolean current = plot.getFlag(flagKey,
                plugin.getConfig().getBoolean("claims.default-flags." + flagKey, false));
        boolean next = !current;
        plot.setFlag(flagKey, next);

        String sound = plugin.getConfig().getString("sounds.flag-toggle", "BLOCK_NOTE_BLOCK_PLING");
        try {
            player.playSound(player.getLocation(), sound, 1f, 1f);
        } catch (Exception ignored) {}

        messages.send(player, "&eFlag &6" + flagKey + " &eis now " + (next ? "&aENABLED" : "&cDISABLED") + "&e.");
    }

    private UUID resolvePlayerId(String input) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException ex) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(input);
            return offline.getUniqueId();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return true;
        }

        if (args.length == 0) {
            guiManager.openMenu(player, "main");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "help" -> {
                for (String line : plugin.getConfig().getStringList("help.player")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
            case "reload" -> {
                if (player.isOp() || player.hasPermission("proshield.admin.reload")) {
                    plugin.reloadConfig();
                    messages.send(player, "&aProShield config reloaded.");
                } else messages.send(player, "&cNo permission.");
            }
            case "debug" -> {
                if (player.isOp()) {
                    plugin.toggleDebug();
                    messages.send(player, "&eDebug mode: " +
                            (plugin.isDebugEnabled() ? "&aON" : "&cOFF"));
                } else messages.send(player, "&cNo permission.");
            }
            case "bypass" -> {
                if (!player.hasPermission("proshield.bypass")) {
                    messages.send(player, "&cNo permission.");
                    return true;
                }
                if (plugin.getBypassing().contains(player.getUniqueId())) {
                    plugin.getBypassing().remove(player.getUniqueId());
                    messages.send(player, "&cBypass disabled.");
                } else {
                    plugin.getBypassing().add(player.getUniqueId());
                    messages.send(player, "&aBypass enabled.");
                }
            }
            case "compass" -> {
                if (!player.hasPermission("proshield.compass")) {
                    messages.send(player, "&cNo permission.");
                    return true;
                }
                giveCompass(player);
            }
            case "admin" -> guiManager.openMenu(player, "main");
            case "flag" -> {
                if (args.length < 2) {
                    messages.send(player, "&cUsage: /proshield flag <key>");
                    return true;
                }
                toggleFlag(player, args[1].toLowerCase(Locale.ROOT));
            }
            case "approve" -> {
                if (!player.hasPermission("proshield.admin")) {
                    messages.send(player, "&cNo permission.");
                    return true;
                }
                if (args.length < 2) {
                    messages.send(player, "&cUsage: /proshield approve <playerName|UUID>");
                    return true;
                }
                UUID targetId = resolvePlayerId(args[1]);
                List<ExpansionRequest> reqs = ExpansionQueue.getRequests(targetId);
                if (reqs.isEmpty()) {
                    messages.send(player, "&cNo pending requests for that player.");
                    return true;
                }
                ExpansionRequest req = reqs.get(0);
                ExpansionQueue.approveRequest(req);
                Player target = Bukkit.getPlayer(targetId);
                if (target != null) {
                    target.sendMessage(ChatColor.GREEN + "Your expansion request was approved!");
                }
                messages.send(player, "&aApproved expansion request for &e" + args[1]);
            }
            case "deny" -> {
                if (!player.hasPermission("proshield.admin")) {
                    messages.send(player, "&cNo permission.");
                    return true;
                }
                if (args.length < 3) {
                    messages.send(player, "&cUsage: /proshield deny <playerName|UUID> <reason>");
                    return true;
                }
                UUID targetId = resolvePlayerId(args[1]);
                String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                List<ExpansionRequest> reqs = ExpansionQueue.getRequests(targetId);
                if (reqs.isEmpty()) {
                    messages.send(player, "&cNo pending requests for that player.");
                    return true;
                }
                ExpansionRequest req = reqs.get(0);
                ExpansionQueue.denyRequest(req, reason);
                Player target = Bukkit.getPlayer(targetId);
                if (target != null) {
                    target.sendMessage(ChatColor.RED + "Your expansion request was denied: " + reason);
                }
                messages.send(player, "&cDenied expansion request for &e" + args[1] +
                        " &7(reason: " + reason + ")");
            }
            default -> messages.send(player, "&cUnknown subcommand.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        if (args.length == 1) {
            return Arrays.asList("help", "reload", "debug", "bypass", "compass",
                    "admin", "flag", "approve", "deny").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        if ((args[0].equalsIgnoreCase("approve") || args[0].equalsIgnoreCase("deny")) && args.length == 2) {
            // Suggest pending request player names
            Set<UUID> pendingPlayers = ExpansionQueue.getPendingRequests().stream()
                    .map(ExpansionRequest::getPlayerId)
                    .collect(Collectors.toSet());

            return pendingPlayers.stream()
                    .map(id -> {
                        Player online = Bukkit.getPlayer(id);
                        return (online != null) ? online.getName() : id.toString();
                    })
                    .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
