package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockProtectionListener implements Listener {
    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    public BlockProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return;

        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canBuild(role)) {
            event.setCancelled(true);
            plugin.getMessagesUtil().send(player, "build.break-deny", plot.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return;

        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canBuild(role)) {
            event.setCancelled(true);
            plugin.getMessagesUtil().send(player, "build.place-deny", plot.getName());
        }
    }
}
