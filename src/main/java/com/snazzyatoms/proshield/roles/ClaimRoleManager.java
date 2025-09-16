package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.UUID;

public class ClaimRoleManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.messages = plugin.getMessagesUtil();
    }

    public void loadAll() {
        // load roles from disk if you persist them separately
    }

    public void saveAll() {
        // persist roles if applicable
    }

    /**
     * Assign a role to a nearby target (invoked from chat after GUI click).
     *
     * @param actor      owner/co-owner who is assigning
     * @param targetUuid player to assign a role to
     * @param rawRole    role name typed in chat
     */
    public void assignRoleViaChat(Player actor, UUID targetUuid, String rawRole) {
        if (actor == null || targetUuid == null || rawRole == null) return;

        Plot plot = plotManager.getPlotAt(actor.getLocation());
        if (plot == null) {
            messages.send(actor, "&cYou must stand inside your claim to manage roles.");
            return;
        }

        // Permission check: only owner or admin can assign
        if (!plot.getOwner().equals(actor.getUniqueId()) && !actor.hasPermission("proshield.admin")) {
            messages.send(actor, "&cOnly the owner (or admin) can assign roles in this claim.");
            return;
        }

        // Normalize role
        String role = rawRole.trim().toLowerCase(Locale.ROOT);
        if (!(role.equals("member") || role.equals("builder") || role.equals("trusted") || role.equals("co-owner"))) {
            role = "trusted"; // fallback
        }

        // ✅ Update trusted map
        plot.getTrusted().put(targetUuid, role);

        OfflinePlayer target = plugin.getServer().getOfflinePlayer(targetUuid);
        String targetName = (target != null && target.getName() != null)
                ? target.getName()
                : targetUuid.toString();

        messages.send(actor, "&aAssigned &f" + targetName + " &ato role &f" + role + " &ain this claim.");
        plotManager.saveAll(); // persist update
    }
}
