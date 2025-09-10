// path: src/main/java/com/snazzyatoms/proshield/roles/ClaimRole.java
package com.snazzyatoms.proshield.roles;

/**
 * Roles for claim access. Keep names stable for YAML/back-compat.
 */
public enum ClaimRole {
    VISITOR,      // walk only
    MEMBER,       // basic interactions (doors/buttons)
    CONTAINER,    // can use containers
    BUILDER,      // can build/break
    CO_OWNER,     // almost full access
    OWNER;        // the claim owner

    public static ClaimRole fromString(String s, ClaimRole def) {
        if (s == null) return def;
        try {
            return ClaimRole.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return def;
        }
    }
}
