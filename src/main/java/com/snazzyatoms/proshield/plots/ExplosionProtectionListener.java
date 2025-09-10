package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;

/**
 * Handles explosion protection inside claims and globally.
 * - Prevents block damage from creepers, TNT, withers, etc.
 * - Fully configurable per explosion type
 */
public class ExplosionProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public ExplosionProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        EntityType type = event.getEntityType();
        Chunk chunk = event.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        FileConfiguration config = plugin.getConfig();

        // Global toggle
        if (!config.getBoolean("protection.explosions.enabled", true)) {
            return; // explosions allowed everywhere
        }

        // Per-type checks
        boolean cancel = switch (type) {
            case CREEPER -> !config.getBoolean("protection.explosions.creeper", true);
            case PRIMED_TNT, TNT_MINECART -> !config.getBoolean("protection.explosions.tnt", true);
            case WITHER -> !config.getBoolean("protection.explosions.wither", true);
            case WITHER_SKULL -> !config.getBoolean("protection.explosions.wither_skull", true);
            case ENDER_CRYSTAL -> !config.getBoolean("protection.explosions.end_crystal", true);
            case ENDER_DRAGON -> !config.getBoolean("protection.explosions.ender_dragon", true);
            default -> false;
        };

        if (cancel) {
            event.blockList().clear();
            return;
        }

        // Claim-specific rule: block all explosions unless explicitly allowed
        if (plot != null) {
            if (!plot.getSettings().isExplosionAllowed()) {
                event.blockList().clear();
                messages.broadcastToNearby(event.getLocation(),
                        "protection.explosion-blocked",
                        "%type%", type.name());
            }
        } else {
            // Wilderness check: global wilderness flag
            boolean wildernessAllowed = config.getBoolean("protection.wilderness.allow-explosions", true);
            if (!wildernessAllowed) {
                event.blockList().clear();
            }
        }

        // Cleanup any invalid or protected blocks
        Iterator<org.bukkit.block.Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            org.bukkit.block.Block block = it.next();
            Plot blockPlot = plotManager.getPlot(block.getChunk());
            if (blockPlot != null && !blockPlot.getSettings().isExplosionAllowed()) {
                it.remove();
            }
        }
    }
}
