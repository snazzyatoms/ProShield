// src/main/java/com/snazzyatoms/proshield/roles/ClaimRole.java
package com.snazzyatoms.proshield.roles;

import org.bukkit.ChatColor;

public enum ClaimRole {
    VISITOR(ChatColor.GRAY + "Visitor", 0),
    MEMBER(ChatColor.GREEN + "Member", 1),
    CONTAINER(ChatColor.GOLD + "Container", 2),
    BUILDER(ChatColor.AQUA + "Builder", 3),
    COOWNER(ChatColor.LIGHT_PURPLE + "Co-Owner", 4),
    // Back-compat alias for older references:
    CO_OWNER(ChatColor.LIGHT_PURPLE + "Co-Owner", 4);

    private final String display;
    private final int level;

    ClaimRole(String display, int level) {
        this.display = display;
        this.level = level;
    }

    public String display() { return display; }
    public int level() { return level; }

    public boolean atLeast(ClaimRole other) {
        return this.level >= other.level;
    }

    public static ClaimRole fromString(String s) {
        if (s == null) return VISITOR;
        String k = s.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        // Accept both COOWNER and CO_OWNER
        if ("CO_OWNER".equals(k)) return COOWNER;
        try {
            return ClaimRole.valueOf(k);
        } catch (IllegalArgumentException ex) {
            return VISITOR;
        }
    }
}
