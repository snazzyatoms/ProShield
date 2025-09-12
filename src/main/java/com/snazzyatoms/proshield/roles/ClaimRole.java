// src/main/java/com/snazzyatoms/proshield/roles/ClaimRole.java
package com.snazzyatoms.proshield.roles;

/**
 * Represents the different roles inside a claim.
 * Each role defines its base permissions.
 */
public enum ClaimRole {
    NONE("None"),
    VISITOR("Visitor"),
    MEMBER("Member"),
    TRUSTED("Trusted"),
    BUILDER("Builder"),
    CONTAINER("Container"),
    MODERATOR("Moderator"),
    MANAGER("Manager");

    private final String displayName;

    ClaimRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Whether this role can build/break blocks.
     */
    public boolean canBuild() {
        return this == BUILDER || this == TRUSTED || this == MANAGER;
    }

    /**
     * Whether this role can open containers.
     */
    public boolean canContainers() {
        return this == CONTAINER || this == TRUSTED || this == MANAGER || this == BUILDER;
    }

    /**
     * Whether this role can manage trust.
     */
    public boolean canManageTrust() {
        return this == MODERATOR || this == MANAGER;
    }

    /**
     * Whether this role can unclaim land.
     */
    public boolean canUnclaim() {
        return this == MANAGER;
    }

    /**
     * Whether this role can manage claim flags.
     */
    public boolean canFlags() {
        return this == MODERATOR || this == MANAGER;
    }

    /**
     * Whether this role can manage roles of others.
     */
    public boolean canManageRoles() {
        return this == MANAGER;
    }
}
