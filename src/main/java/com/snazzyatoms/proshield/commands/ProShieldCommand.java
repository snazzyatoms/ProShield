package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProShieldCommand implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final PlotManager plots;
    private final GUIManager gui;

    // Chat-await states for trust/untrust/transfer input
    private final Map<UUID, String> chatAwaitState = new ConcurrentHashMap<>();

    public ProShieldCommand(ProShield plugin, PlotManager plots, GUIManager gui) {
        this.plugin = plugin;
        this.plots = plots;
        this.gui = gui;
    }

    /* -----------------------------------------------------
     * Core Command Handler
     * --------------------------------------------------- */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (args.length == 0) {
            gui.openMain(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "claim" -> claim(player);
            case "unclaim" -> unclaim(player);
            case "info" -> info(player);
            case "trust" -> {
                if (args.length >= 2) {
                    trust(player, args[1]);
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /proshield trust <player>");
                }
            }
            case "untrust" -> {
                if (args.length >= 2) {
                    untrust(player, args[1]);
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /proshield untrust <player>");
                }
            }
            case "trusted" -> listTrusted(player);
            case "transfer" -> {
                if (args.length >= 2) {
                    transfer(player, args[1]);
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /proshield transfer <player>");
                }
            }
            case "preview" -> preview(player);
            case "compass" -> gui.giveCompass(player, player.isOp());
            case "bypass" -> toggleBypass(player, args);
            case "reload" -> plugin.reloadAllConfigs();
            case "purgeexpired" -> purgeExpired(player);
            case "debug" -> toggleDebug(player);
            default -> gui.openMain(player);
        }
        return true;
    }

    /* -----------------------------------------------------
     * Player Command Logic
     * --------------------------------------------------- */
    public void claim(Player player) {
        plots.claim(player);
    }

    public void unclaim(Player player) {
        plots.unclaim(player);
    }

    public void info(Player player) {
        plots.showInfo(player);
    }

    public void trust(Player player, String target) {
        plots.trust(player, target);
    }

    public void untrust(Player player, String target) {
        plots.untrust(player, target);
    }

    public void listTrusted(Player player) {
        plots.listTrusted(player);
    }

    public void transfer(Player player, String target) {
        plots.transfer(player, target);
    }

    public void preview(Player player) {
        plots.preview(player);
    }

    /* -----------------------------------------------------
     * GUI → Command Handler Hooks
     * --------------------------------------------------- */
    public void awaitTrust(Player player) {
        chatAwaitState.put(player.getUniqueId(), "trust");
    }

    public void awaitUntrust(Player player) {
        chatAwaitState.put(player.getUniqueId(), "untrust");
    }

    public void awaitTransfer(Player player) {
        chatAwaitState.put(player.getUniqueId(), "transfer");
    }

    public void setRole(Player player, String role) {
        plots.assignRole(player, role);
    }

    public void toggleFlag(Player player, String flag) {
        plots.toggleFlag(player, flag);
    }

    /* -----------------------------------------------------
     * Admin Tools
     * --------------------------------------------------- */
    public void purgeExpired(Player player) {
        if (!player.hasPermission("proshield.admin.expired.purge")) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return;
        }
        plots.purgeExpired(player);
    }

    public void toggleBypass(Player player, String[] args) {
        boolean state = plugin.toggleBypass(player);
        player.sendMessage(ChatColor.GREEN + "Bypass: " + state);
    }

    public void toggleDebug(Player player) {
        plugin.toggleDebug(player);
    }

    /* -----------------------------------------------------
     * Tab Completion
     * --------------------------------------------------- */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("claim", "unclaim", "info", "trust", "untrust", "trusted", "transfer", "preview", "compass", "bypass", "reload", "purgeexpired", "debug");
        }
        return Collections.emptyList();
    }

    /* -----------------------------------------------------
     * Chat Input Handling (Trust/Untrust/Transfer)
     * --------------------------------------------------- */
    public boolean handleChatInput(Player player, String message) {
        String state = chatAwaitState.remove(player.getUniqueId());
        if (state == null) return false;

        switch (state) {
            case "trust" -> trust(player, message);
            case "untrust" -> untrust(player, message);
            case "transfer" -> transfer(player, message);
        }
        return true;
    }
}
