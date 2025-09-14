// src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ProShieldCommand implements CommandExecutor {

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
        ItemStack compass = new ItemStack(Material.COMPASS);
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

        boolean current = plot.getFlag(flagKey, plugin.getConfig().getBoolean("claims.default-flags." + flagKey, false));
        boolean next = !current;
        plot.setFlag(flagKey, next);

        // optional sound feedback
        String sound = plugin.getConfig().getString("sounds.flag-toggle", "BLOCK_NOTE_BLOCK_PLING");
        try { player.playSound(player.getLocation(), sound, 1f, 1f); } catch (Exception ignored) {}

        messages.send(player, "&eFlag &6" + flagKey + " &eis now " + (next ? "&aENABLED" : "&cDISABLED") + "&e.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return true;
        }

        String command = label.toLowerCase();

        // --- Shortcut commands ---
        switch (command) {
            case "claim" -> {
                player.performCommand("proshield claim");
                return true;
            }
            case "unclaim" -> {
                player.performCommand("proshield unclaim");
                return true;
            }
            case "trust" -> {
                if (args.length < 1) {
                    messages.send(player, "&cUsage: /trust <player> [role]");
                    return true;
                }
                player.performCommand("proshield trust " + String.join(" ", args));
                return true;
            }
            case "untrust" -> {
                if (args.length < 1) {
                    messages.send(player, "&cUsage: /untrust <player>");
                    return true;
                }
                player.performCommand("proshield untrust " + args[0]);
                return true;
            }
            case "roles" -> {
                guiManager.openMenu(player, "roles");
                return true;
            }
            case "transfer" -> {
                if (args.length < 1) {
                    messages.send(player, "&cUsage: /transfer <player>");
                    return true;
                }
                player.performCommand("proshield transfer " + args[0]);
                return true;
            }
        }

        // --- Root /proshield handler ---
        if (command.equals("proshield") || command.equals("ps") || command.equals("shield")) {
            if (args.length == 0) {
                guiManager.openMenu(player, "main");
                return true;
            }

            String sub = args[0].toLowerCase();
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
                    } else {
                        messages.send(player, "&cNo permission.");
                    }
                }
                case "debug" -> {
                    if (player.isOp()) {
                        plugin.toggleDebug();
                        messages.send(player, "&eDebug mode: " + (plugin.isDebugEnabled() ? "&aON" : "&cOFF"));
                    } else {
                        messages.send(player, "&cNo permission.");
                    }
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
                case "compass" -> giveCompass(player);
                case "admin" -> guiManager.openMenu(player, "main");
                case "flag" -> {
                    if (args.length < 2) {
                        messages.send(player, "&cUsage: /proshield flag <key>");
                        return true;
                    }
                    toggleFlag(player, args[1].toLowerCase());
                }
                default -> messages.send(player, "&cUnknown subcommand.");
            }
            return true;
        }

        return false;
    }
}
