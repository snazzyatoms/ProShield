// src/main/java/com/snazzyatoms/proshield/roles/ClaimRole.java
package com.snazzyatoms.proshield.roles;

import java.util.Locale;

/**
 * Defines claim roles with permissions and hierarchy.
 * Consolidated and polished (v1.2.0 → v1.2.5).
 */
public enum ClaimRole {

    NONE(false, false, false),
    VISITOR(false, false, false),
    MEMBER(true, false, false),
    TRUSTED(true, true, false),
    BUILDER(true, true, true),
    CONTAINER(true, true, false),
    MODERATOR(true, true, true),
    MANAGER(true, true, true),
    OWNER(true, true, true);

    private final boolean canInteract;
    private final boolean canBuild;
    private final boolean canManage;

    ClaimRole(boolean canInteract, boolean canBuild, boolean canManage) {
        this.canInteract = canInteract;
        this.canBuild = canBuild;
        this.canManage = canManage;
    }

    public boolean canInteract() {
        return canInteract;
    }

    public boolean canBuild() {
        return canBuild;
    }

    public boolean canManage() {
        return canManage;
    }

    /**
     * Resolve role by name (case-insensitive).
     */
    public static ClaimRole fromName(String name) {
        if (name == null) return NONE;
        try {
            return ClaimRole.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return NONE;
        }
    }

    /**
     * Get display-friendly name.
     */
    public String getDisplayName() {
        return name().charAt(0) + name().substring(1).toLowerCase(Locale.ROOT);
    }
}
