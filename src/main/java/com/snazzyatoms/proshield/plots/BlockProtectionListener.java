// path: src/main/java/com/snazzyatoms/proshield/plots/BlockProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.UUID;

public class BlockProtectionListener implements Listener {

    private final ProShield plugin;

    public BlockProtectionListener(ProShield plugin) {
        this.plugin = plugin;
    }

    private boolean canEdit(UUID playerId, Claim claim) {
        if (claim == null) return true;
        if (playerId.equals(claim.getOwner())) return true;
        return plugin.getServer().getPlayer(playerId) != null &&
               (plugin.getServer().getPlayer(playerId).isOp() ||
                plugin.getServer().getPlayer(playerId).hasPermission("proshield.bypass"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Claim cl = plugin.getPlotManager().getClaimAt(e.getBlock().getLocation());
        if (cl == null) return;
        if (!canEdit(e.getPlayer().getUniqueId(), cl)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "This chunk is claimed. You cannot break blocks here.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Claim cl = plugin.getPlotManager().getClaimAt(e.getBlock().getLocation());
        if (cl == null) return;
        if (!canEdit(e.getPlayer().getUniqueId(), cl)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "This chunk is claimed. You cannot place blocks here.");
        }
    }
}
