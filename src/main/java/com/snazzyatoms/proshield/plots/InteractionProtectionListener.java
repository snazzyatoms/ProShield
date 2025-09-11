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
 * - Uses PlotSettings for per-claim interaction rules
 * - Uses ClaimRoleManager for trusted role enforcement
 * - Falls back to wilderness config
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
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getClickedBlock() == null) return;

        Chunk chunk = event.getClickedBlock().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            // Wilderness rules
            if (!plugin.getConfig().getBoolean("protection.interactions.wilderness", true)) {
                event.setCancelled(true);
                messages.send(player, "interaction-deny");
            }
            return;
        }

        ClaimRole role = roleManager.getRole(plot, player.getUniqueId());

        if (role == null || !roleManager.canInteract(role)) {
            event.setCancelled(true);
            messages.send(player, "interaction-deny");
            messages.debug("&cPrevented interaction in claim: " + plot.getDisplayNameSafe() +
                    " by " + player.getName());
        }
    }
}
