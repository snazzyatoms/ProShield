// path: src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    private final ProShield plugin;
    private final Map<UUID, Claim> claims = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadClaims();
    }

    /** ==============================
     *  CLAIM CREATION
     *  ============================== */
    public boolean createClaim(Player player) {
        UUID uuid = player.getUniqueId();
        if (claims.containsKey(uuid)) {
            return false; // already has a claim
        }

        Location loc = player.getLocation();
        Claim claim = new Claim(uuid, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockZ());
        claims.put(uuid, claim);

        saveClaim(claim);
        return true;
    }

    /** ==============================
     *  CLAIM INFO
     *  ============================== */
    public String getClaimInfo(Location location) {
        for (Claim claim : claims.values()) {
            if (claim.isInside(location)) {
                return "Owner: " + claim.getOwner() + " | World: " + claim.getWorld();
            }
        }
        return null;
    }

    /** ==============================
     *  CLAIM REMOVAL
     *  ============================== */
    public boolean removeClaim(Player player) {
        UUID uuid = player.getUniqueId();
        if (!claims.containsKey(uuid)) {
            return false;
        }
        claims.remove(uuid);

        plugin.getConfig().set("claims." + uuid, null);
        plugin.saveConfig();
        return true;
    }

    /** ==============================
     *  LOAD + SAVE
     *  ============================== */
    private void loadClaims() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("claims");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            String world = section.getString(key + ".world");
            int x = section.getInt(key + ".x");
            int z = section.getInt(key + ".z");
            Claim claim = new Claim(uuid, world, x, z);
            claims.put(uuid, claim);
        }
    }

    private void saveClaim(Claim claim) {
        String path = "claims." + claim.getOwner().toString();
        plugin.getConfig().set(path + ".world", claim.getWorld());
        plugin.getConfig().set(path + ".x", claim.getX());
        plugin.getConfig().set(path + ".z", claim.getZ());
        plugin.saveConfig();
    }

    public void saveAll() {
        for (Claim claim : claims.values()) {
            saveClaim(claim);
        }
    }

    /** ==============================
     *  UTILS
     *  ============================== */
    public int getClaimCount() {
        return claims.size();
    }
}
