// path: src/main/java/com/snazzyatoms/proshield/plots/PlayerJoinListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

        p.getInventory().addItem(GUIManager.createAdminCompass());
        p.sendMessage(prefix() + ChatColor.GREEN + "ProShield compass added to your inventory.");
    }

    private boolean hasProShieldCompass(Player p) {
        for (ItemStack it : p.getInventory().getContents()) {
            if (it == null) continue;
            if (it.getType() != Material.COMPASS) continue;
            if (it.hasItemMeta() && it.getItemMeta().hasDisplayName()) {
                String name = ChatColor.stripColor(it.getItemMeta().getDisplayName());
                if ("ProShield Compass".equalsIgnoreCase(name)) return true;
            }
        }
        return false;
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }
}
