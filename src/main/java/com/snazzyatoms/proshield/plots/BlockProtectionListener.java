package com.snazzyatoms.proshield.plots;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockProtectionListener implements Listener {

    private final PlotManager plots;

    public BlockProtectionListener(PlotManager plots) {
        this.plots = plots;
    }

    private boolean blocked(Block b) {
        return plots.getClaim(b.getLocation()).isPresent();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (!blocked(e.getBlock())) return;
        Player p = e.getPlayer();
        if (!plots.isTrustedOrOwner(p.getUniqueId(), e.getBlock().getLocation())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (!blocked(e.getBlock())) return;
        Player p = e.getPlayer();
        if (!plots.isTrustedOrOwner(p.getUniqueId(), e.getBlock().getLocation())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        if (plots.getClaim(e.getLocation()).isEmpty()) return;
        if (!plots.getPlugin().getConfig().getBoolean("protection.explosions.enabled", true)) return;
        e.blockList().clear();
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireSpread(BlockSpreadEvent e) {
        if (e.getSource() == null) return;
        if (plots.getClaim(e.getBlock().getLocation()).isEmpty()) return;
        if (!plots.getPlugin().getConfig().getBoolean("protection.fire.enabled", true)) return;
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (!(e.getEntity() instanceof Enderman)) return;
        if (plots.getClaim(e.getBlock().getLocation()).isEmpty()) return;
        if (!plots.getPlugin().getConfig().getBoolean("protection.entity-grief.enabled", true)) return;
        e.setCancelled(true);
    }
}
