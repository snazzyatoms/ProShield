package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Handles block breaking & placing with:
 * - Wilderness allow/deny toggles (config)
 * - Per-claim role checks (preserved logic)
 * - Admin bypass
 * - Messages via MessagesUtil
 */
public class BlockProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final MessagesUtil msg;

    public BlockProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plots = plotManager;
        this.roles = roleManager;
        this.msg = plugin.getMessagesUtil();
    }

    /* ---------------------------------------------------------
     * Block BREAK
     * --------------------------------------------------------- */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        if (plugin.isBypassing(p)) return;

        Chunk chunk = event.getBlock().getChunk();
        Plot plot = plots.getPlot(chunk);

        // Wilderness rules
        if (plot == null) {
            FileConfiguration cfg = plugin.getConfig();
            boolean allow = cfg.getBoolean("protection.wilderness.allow-block-break", true);
            if (!allow) {
                event.setCancelled(true);
                msg.send(p, "wilderness.break-deny");
            }
            return;
        }

        // Inside a claim → role-based check (preserved behavior)
        ClaimRole role = roles.getRole(plot, p);
        if (!roles.canBuild(role)) {
            event.setCancelled(true);
            msg.send(p, "build.break-deny", plot.getDisplayNameSafe(), p.getName());
        }
    }

    /* ---------------------------------------------------------
     * Block PLACE
     * --------------------------------------------------------- */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        if (plugin.isBypassing(p)) return;

        Chunk chunk = event.getBlock().getChunk();
        Plot plot = plots.getPlot(chunk);

        // Wilderness rules
        if (plot == null) {
            FileConfiguration cfg = plugin.getConfig();
            boolean allow = cfg.getBoolean("protection.wilderness.allow-block-place", true);
            if (!allow) {
                event.setCancelled(true);
                msg.send(p, "wilderness.place-deny");
            }
            return;
        }

        // Inside a claim → role-based check (preserved behavior)
        ClaimRole role = roles.getRole(plot, p);
        if (!roles.canBuild(role)) {
            event.setCancelled(true);
            msg.send(p, "build.place-deny", plot.getDisplayNameSafe(), p.getName());
        }
    }
}
