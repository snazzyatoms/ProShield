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
 * Handles block breaking & placing inside claims with role-based checks.
 * - Uses MessagesUtil for feedback
 * - Global + per-claim enforcement
 */
public class BlockProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public BlockProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
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
        if (plot == null) return; // wilderness, allow

        ClaimRole role = roleManager.getRole(plot, player);

        if (!roleManager.canBuild(role)) {
            event.setCancelled(true);
            messages.send(player, "protection.block.break-denied",
                    messages.buildPlaceholders("player", player.getName(), "claim", plot.getName()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return; // wilderness, allow

        ClaimRole role = roleManager.getRole(plot, player);

        if (!roleManager.canBuild(role)) {
            event.setCancelled(true);
            messages.send(player, "protection.block.place-denied",
                    messages.buildPlaceholders("player", player.getName(), "claim", plot.getName()));
        }
    }
}
