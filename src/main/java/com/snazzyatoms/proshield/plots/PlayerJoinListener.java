package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerJoinListener implements Listener {

    private final ProShield plugin;

    public PlayerJoinListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        boolean autoGive = plugin.getConfig().getBoolean("autogive.compass-on-join", true);
        if (!autoGive) return;

        boolean eligible = p.isOp() || p.hasPermission("proshield.compass") || p.hasPermission("proshield.admin");
        if (!eligible) return;

        if (hasProShieldCompass(p)) return;

        ItemStack compass = GUIManager.createAdminCompass();
        // Try to add; if full, optionally drop
        var leftovers = p.getInventory().addItem(compass);
        if (!leftovers.isEmpty()) {
            boolean dropIfFull = plugin.getConfig().getBoolean("compass.drop-if-full", true);
            if (dropIfFull) {
                p.getWorld().dropItemNaturally(p.getLocation(), compass);
                p.sendMessage(prefix() + ChatColor.YELLOW + "Inventory full — dropped a ProShield compass at your feet.");
            } else {
                p.sendMessage(prefix() + ChatColor.RED + "Inventory full — could not give ProShield compass. Free a slot or use /proshield compass.");
            }
        } else {
            p.sendMessage(prefix() + ChatColor.GREEN + "ProShield compass added to your inventory.");
        }
    }

    private boolean hasProShieldCompass(Player p) {
        return java.util.Arrays.stream(p.getInventory().getContents())
                .filter(it -> it != null && it.hasItemMeta() && it.getItemMeta().hasDisplayName())
                .anyMatch(it -> ChatColor.stripColor(it.getItemMeta().getDisplayName())
                        .equalsIgnoreCase("ProShield Compass"));
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }
}
