package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;

/**
 * ClaimRoleManager
 * - Handles roles & per-player permissions inside claims
 * - Supports trust/untrust, transfer, and permission checks
 *
 * Preserves prior behavior; implementation now references PlotManager provided by ProShield.
 */
public class ClaimRoleManager {

    private final ProShield plugin;
    private final PlotManager plotManager;

    /**
     * Maps: plotId -> {playerName -> PlayerRoleData}
     */
    private final Map<UUID, Map<String, PlayerRoleData>> roleCache = new HashMap<>();

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
    }

    /* ======================================================
     * PLAYER ROLE DATA
     * ====================================================== */
    public static class PlayerRoleData {
        private String role;
        private final Map<String, Boolean> permissions = new HashMap<>();

        public PlayerRoleData(String role) {
            this.role = (role == null ? "trusted" : role).toLowerCase(Locale.ROOT);
        }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = (role == null ? "trusted" : role).toLowerCase(Locale.ROOT); }
        public boolean getPermission(String key, boolean def) { return permissions.getOrDefault(key.toLowerCase(Locale.ROOT), def); }
        public void setPermission(String key, boolean value) { permissions.put(key.toLowerCase(Locale.ROOT), value); }
        public Map<String, Boolean> getAllPermissions() { return new HashMap<>(permissions); }
    }

    /* ======================================================
     * TRUST / UNTRUST
     * ====================================================== */
    public boolean trustPlayer(Plot plot, String playerName, String role) {
        UUID plotId = plot.getId();
        Map<String, PlayerRoleData> map = roleCache.computeIfAbsent(plotId, k -> new HashMap<>());
        if (map.containsKey(playerName)) return false;
        map.put(playerName, new PlayerRoleData(role));
        return true;
    }

    public boolean untrustPlayer(Plot plot, String playerName) {
        UUID plotId = plot.getId();
        Map<String, PlayerRoleData> map = roleCache.get(plotId);
        if (map == null || !map.containsKey(playerName)) return false;
        map.remove(playerName);
        return true;
    }

    public boolean transferOwnership(Plot plot, String newOwnerName) {
        if (newOwnerName == null || newOwnerName.isBlank()) return false;
        OfflinePlayer offline = Bukkit.getOfflinePlayer(newOwnerName);
        if (offline == null || offline.getUniqueId() == null) return false;
        UUID newOwnerId = offline.getUniqueId();
        if (plot.isOwner(newOwnerId)) return false; // same player
        plot.setOwner(newOwnerId);
        roleCache.remove(plot.getId()); // clear roles for new owner
        return true;
    }

    /* ======================================================
     * ROLE QUERIES
     * ====================================================== */
    public String getRole(UUID plotId, String playerName) {
        Map<String, PlayerRoleData> map = roleCache.get(plotId);
        if (map == null) return null;
        PlayerRoleData data = map.get(playerName);
        return data != null ? data.getRole() : null;
    }

    public boolean isTrusted(UUID plotId, String playerName) {
        Map<String, PlayerRoleData> map = roleCache.get(plotId);
        return map != null && map.containsKey(playerName);
    }

    /** Get all trusted players + their roles for GUI menus */
    public Map<String, String> getTrusted(UUID plotId) {
        Map<String, PlayerRoleData> map = roleCache.getOrDefault(plotId, Collections.emptyMap());
        Map<String, String> out = new HashMap<>();
        map.forEach((name, data) -> out.put(name, data.getRole()));
        return out;
    }

    /** Permissions API */
    public Map<String, Boolean> getPermissions(UUID plotId, String playerName) {
        Map<String, PlayerRoleData> map = roleCache.get(plotId);
        if (map == null) return Collections.emptyMap();
        PlayerRoleData data = map.get(playerName);
        return data != null ? data.getAllPermissions() : Collections.emptyMap();
    }

    public void setPermission(UUID plotId, String playerName, String key, boolean value) {
        Map<String, PlayerRoleData> map = roleCache.get(plotId);
        if (map == null) return;
        PlayerRoleData data = map.get(playerName);
        if (data != null) data.setPermission(key, value);
    }

    /* ======================================================
     * PERMISSION CHECKS
     * ====================================================== */
    public boolean canManage(UUID playerId, UUID plotId) {
        return hasPermission(playerId, plotId, "build", Set.of("owner", "co-owner", "builder"));
    }

    public boolean canInteract(UUID playerId, UUID plotId) {
        return hasPermission(playerId, plotId, "interact", Set.of("owner", "co-owner", "builder", "trusted"));
    }

    public boolean canContainers(UUID playerId, UUID plotId) {
        return hasPermission(playerId, plotId, "containers", Set.of("owner", "co-owner", "builder"));
    }

    public boolean canUnclaim(UUID playerId, UUID plotId) {
        return hasPermission(playerId, plotId, "unclaim", Set.of("owner", "co-owner"));
    }

    /* ======================================================
     * INTERNAL HELPERS
     * ====================================================== */
    private boolean hasPermission(UUID playerId, UUID plotId, String permKey, Set<String> allowedRoles) {
        Plot plot = plotManager.getPlotById(plotId); // helper we’ll add below for completeness
        if (plot == null) return false;

        if (plot.isOwner(playerId)) return true; // owner bypass

        String playerName = plotManager.getPlayerName(playerId);
        if (playerName == null) return false;

        Map<String, PlayerRoleData> map = roleCache.get(plotId);
        if (map == null) return false;

        PlayerRoleData data = map.get(playerName);
        if (data == null) return false;

        if (data.getAllPermissions().containsKey(permKey)) {
            return data.getPermission(permKey, false); // explicit override
        }

        String role = data.getRole();
        return role != null && allowedRoles.contains(role.toLowerCase(Locale.ROOT));
    }
}
