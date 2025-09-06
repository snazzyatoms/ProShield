package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlotManager {
    private final ProShield plugin;
    private final HashMap<UUID, Claim> claims;

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.claims = new HashMap<>();
    }

    public void createClaim(Player player) {
        claims.put(player.getUniqueId(), new Claim(player.getUniqueId(), player.getLocation()));
        player.sendMessage("✅ Claim created at your current location.");
    }

    public void getClaimInfo(Player player) {
        Claim claim = claims.get(player.getUniqueId());
        if (claim != null) {
            player.sendMessage("📍 Claim Info: " + claim.toString());
        } else {
            player.sendMessage("⚠️ You don’t have a claim yet.");
        }
    }

    public void removeClaim(Player player) {
        if (claims.remove(player.getUniqueId()) != null) {
            player.sendMessage("❌ Claim removed.");
        } else {
            player.sendMessage("⚠️ You don’t have a claim to remove.");
        }
    }
}
