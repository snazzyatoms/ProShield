// src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final GUIManager guiManager;
    private final CompassManager compassManager;
    private final MessagesUtil messages;

    public ProShieldCommand(ProShield plugin,
                            PlotManager plotManager,
                            GUIManager guiManager,
                            CompassManager compassManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.guiManager = guiManager;
        this.compassManager = compassManager;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "⛔ Only players may use this command.");
            return true;
        }

        if (args.length == 0) {
            // Open main GUI
            guiManager.openMain(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> {
                sender.sendMessage(ChatColor.GOLD + "------ ProShield Help ------");
                sender.sendMessage(ChatColor.YELLOW + "/proshield" + ChatColor.GRAY + " - Open the main menu");
                sender.sendMessage(ChatColor.YELLOW + "/claim" + ChatColor.GRAY + " - Claim your current chunk");
                sender.sendMessage(ChatColor.YELLOW + "/unclaim" + ChatColor.GRAY + " - Unclaim your current chunk");
                sender.sendMessage(ChatColor.YELLOW + "/trust <player> [role]" + ChatColor.GRAY + " - Trust a player");
                sender.sendMessage(ChatColor.YELLOW + "/untrust <player>" + ChatColor.GRAY + " - Untrust a player");
                sender.sendMessage(ChatColor.YELLOW + "/roles" + ChatColor.GRAY + " - Manage claim roles");
                sender.sendMessage(ChatColor.YELLOW + "/flags" + ChatColor.GRAY + " - Manage claim flags");
                return true;
            }
            case "debug" -> {
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, "error.no-permission");
                    return true;
                }
                boolean newState = toggleDebug();
                sender.sendMessage(ChatColor.AQUA + "Debug mode is now " +
                        (newState ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));
                return true;
            }
            case "compass" -> {
                if (!sender.hasPermission("proshield.use")) {
                    messages.send(sender, "error.no-permission");
                    return true;
                }
                compassManager.giveCompass(player);
                sender.sendMessage(ChatColor.GREEN + "🧭 You received a ProShield Compass!");
                return true;
            }
            default -> {
                sender.sendMessage(ChatColor.RED + "⛔ Unknown subcommand. Try /proshield help");
                return true;
            }
        }
    }

    /* -------------------------------------------------------
     * Helpers
     * ------------------------------------------------------- */

    private boolean toggleDebug() {
        boolean newState = !plugin.getConfig().getBoolean("debug", false);
        plugin.getConfig().set("debug", newState);
        plugin.saveConfig();
        return newState;
    }

    public GUICache getGuiCache() {
        return plugin.getGuiCache();
    }
}
