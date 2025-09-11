package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * /flags command
 *
 * ✅ Opens the claim flags GUI
 * ✅ Players need "proshield.flags"
 * ✅ Admins with "proshield.admin.flags" can use anywhere
 * ✅ Adds cooldown (default: 2 seconds) to prevent spam
 */
public class FlagsCommand implements CommandExecutor {

    private final GUIManager gui;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 2000; // 2 seconds

    public FlagsCommand(GUIManager gui) {
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("proshield.flags")) {
            player.sendMessage("§cYou don’t have permission to use this command.");
            return true;
        }

        // Cooldown check
        long now = System.currentTimeMillis();
        long last = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < COOLDOWN_MS && !player.hasPermission("proshield.admin.flags")) {
            long remaining = (COOLDOWN_MS - (now - last)) / 1000 + 1;
            player.sendMessage("§cPlease wait " + remaining + "s before using /flags again.");
            return true;
        }

        cooldowns.put(player.getUniqueId(), now);
        gui.openFlagsMenu(player);
        return true;
    }
}
