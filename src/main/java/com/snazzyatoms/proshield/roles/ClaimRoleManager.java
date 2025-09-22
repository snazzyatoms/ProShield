package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages trusted player roles per plot.
 */
public class ClaimRoleManager {

    // Map<PlotID, Map<PlayerUUID, ClaimRole>>
    private final Map<UUID, Map<UUID, ClaimRole>> roles = new HashMap<>();

    /** Get a player's role for a plot. Defaults to NONE if not set. */
    public ClaimRole getRole(Plot plot, UUID playerId) {
        if (plot == null || playerId == null) return ClaimRole.NONE;
        Map<UUID, ClaimRole> map = roles.get(plot.getId());
        return (map != null) ? map.getOrDefault(playerId, ClaimRole.NONE) : ClaimRole.NONE;
    }

    /** Assign a role to a player in a plot. */
    public void setRole(Plot plot, UUID playerId, ClaimRole role) {
        if (plot == null || playerId == null || role == null) return;
        roles.computeIfAbsent(plot.getId(), k -> new HashMap<>()).put(playerId, role);
    }

    /** Load roles from config. */
    public void load(ConfigurationSection section) {
        roles.clear();
        if (section == null) return;
        for (String plotIdStr : section.getKeys(false)) {
            UUID plotId = UUID.fromString(plotIdStr);
            Map<UUID, ClaimRole> map = new HashMap<>();
            ConfigurationSection sub = section.getConfigurationSection(plotIdStr);
            if (sub != null) {
                for (String playerIdStr : sub.getKeys(false)) {
                    UUID playerId = UUID.fromString(playerIdStr);
                    String roleStr = sub.getString(playerIdStr, "NONE");
                    ClaimRole role = ClaimRole.fromName(roleStr);
                    map.put(playerId, role);
                }
            }
            roles.put(plotId, map);
        }
    }

    /** Save roles to config. */
    public void save(ConfigurationSection section) {
        if (section == null) return;
        for (Map.Entry<UUID, Map<UUID, ClaimRole>> entry : roles.entrySet()) {
            String plotIdStr = entry.getKey().toString();
            ConfigurationSection sub = section.createSection(plotIdStr);
            for (Map.Entry<UUID, ClaimRole> e : entry.getValue().entrySet()) {
                sub.set(e.getKey().toString(), e.getValue().name());
            }
        }
    }
}
