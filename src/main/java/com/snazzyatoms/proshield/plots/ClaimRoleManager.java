package com.snazzyatoms.proshield.plots;

import java.util.*;

/**
 * Minimal roles facade for 1.1.9.
 * Backed by PlotManager's owner + trusted list.
 * (We keep it tiny & safe; real role tiers can arrive in 1.2/2.0.)
 */
public class ClaimRoleManager {

    public enum Role {
        OWNER,   // claim owner
        TRUSTED, // on trusted list
        GUEST    // everyone else
    }

    private final PlotManager plotManager;

    public ClaimRoleManager(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    /** Resolve role for a player at a location's claim. */
    public Role getRole(java.util.UUID player, org.bukkit.Location loc) {
        var opt = plotManager.getClaim(loc);
        if (opt.isEmpty()) return Role.GUEST;
        var c = opt.get();
        if (c.getOwner().equals(player)) return Role.OWNER;
        if (c.getTrusted().contains(player)) return Role.TRUSTED;
        return Role.GUEST;
    }

    /** Convenience checks */
    public boolean isOwner(java.util.UUID player, org.bukkit.Location loc) {
        return getRole(player, loc) == Role.OWNER;
    }

    public boolean isTrusted(java.util.UUID player, org.bukkit.Location loc) {
        var r = getRole(player, loc);
        return r == Role.OWNER || r == Role.TRUSTED;
    }
}
