package com.snazzyatoms.proshield.roles;

/**
 * Claim roles with simple helper for parsing.
 */
public enum ClaimRole {
    VISITOR,
    MEMBER,
    CONTAINER,
    BUILDER,
    COOWNER,
    OWNER;

    public static ClaimRole fromString(String s) {
        if (s == null) return VISITOR;
        try {
            return ClaimRole.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return VISITOR;
        }
    }
}
