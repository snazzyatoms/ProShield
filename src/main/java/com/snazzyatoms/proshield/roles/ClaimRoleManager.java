package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.plots.Plot;

import java.util.*;

/**
 * ClaimRoleManager
 * - Manages trusted players and roles per claim
 * - Persistence hooks: loadAll() and saveAll()
 */
public class ClaimRoleManager {

    // Internal: claimId -> playerName -> role data
    private final Map<UUID, Map<String, PlayerRoleData>> claimRoles = new HashMap<>();

    public static class PlayerRoleData {
        private String role;
        private final Map<String, Boolean> permissions = new HashMap<>();

        public PlayerRoleData(String role) {
            this.role = role;
        }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public Map<String, Boolean> getPermissions() { return permissions; }
        public void setPermission(String key, boolean value) { permissions.put(key, value); }
    }

    // ====== Role API ======
    public boolean trustPlayer(Plot plot, String playerName, String role) {
        Map<String, PlayerRoleData> map = claimRoles.computeIfAbsent(plot.getId(), k -> new HashMap<>());
        if (map.containsKey(playerName)) return false;
        map.put(playerName, new PlayerRoleData(role));
        return true;
    }

    public boolean untrustPlayer(Plot plot, String playerName) {
        Map<String, PlayerRoleData> map = claimRoles.get(plot.getId());
        if (map == null) return false;
        return map.remove(playerName) != null;
    }

    public String getRole(UUID plotId, String playerName) {
        Map<String, PlayerRoleData> map = claimRoles.get(plotId);
        if (map == null) return null;
        PlayerRoleData data = map.get(playerName);
        return data != null ? data.getRole() : null;
    }

    public Map<String, String> getTrusted(UUID plotId) {
        Map<String, String> result = new HashMap<>();
        Map<String, PlayerRoleData> map = claimRoles.get(plotId);
        if (map != null) {
            for (Map.Entry<String, PlayerRoleData> e : map.entrySet()) {
                result.put(e.getKey(), e.getValue().getRole());
            }
        }
        return result;
    }

    public Map<String, Boolean> getPermissions(UUID plotId, String playerName) {
        Map<String, PlayerRoleData> map = claimRoles.get(plotId);
        if (map == null) return Collections.emptyMap();
        PlayerRoleData data = map.get(playerName);
        if (data == null) return Collections.emptyMap();
        return new HashMap<>(data.getPermissions());
    }

    public void setPermission(UUID plotId, String playerName, String key, boolean value) {
        Map<String, PlayerRoleData> map = claimRoles.computeIfAbsent(plotId, k -> new HashMap<>());
        PlayerRoleData data = map.computeIfAbsent(playerName, n -> new PlayerRoleData("trusted"));
        data.setPermission(key, value);
    }

    // ====== Persistence Hooks ======
    public void loadAll() {
        // TODO: Load from roles.yml (YAMLConfiguration)
        // Example plan:
        // File file = new File(plugin.getDataFolder(), "roles.yml");
        // YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        // Parse into claimRoles map
    }

    public void saveAll() {
        // TODO: Save to roles.yml (YAMLConfiguration)
        // Example plan:
        // YamlConfiguration cfg = new YamlConfiguration();
        // Iterate claimRoles, dump into config
        // cfg.save(file);
    }
}
