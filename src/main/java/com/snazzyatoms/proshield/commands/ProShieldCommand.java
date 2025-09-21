// src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ProShieldCommand implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final MessagesUtil messages;

    public ProShieldCommand(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Default: open GUI for player
        if (args.length == 0) {
            if (sender instanceof Player player) {
                if (player.hasPermission("proshield.player.access")) {
                    plugin.getGuiManager().openMain(player);
                } else {
                    messages.send(player, messages.getOrDefault("messages.error.no-permission", "&cNo permission."));
                }
            } else {
                messages.send(sender, messages.getOrDefault("messages.error.player-only", "&cPlayers only."));
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        // --- HELP ---
        if (sub.equals("help")) {
            if (sender instanceof Player player && player.hasPermission("proshield.player.access")) {
                messages.sendList(sender, messages.getList("help.player"));
            } else if (sender.hasPermission("proshield.admin")) {
                messages.sendList(sender, messages.getList("help.admin"));
            } else if (sender.hasPermission("proshield.admin.worldcontrols")
                    || sender.hasPermission("proshield.admin.expansions")) {
                messages.sendList(sender, messages.getList("help.senior"));
            } else {
                messages.send(sender, messages.getOrDefault("messages.error.no-permission", "&cNo permission."));
            }
            return true;
        }

        // --- COMPASS ---
        if (sub.equals("compass")) {
            if (!(sender instanceof Player player)) {
                messages.send(sender, messages.getOrDefault("messages.error.player-only", "&cPlayers only."));
                return true;
            }
            if (!player.hasPermission("proshield.player.access")) {
                messages.send(player, messages.getOrDefault("messages.error.no-permission", "&cNo permission."));
                return true;
            }

            // Check if player already has a compass
            boolean alreadyHas = false;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == Material.COMPASS) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.hasDisplayName()
                            && meta.getDisplayName().contains("ProShield Compass")) {
                        alreadyHas = true;
                        break;
                    }
                }
            }

            if (alreadyHas) {
                messages.send(player, messages.getOrDefault("messages.compass.already-have", "&cYou already have a ProShield compass."));
                return true;
            }

            // Check inventory space
            if (player.getInventory().firstEmpty() == -1) {
                messages.send(player, messages.getOrDefault("messages.error.inventory-full", "&cYour inventory is full."));
                return true;
            }

            // Give compass
            ItemStack compass = new ItemStack(Material.COMPASS);
            ItemMeta meta = compass.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(messages.color("&bProShield Compass"));
                compass.setItemMeta(meta);
            }
            player.getInventory().addItem(compass);
            messages.send(player, messages.getOrDefault("messages.compass.command-success", "&aYou received a ProShield compass."));
            return true;
        }

        // --- ADMIN COMMANDS ---
        if (!sender.hasPermission("proshield.admin")) {
            messages.send(sender, messages.getOrDefault("messages.error.no-permission", "&cNo permission."));
            return true;
        }

        switch (sub) {
            case "reload" -> {
                plugin.reloadConfig();
                plugin.loadMessagesConfig(); // reload messages.yml
                plugin.getExpansionRequestManager().reload(); // reload expansions.yml
                messages.send(sender, messages.getOrDefault("messages.reloaded", "&aProShield reloaded."));
            }

            case "debug" -> {
                plugin.toggleDebug();
                if (plugin.isDebugEnabled()) {
                    messages.send(sender, messages.getOrDefault("messages.admin.debug-on", "&aDebug mode enabled."));
                } else {
                    messages.send(sender, messages.getOrDefault("messages.admin.debug-off", "&cDebug mode disabled."));
                }
            }

            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, messages.getOrDefault("messages.error.player-only", "&cPlayers only."));
                    return true;
                }
                UUID uuid = player.getUniqueId();
                if (plugin.getBypassing().contains(uuid)) {
                    plugin.getBypassing().remove(uuid);
                    messages.send(player, messages.getOrDefault("messages.admin.bypass-off", "&cBypass disabled."));
                } else {
                    plugin.getBypassing().add(uuid);
                    messages.send(player, messages.getOrDefault("messages.admin.bypass-on", "&aBypass enabled."));
                }
            }

            case "admin" -> {
                if (sender instanceof Player player) {
                    plugin.getGuiManager().openAdminTools(player);
                } else {
                    messages.send(sender, messages.getOrDefault("messages.error.player-only", "&cPlayers only."));
                }
            }

            default -> {
                messages.send(sender, "&cUnknown subcommand.");
                messages.sendList(sender, messages.getList("help.admin")); // fallback to admin help
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("proshield.admin")) {
                return List.of("help", "compass", "admin", "reload", "debug", "bypass");
            } else if (sender.hasPermission("proshield.player.access")) {
                return List.of("help", "compass");
            }
        }
        return Collections.emptyList();
    }
}
