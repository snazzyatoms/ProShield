package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * ClaimRoleManager (v1.2.6 FINAL)
 *
 * - Handles persistent storage of player roles inside plots
 * - Converts safely between String <-> ClaimRole
 * - All role names mapped via ClaimRole.fromName()
 */
public class ClaimRoleManager {

    private final ProShield plugin;
    private final Map<UUID, Map<UUID, ClaimRole>> roleCache = new HashMap<>();
    private final FileConfiguration config;

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        load();
    }

    /** Get role of target inside plot. */
    public ClaimRole getRole(Plot plot, UUID target) {
        if (plot == null || target == null) return ClaimRole.NONE;
        return plot.getTrusted().getOrDefault(target, ClaimRole.NONE);
    }

    /** Set role and persist. */
    public void setRole(Plot plot, UUID target, ClaimRole role) {
        if (plot == null || target == null || role == null) return;
        plot.getTrusted().put(target, role);
        save(plot);
    }

    /** Load all plot role data from config. */
    public void load() {
        roleCache.clear();
        if (!config.isConfigurationSection("roles")) return;

        for (String plotId : config.getConfigurationSection("roles").getKeys(false)) {
            UUID plotUUID = UUID.fromString(plotId);
            Map<UUID, ClaimRole> map = new HashMap<>();

            for (String playerId : config.getConfigurationSection("roles." + plotId).getKeys(false)) {
                UUID playerUUID = UUID.fromString(playerId);
                String roleName = config.getString("roles." + plotId + "." + playerId, "NONE");
                ClaimRole role = ClaimRole.fromName(roleName); // ✅ String → ClaimRole
                map.put(playerUUID, role);
            }
            roleCache.put(plotUUID, map);
        }
    }

    /** Save a single plot's role data back into config. */
    public void save(Plot plot) {
        if (plot == null) return;
        UUID plotId = plot.getId();

        // clear section first
        config.set("roles." + plotId.toString(), null);

        Map<UUID, ClaimRole> map = plot.getTrusted();
        if (map != null) {
            for (Map.Entry<UUID, ClaimRole> e : map.entrySet()) {
                String playerId = e.getKey().toString();
                String roleName = e.getValue().name(); // ✅ ClaimRole → String
                config.set("roles." + plotId + "." + playerId, roleName);
            }
        }
        plugin.saveConfig();
    }

    /** Save all cached roles. */
    public void saveAll() {
        for (UUID plotId : roleCache.keySet()) {
            Map<UUID, ClaimRole> map = roleCache.get(plotId);
            config.set("roles." + plotId.toString(), null);
            for (Map.Entry<UUID, ClaimRole> e : map.entrySet()) {
                config.set("roles." + plotId + "." + e.getKey(), e.getValue().name()); // ✅
            }
        }
        plugin.saveConfig();
    }
}
