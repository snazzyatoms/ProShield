package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;

import java.util.*;

/**
 * ClaimRoleManager
 * - Handles trusted players, roles, per-player permissions.
 * - Persistence via saveAll/loadAll stubs.
 */
public class ClaimRoleManager {

    private final ProShield plugin;

    // Map<plotId, Map<playerName, PlayerRoleData>>
    private final Map<UUID, Map<String, PlayerRoleData>> trusted = new HashMap<>();

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /* ========================
     * Core Trust / Untrust Ops
     * ======================== */
    public boolean trustPlayer(Plot plot, String playerName, String role) {
        Map<String, PlayerRoleData> map = trusted.computeIfAbsent(plot.getId(), k -> new HashMap<>());
        if (map.containsKey(playerName)) return false;
        map.put(playerName, new PlayerRoleData(role));
        return true;
    }

    public boolean untrustPlayer(Plot plot, String playerName) {
        Map<String, PlayerRoleData> map = trusted.get(plot.getId());
        if (map == null) return false;
        return map.remove(playerName) != null;
    }

    public Map<String, String> getTrusted(UUID plotId) {
        Map<String, PlayerRoleData> map = trusted.getOrDefault(plotId, Collections.emptyMap());
        Map<String, String> roles = new HashMap<>();
        for (Map.Entry<String, PlayerRoleData> e : map.entrySet()) {
            roles.put(e.getKey(), e.getValue().getRole());
        }
        return roles;
    }

    public String getRole(UUID plotId, String playerName) {
        Map<String, PlayerRoleData> map = trusted.get(plotId);
        if (map == null) return null;
        PlayerRoleData data = map.get(playerName);
        return data != null ? data.getRole() : null;
    }

    /* ==================
     * Permissions system
     * ================== */
    public Map<String, Boolean> getPermissions(UUID plotId, String playerName) {
        Map<String, PlayerRoleData> map = trusted.get(plotId);
        if (map == null) return Collections.emptyMap();
        PlayerRoleData data = map.get(playerName);
        if (data == null) return Collections.emptyMap();
        return data.getPermissions();
    }

    public void setPermission(UUID plotId, String playerName, String key, boolean value) {
        Map<String, PlayerRoleData> map = trusted.computeIfAbsent(plotId, k -> new HashMap<>());
        PlayerRoleData data = map.computeIfAbsent(playerName, k -> new PlayerRoleData("trusted"));
        data.setPermission(key, value);
    }

    /* ==================
     * Persistence Stubs
     * ================== */
    public void saveAll() {
        // TODO: Write trusted map to roles.yml in plugin.getDataFolder()
        // Keep roles and permissions per plot
    }

    public void loadAll() {
        // TODO: Read roles.yml from plugin.getDataFolder() and rebuild trusted map
    }

    /* ==================
     * Data Class
     * ================== */
    public static class PlayerRoleData {
        private String role;
        private final Map<String, Boolean> permissions = new HashMap<>();

        public PlayerRoleData(String role) {
            this.role = role;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Map<String, Boolean> getPermissions() {
            return permissions;
        }

        public void setPermission(String key, boolean value) {
            permissions.put(key, value);
        }
    }
}
