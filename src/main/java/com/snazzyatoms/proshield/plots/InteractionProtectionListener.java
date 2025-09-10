package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Handles player interactions with blocks/entities inside claims.
 * - Role-based interaction checks
 * - Config-driven categories
 * - Uses MessagesUtil for feedback
 */
public class InteractionProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public InteractionProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;

        Chunk chunk = block.getChunk();
        Plot plot = plotManager.getPlot(chunk);

        // Wilderness → follow global config (allow/deny)
        if (plot == null) {
            boolean wildernessAllowed = plugin.getConfig().getBoolean("protection.wilderness.allow-interact", true);
            if (!wildernessAllowed) {
                event.setCancelled(true);
                messages.send(player, "protection.interact.wilderness-denied",
                        messages.buildPlaceholders("player", player.getName(), "block", block.getType().name()));
            }
            return;
        }

        // Inside claim → role-based check
        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canInteract(role)) {
            event.setCancelled(true);
            messages.send(player, "protection.interact.claim-denied",
                    messages.buildPlaceholders("player", player.getName(), "claim", plot.getName(), "block", block.getType().name()));
        }
    }
}
