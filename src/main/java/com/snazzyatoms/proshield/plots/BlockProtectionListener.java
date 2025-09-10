package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.Action;
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
        if (claim == null) return; // No claim → allow

        if (plots.isBypassing(player)) return; // Admin bypass

        ClaimRole role = claim.getRole(player.getUniqueId());

        boolean allowed = role.canBuild(); // Builder+ roles allowed
        if (!allowed) {
            event.setCancelled(true);
            player.sendMessage(plugin.prefix() + ChatColor.RED + "You cannot break blocks in this claim!");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Claim claim = plots.getClaimAt(block.getLocation());
        if (claim == null) return; // No claim → allow

        if (plots.isBypassing(player)) return;

        ClaimRole role = claim.getRole(player.getUniqueId());
        boolean allowed = role.canBuild();

        if (!allowed) {
            event.setCancelled(true);
            player.sendMessage(plugin.prefix() + ChatColor.RED + "You cannot place blocks in this claim!");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;

        Claim claim = plots.getClaimAt(block.getLocation());
        if (claim == null) return;

        if (plots.isBypassing(player)) return;

        // Example: container interaction requires Container role
        Material type = block.getType();
        boolean isContainer = (type == Material.CHEST || type == Material.BARREL || type == Material.FURNACE);

        ClaimRole role = claim.getRole(player.getUniqueId());
        boolean allowed = isContainer ? role.canUseContainers() : role.canInteract();

        if (!allowed) {
            event.setCancelled(true);
            player.sendMessage(plugin.prefix() + ChatColor.RED + "You cannot interact with blocks here!");
        }
    }
}
