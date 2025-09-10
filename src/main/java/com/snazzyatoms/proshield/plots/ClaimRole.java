package com.snazzyatoms.proshield.roles;

/**
 * Claim access roles. We keep CO_OWNER and also provide COOWNER
 * to preserve any legacy references.
 */
public enum ClaimRole {
    VISITOR,
    MEMBER,
    CONTAINER,
    BUILDER,
    CO_OWNER,
    // Legacy alias to preserve older references:
    COOWNER;

    /**
     * Case-insensitive string to role. Defaults to MEMBER if unknown.
     * Preserves prior behavior (no exception).
     */
    public static ClaimRole fromString(String s) {
        if (s == null || s.isEmpty()) return MEMBER;
        String k = s.trim().replace('-', '_').toUpperCase();
        for (ClaimRole r : values()) {
            if (r.name().equalsIgnoreCase(k)) return r;
        }
        // handle common aliases
        if (k.equals("COOWNER")) return COOWNER;
        if (k.equals("CO_OWNER")) return CO_OWNER;
        return MEMBER;
    }

    /** true if this role is at least CONTAINER privileges */
    public boolean canUseContainers() {
        return this == CONTAINER || this == BUILDER || this == CO_OWNER || this == COOWNER;
    }

    /** true if this role can build/break */
    public boolean canBuild() {
        return this == BUILDER || this == CO_OWNER || this == COOWNER;
    }

    /** true if role is co-owner tier */
    public boolean isCoOwner() {
        return this == CO_OWNER || this == COOWNER;
    }
}
