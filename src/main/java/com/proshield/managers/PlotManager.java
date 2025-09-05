package com.snazzyatoms.proshield.managers;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlotManager {

    public static final class Claim {
        public final UUID owner;
        public final String world;
        public final int x, y, z;
        public final int radius;

        public Claim(UUID owner, String world, int x, int y, int z, int radius) {
            this.owner = owner;
            this.world = world;
            this.x = x; this.y = y; this.z = z;
            this.radius = radius;
        }

        public Location toLocation() {
            World w = Bukkit.getWorld(world);
            return (w == null) ? null : new Location(w, x, y, z);
        }
    }

    private final ProShield plugin;
    private final Map<UUID, Claim> claims = new ConcurrentHashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadFromConfig();
    }

    /* ======================
       Public API (GUI calls)
       ====================== */

    public boolean createClaim(Player player) {
        UUID id = player.getUniqueId();
        if (claims.containsKey(id)) return false;

        Location loc = player.getLocation();
        int defaultRadius = Math.max(1, plugin.getConfig().getInt("settings.default-radius", 16));

        Claim claim = new Claim(
                id,
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ(),
                defaultRadius
        );

        claims.put(id, claim);
        saveClaimToConfig(claim);
        return true;
    }

    /** Returns a pretty string or null if not claimed */
    public String getClaimInfo(Player player) {
        Claim c = claims.get(player.getUniqueId());
        if (c == null) return null;
        return String.format(
                "World=%s | Center X=%d, Y=%d, Z=%d | Radius=%d",
                c.world, c.x, c.y, c.z, c.radius
        );
    }

    public boolean removeClaim(Player player) {
        UUID id = player.getUniqueId();
        Claim c = claims.remove(id);
        if (c == null) return false;
        deleteClaimFromConfig(id);
        return true;
    }

    /* ======================
       Persistence
       ====================== */

    public void reloadFromDisk() {
        claims.clear();
        plugin.reloadConfig();
        loadFromConfig();
    }

    private void loadFromConfig() {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.isConfigurationSection("claims")) return;

        for (String key : Objects.requireNonNull(cfg.getConfigurationSection("claims")).getKeys(false)) {
            try {
                UUID owner = UUID.fromString(key);
                String world = cfg.getString("claims." + key + ".world", "world");
                int x = cfg.getInt("claims." + key + ".x");
                int y = cfg.getInt("claims." + key + ".y");
                int z = cfg.getInt("claims." + key + ".z");
                int radius = cfg.getInt("claims." + key + ".radius", Math.max(1, cfg.getInt("settings.default-radius", 16)));

                // Be tolerant if world is missing; still load so info can be removed
                Claim c = new Claim(owner, world, x, y, z, Math.max(1, radius));
                claims.put(owner, c);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load claim for key " + key + ": " + e.getMessage());
            }
        }
    }

    private void saveClaimToConfig(Claim claim) {
        FileConfiguration cfg = plugin.getConfig();
        String base = "claims." + claim.owner.toString();
        cfg.set(base + ".world", claim.world);
        cfg.set(base + ".x", claim.x);
        cfg.set(base + ".y", claim.y);
        cfg.set(base + ".z", claim.z);
        cfg.set(base + ".radius", claim.radius);
        plugin.saveConfig();
    }

    private void deleteClaimFromConfig(UUID owner) {
        FileConfiguration cfg = plugin.getConfig();
        cfg.set("claims." + owner.toString(), null);
        plugin.saveConfig();
    }

    /* ======================
       Helpers (optional)
       ====================== */

    public boolean hasClaim(UUID owner) {
        return claims.containsKey(owner);
    }

    public Optional<Claim> getClaim(UUID owner) {
        return Optional.ofNullable(claims.get(owner));
    }
}
