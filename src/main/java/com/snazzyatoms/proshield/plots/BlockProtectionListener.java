package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.roles.RolePermissions;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.UUID;

/**
 * Handles block placement and breaking protections inside claims.
 * - Uses PlotSettings for per-claim rules
 * - Uses ClaimRoleManager for trust/permissions
 * - Falls back to global wilderness settings from config
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
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();

        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            // Wilderness rules
            if (!plugin.getConfig().getBoolean("protection.blocks.break", true)) {
                event.setCancelled(true);
                messages.send(player, "block-break-deny");
            }
            return;
        }

        UUID uid = player.getUniqueId();
        ClaimRole role = roleManager.getRole(plot, uid);
        RolePermissions perms = roleManager.getRolePermissions(plot.getId(), role);

        if (!perms.canBuild()) {
            event.setCancelled(true);
            messages.send(player, "block-break-deny");
            messages.debug("&cPrevented block break in claim: " + plot.getDisplayNameSafe() +
                    " by " + player.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();

        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            // Wilderness rules
            if (!plugin.getConfig().getBoolean("protection.blocks.place", true)) {
                event.setCancelled(true);
                messages.send(player, "block-place-deny");
            }
            return;
        }

        UUID uid = player.getUniqueId();
        ClaimRole role = roleManager.getRole(plot, uid);
        RolePermissions perms = roleManager.getRolePermissions(plot.getId(), role);

        if (!perms.canBuild()) {
            event.setCancelled(true);
            messages.send(player, "block-place-deny");
            messages.debug("&cPrevented block place in claim: " + plot.getDisplayNameSafe() +
                    " by " + player.getName());
        }
    }
}
