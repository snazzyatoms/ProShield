package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

/**
 * ClaimRoleManager
 * - trusted players per plot
 * - role strings + per-player boolean flags
 * - chat-driven "assignRoleViaChat" for GUIManager flow
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

    // === Wrappers for GUI ===
    public boolean addTrusted(UUID plotId, String playerName) {
        Plot plot = plugin.getPlotManager().getPlotById(plotId);
        if (plot == null) return false;
        return trustPlayer(plot, playerName, "trusted");
    }

    public boolean removeTrusted(UUID plotId, String playerName) {
        Plot plot = plugin.getPlotManager().getPlotById(plotId);
        if (plot == null) return false;
        return untrustPlayer(plot, playerName);
    }

    /* ==================
     * Query Ops
     * ================== */
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

    /* ===========================
     * Chat-driven role assignment
     * =========================== */
    public void assignRoleViaChat(Player player, String chatLine) {
        if (player == null || chatLine == null || chatLine.isEmpty()) return;

        // Format: "<playerName> [role]"  (role optional → defaults to "trusted")
        String[] parts = chatLine.trim().split("\\s+", 2);
        String targetName = parts[0];
        String role = parts.length > 1 ? parts[1] : "trusted";

        Location loc = player.getLocation();
        UUID plotId = plugin.getPlotManager().getClaimIdAt(loc);
        if (plotId == null) {
            player.sendMessage("§cNo claim found here.");
            return;
        }
        Plot plot = plugin.getPlotManager().getPlotById(plotId);
        if (plot == null) {
            player.sendMessage("§cNo claim data found.");
            return;
        }
        if (!plot.isOwner(player.getUniqueId()) && !player.hasPermission("proshield.admin")) {
            player.sendMessage("§cOnly the owner (or admin) can manage roles.");
            return;
        }

        Map<String, PlayerRoleData> map = trusted.computeIfAbsent(plotId, k -> new HashMap<>());
        PlayerRoleData data = map.get(targetName);
        if (data == null) {
            data = new PlayerRoleData(role);
            map.put(targetName, data);
            player.sendMessage("§aTrusted §f" + targetName + " §ain this claim as §e" + role + "§a.");
        } else {
            data.setRole(role);
            player.sendMessage("§aUpdated §f" + targetName + " §arole to §e" + role + "§a.");
        }
    }

    /* ==================
     * Persistence Stubs
     * ================== */
    public void saveAll() {
        // TODO: persist trusted-> roles.yml
        File f = new File(plugin.getDataFolder(), "roles.yml");
        // write out as needed
    }

    public void loadAll() {
        // TODO: read roles.yml and rebuild trusted map
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

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public Map<String, Boolean> getPermissions() { return permissions; }
        public void setPermission(String key, boolean value) { permissions.put(key, value); }
    }
}
