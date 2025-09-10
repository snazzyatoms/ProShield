package com.snazzyatoms.proshield.roles;

/**
 * Enum representing different claim roles in ProShield.
 * Controls player access inside claims.
 */
public enum ClaimRole {
    VISITOR,     // Can enter claim but no interactions
    MEMBER,      // Can use doors, buttons, levers, etc.
    CONTAINER,   // Can access chests, barrels, furnaces
    BUILDER,     // Can break/place blocks
    CO_OWNER,    // Near full permissions
    OWNER;       // Claim creator

    public boolean isAtLeast(ClaimRole other) {
        return this.ordinal() >= other.ordinal();
    }
}
