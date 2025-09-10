// src/main/java/com/snazzyatoms/proshield/roles/ClaimRole.java
package com.snazzyatoms.proshield.roles;

import org.bukkit.ChatColor;

public enum ClaimRole {
    OWNER(ChatColor.DARK_RED + "Owner", 5), // 🔹 Added owner role
    COOWNER(ChatColor.LIGHT_PURPLE + "Co-Owner", 4),
    // Back-compat alias for older references:
    CO_OWNER(ChatColor.LIGHT_PURPLE + "Co-Owner", 4),
    BUILDER(ChatColor.AQUA + "Builder", 3),
    CONTAINER(ChatColor.GOLD + "Container", 2),
    MEMBER(ChatColor.GREEN + "Member", 1),
    VISITOR(ChatColor.GRAY + "Visitor", 0);

    private final String display;
    private final int level;

    ClaimRole(String display, int level) {
        this.display = display;
        this.level = level;
    }

    public String display() {
        return display;
    }

    public int level() {
        return level;
    }

    /**
     * Returns true if this role has at least the same or higher privileges
     * compared to another role.
     */
    public boolean atLeast(ClaimRole other) {
        return this.level >= other.level;
    }

    /**
     * Convert string (config, commands, etc.) into a ClaimRole.
     * Accepts multiple aliases for compatibility.
     */
    public static ClaimRole fromString(String s) {
        if (s == null) return VISITOR;
        String k = s.trim().toUpperCase().replace('-', '_').replace(' ', '_');

        // Aliases
        if ("CO_OWNER".equals(k)) return COOWNER;
        if ("OWNER".equals(k)) return OWNER;

        try {
            return ClaimRole.valueOf(k);
        } catch (IllegalArgumentException ex) {
            return VISITOR;
        }
    }
}
