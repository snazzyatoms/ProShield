package com.snazzyatoms.proshield.plots;

import java.util.*;

/**
 * Represents a single claimed plot (chunk).
 * Stores ownership, trusted players, roles, and settings.
 */
public class Plot {

    private final UUID owner;
    private final String world;
    private final int x;
    private final int z;

    private final Set<UUID> trustedPlayers = new HashSet<>();
    private final Map<UUID, String> roles = new HashMap<>();
    private PlotSettings settings;

    public Plot(UUID owner, String world, int x, int z) {
        this.owner = owner;
        this.world = world;
        this.x = x;
        this.z = z;
        this.settings = new PlotSettings();
    }

    /* ---------------------------------------------------------
     * 🔹 Ownership
     * --------------------------------------------------------- */
    public UUID getOwner() { return owner; }

    public boolean isOwner(UUID playerId) {
        return owner.equals(playerId);
    }

    /* ---------------------------------------------------------
     * 🔹 Trusted Players
     * --------------------------------------------------------- */
    public void addTrusted(UUID playerId) {
        trustedPlayers.add(playerId);
    }

    public void removeTrusted(UUID playerId) {
        trustedPlayers.remove(playerId);
        roles.remove(playerId);
    }

    public boolean isTrusted(UUID playerId) {
        return trustedPlayers.contains(playerId);
    }

    public Set<UUID> getTrustedPlayers() {
        return Collections.unmodifiableSet(trustedPlayers);
    }

    /* ---------------------------------------------------------
     * 🔹 Roles
     * --------------------------------------------------------- */
    public void assignRole(UUID playerId, String role) {
        if (trustedPlayers.contains(playerId)) {
            roles.put(playerId, role);
        }
    }

    public String getRole(UUID playerId) {
        return roles.getOrDefault(playerId, "Visitor");
    }

    public Map<UUID, String> getRoles() {
        return Collections.unmodifiableMap(roles);
    }

    /* ---------------------------------------------------------
     * 🔹 Settings
     * --------------------------------------------------------- */
    public PlotSettings getSettings() {
        return settings;
    }

    public void setSettings(PlotSettings settings) {
        this.settings = settings;
    }

    /* ---------------------------------------------------------
     * 🔹 Location
     * --------------------------------------------------------- */
    public String getWorld() { return world; }
    public int getX() { return x; }
    public int getZ() { return z; }

    /* ---------------------------------------------------------
     * 🔹 Serialization
     * --------------------------------------------------------- */
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("owner", owner.toString());
        map.put("world", world);
        map.put("x", x);
        map.put("z", z);

        List<String> trustedList = new ArrayList<>();
        for (UUID id : trustedPlayers) {
            trustedList.add(id.toString());
        }
        map.put("trusted", trustedList);

        Map<String, String> roleMap = new HashMap<>();
        for (Map.Entry<UUID, String> entry : roles.entrySet()) {
            roleMap.put(entry.getKey().toString(), entry.getValue());
        }
        map.put("roles", roleMap);

        map.put("settings", settings.serialize());
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Plot deserialize(Map<String, Object> map) {
        UUID owner = UUID.fromString((String) map.get("owner"));
        String world = (String) map.get("world");
        int x = (int) map.get("x");
        int z = (int) map.get("z");

        Plot plot = new Plot(owner, world, x, z);

        List<String> trustedList = (List<String>) map.getOrDefault("trusted", new ArrayList<>());
        for (String id : trustedList) {
            plot.addTrusted(UUID.fromString(id));
        }

        Map<String, String> roleMap = (Map<String, String>) map.getOrDefault("roles", new HashMap<>());
        for (Map.Entry<String, String> entry : roleMap.entrySet()) {
            plot.assignRole(UUID.fromString(entry.getKey()), entry.getValue());
        }

        Map<String, Object> settingsMap = (Map<String, Object>) map.get("settings");
        if (settingsMap != null) {
            plot.setSettings(PlotSettings.deserialize(settingsMap));
        }

        return plot;
    }
}
