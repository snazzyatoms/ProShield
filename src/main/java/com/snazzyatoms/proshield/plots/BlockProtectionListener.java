// src/main/java/com/snazzyatoms/proshield/plots/BlockProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Handles block break/place protections inside claims.
 *
 * Preserves prior logic:
 * ✅ Uses PlotManager to check claims
 * ✅ Uses ClaimRoleManager for role permissions
 * ✅ Cancels events when player lacks build rights
 */
public class BlockProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public BlockProtectionListener(ProShield plugin,
                                   PlotManager plotManager,
                                   ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) return; // wilderness

        ClaimRole role = roleManager.getRole(plot, player.getUniqueId());

        if (!roleManager.canBuild(role)) {
            event.setCancelled(true);
            messages.send(player, "block-break-deny");
            messages.debug("&c" + player.getName() +
                    " tried to break a block in claim: " + plot.getDisplayNameSafe());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) return; // wilderness

        ClaimRole role = roleManager.getRole(plot, player.getUniqueId());

        if (!roleManager.canBuild(role)) {
            event.setCancelled(true);
            messages.send(player, "block-place-deny");
            messages.debug("&c" + player.getName() +
                    " tried to place a block in claim: " + plot.getDisplayNameSafe());
        }
    }
}
