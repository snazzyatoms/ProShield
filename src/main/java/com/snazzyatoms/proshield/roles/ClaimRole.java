// src/main/java/com/snazzyatoms/proshield/roles/ClaimRole.java
package com.snazzyatoms.proshield.roles;

/**
 * ClaimRole
 *
 * Defines the available roles inside a ProShield claim.
 * Each role has increasing permissions.
 *
 * Order is important → progression is from lowest (VISITOR) to highest (OWNER).
 */
public enum ClaimRole {
    VISITOR("Visitor", "§7Minimal access, can only enter claim."),
    MEMBER("Member", "§aBasic access to walk and interact."),
    TRUSTED("Trusted", "§bCan interact with doors, buttons, etc."),
    BUILDER("Builder", "§2Can place and break blocks."),
    CONTAINER("Container", "§6Can open chests, furnaces, hoppers."),
    MODERATOR("Moderator", "§cCan manage PvP, entities, containers."),
    MANAGER("Manager", "§eFull management rights except transfer."),
    OWNER("Owner", "§dClaim owner, full permissions.");

    private final String displayName;
    private final String description;

    ClaimRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get the next higher role in progression.
     */
    public ClaimRole next() {
        int index = this.ordinal();
        ClaimRole[] values = ClaimRole.values();
        if (index + 1 < values.length) {
            return values[index + 1];
        }
        return this; // already at highest
    }

    /**
     * Get the previous lower role in progression.
     */
    public ClaimRole previous() {
        int index = this.ordinal();
        if (index - 1 >= 0) {
            return ClaimRole.values()[index - 1];
        }
        return this; // already at lowest
    }

    /**
     * Look up a ClaimRole by name (case-insensitive).
     */
    public static ClaimRole fromString(String name) {
        for (ClaimRole role : ClaimRole.values()) {
            if (role.name().equalsIgnoreCase(name)) {
                return role;
            }
        }
        return null;
    }
}
