package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    public BlockProtectionListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Claim claim = plots.getClaimAt(block.getLocation());
        if (claim == null) return;

        if (plots.isBypassing(player)) return;

        if (!claim.canBuild(player)) {
            event.setCancelled(true);
            player.sendMessage(plugin.prefix() + ChatColor.RED + "You cannot break blocks in this claim.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Claim claim = plots.getClaimAt(block.getLocation());
        if (claim == null) return;

        if (plots.isBypassing(player)) return;

        if (!claim.canBuild(player)) {
            event.setCancelled(true);
            player.sendMessage(plugin.prefix() + ChatColor.RED + "You cannot place blocks in this claim.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        Claim claim = plots.getClaimAt(block.getLocation());
        if (claim == null) return;

        if (plots.isBypassing(player)) return;

        // Check interactions (doors, trapdoors, levers, chests, etc.)
        Material type = block.getType();
        if (!claim.canInteract(player, type)) {
            event.setCancelled(true);
            player.sendMessage(plugin.prefix() + ChatColor.RED + "You cannot interact with that here.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Claim claim = plots.getClaimAt(event.getBlock().getLocation());
        if (claim == null) return;

        boolean globalFire = plugin.getConfig().getBoolean("protection.fire.ignite.spread", true);
        Boolean claimFire = claim.getFlag("fire-ignite");

        if (!(claimFire != null ? claimFire : globalFire)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent event) {
        Claim claim = plots.getClaimAt(event.getLocation());
        if (claim == null) return;

        boolean globalExplosions = plugin.getConfig().getBoolean("protection.explosions.enabled", true);
        Boolean claimExplosions = claim.getFlag("explosions");

        if (!(claimExplosions != null ? claimExplosions : globalExplosions)) {
            event.blockList().clear();
        }
    }
}
