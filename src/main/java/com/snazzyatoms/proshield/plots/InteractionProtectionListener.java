// src/main/java/com/snazzyatoms/proshield/plots/InteractionProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Handles protections for player interactions inside claims.
 *
 * Preserves all prior logic:
 * ✅ Checks claims via PlotManager
 * ✅ Uses ClaimRoleManager for permission checks
 * ✅ Cancels interaction if player lacks role rights
 */
public class InteractionProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public InteractionProtectionListener(ProShield plugin,
                                         PlotManager plotManager,
                                         ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getClickedBlock() == null) return;

        Chunk chunk = event.getClickedBlock().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) return; // wilderness

        ClaimRole role = roleManager.getRole(plot, player.getUniqueId());

        if (!roleManager.canInteract(role)) {
            event.setCancelled(true);
            messages.send(player, "interaction-deny");
            messages.debug("&c" + player.getName() +
                    " tried to interact inside claim: " + plot.getDisplayNameSafe());
        }
    }
}
