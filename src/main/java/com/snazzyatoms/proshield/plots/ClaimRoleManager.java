// path: src/main/java/com/snazzyatoms/proshield/plots/ClaimRoleManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class ClaimRoleManager {

    private final ProShield plugin;

    // key -> (playerUUID -> role)
    private final Map<String, Map<UUID, ClaimRole>> roles = new HashMap<>();

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
        reloadFromConfig();
    }

    public void reloadFromConfig() {
        roles.clear();
        ConfigurationSection claims = plugin.getConfig().getConfigurationSection("claims");
        if (claims == null) return;
        for (String key : claims.getKeys(false)) {
            ConfigurationSection sec = claims.getConfigurationSection(key);
            if (sec == null) continue;
            ConfigurationSection r = sec.getConfigurationSection("roles");
            if (r == null) continue;
            Map<UUID, ClaimRole> map = new HashMap<>();
            for (String uuidStr : r.getKeys(false)) {
                try {
                    UUID u = UUID.fromString(uuidStr);
                    ClaimRole role = ClaimRole.from(r.getString(uuidStr), ClaimRole.MEMBER);
                    map.put(u, role);
                } catch (Exception ignored) {}
            }
            if (!map.isEmpty()) roles.put(key, map);
        }
    }

    public ClaimRole getRole(Location loc, UUID player) {
        String key = key(loc);
        Map<UUID, ClaimRole> map = roles.get(key);
        if (map == null) return ClaimRole.VISITOR;
        return map.getOrDefault(player, ClaimRole.VISITOR);
    }

    public String getRoleName(UUID owner, UUID player) {
        // Human-readable, used by claim messages/listing
        ClaimRole role = ClaimRole.VISITOR;
        // (No owner context needed here; kept for future expansion)
        return role.name();
    }

    public void setRole(String claimKey, UUID player, ClaimRole role) {
        roles.computeIfAbsent(claimKey, k -> new HashMap<>()).put(player, role);
        // also persist to config
        String path = "claims." + claimKey + ".roles." + player.toString();
        plugin.getConfig().set(path, role.name());
        plugin.saveConfig();
    }

    public void clearRole(String claimKey, UUID player) {
        Map<UUID, ClaimRole> map = roles.get(claimKey);
        if (map != null) map.remove(player);
        plugin.getConfig().set("claims." + claimKey + ".roles." + player.toString(), null);
        plugin.saveConfig();
    }

    private String key(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getChunk().getX() + ":" + loc.getChunk().getZ();
    }
}
