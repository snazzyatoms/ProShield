// src/main/java/com/snazzyatoms/proshield/plots/ClaimExpansionHandler.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Handles claim expansion logic (/proshield expand).
 *
 * Preserves prior logic and fixes:
 * ✅ Uses createClaim(UUID, Location) properly
 * ✅ Corrects return type mismatch (Plot != boolean)
 */
public class ClaimExpansionHandler {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public ClaimExpansionHandler(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    public void expandClaim(Player player, int radius) {
        if (radius <= 0) {
            messages.send(player, "expand-invalid-radius");
            return;
        }

        Location center = player.getLocation();
        Chunk baseChunk = center.getChunk();
        Plot basePlot = plotManager.getPlot(baseChunk);

        if (basePlot == null || !basePlot.isOwner(player.getUniqueId())) {
            messages.send(player, "expand-not-owner");
            return;
        }

        int expanded = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Chunk chunk = baseChunk.getWorld().getChunkAt(baseChunk.getX() + dx, baseChunk.getZ() + dz);

                if (plotManager.getPlot(chunk) != null) continue; // already claimed

                Plot newPlot = plotManager.createClaim(player.getUniqueId(), chunk.getBlock(0, 64, 0).getLocation());
                if (newPlot != null) {
                    expanded++;
                }
            }
        }

        if (expanded > 0) {
            messages.send(player, "expand-success", String.valueOf(expanded));
        } else {
            messages.send(player, "expand-none");
        }
    }
}
