package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import java.util.Locale;

/**
 * ClaimRole
 * - Defines claim roles, hierarchy, and permissions
 * - v1.2.6: Display names now check messages.yml first
 */
public enum ClaimRole {

    NONE(0, false, false, false, false, false, false),
    VISITOR(1, true, false, false, false, false, false),
    MEMBER(2, true, true, false, false, false, false),
    TRUSTED(3, true, true, true, true, false, false),
    BUILDER(4, true, true, true, false, false, false),
    CONTAINER(4, true, false, true, false, false, false),
    MODERATOR(5, true, true, true, true, true, false),
    MANAGER(6, true, true, true, true, true, true),
    OWNER(7, true, true, true, true, true, true);

    private final int rank;
    private final boolean canInteract;
    private final boolean canBuild;
    private final boolean canOpenContainers;
    private final boolean canModifyFlags;
    private final boolean canManageRoles;
    private final boolean canTransferClaim;

    ClaimRole(int rank, boolean canInteract, boolean canBuild,
              boolean canOpenContainers, boolean canModifyFlags,
              boolean canManageRoles, boolean canTransferClaim) {
        this.rank = rank;
        this.canInteract = canInteract;
        this.canBuild = canBuild;
        this.canOpenContainers = canOpenContainers;
        this.canModifyFlags = canModifyFlags;
        this.canManageRoles = canManageRoles;
        this.canTransferClaim = canTransferClaim;
    }

    // Permissions
    public boolean canInteract() { return canInteract; }
    public boolean canBuild() { return canBuild; }
    public boolean canOpenContainers() { return canOpenContainers; }
    public boolean canModifyFlags() { return canModifyFlags; }
    public boolean canManageRoles() { return canManageRoles; }
    public boolean canTransferClaim() { return canTransferClaim; }

    public int getRank() { return rank; }

    // Compare hierarchy
    public boolean isAtLeast(ClaimRole other) {
        return this.rank >= other.rank;
    }

    // Resolve role by name
    public static ClaimRole fromName(String name) {
        if (name == null || name.isBlank()) return NONE;
        try {
            return ClaimRole.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return NONE;
        }
    }

    /**
     * Get display-friendly name.
     * Checks messages.yml (messages.roles.display.<role>) first.
     * Falls back to title-cased enum name if not found.
     */
    public String getDisplayName() {
        ProShield plugin = ProShield.getInstance();
        String key = "messages.roles.display." + this.name().toLowerCase(Locale.ROOT);
        String custom = plugin.getMessagesUtil().getOrNull(key);
        if (custom != null && !custom.isBlank()) {
            return plugin.getMessagesUtil().color(custom);
        }

        // Fallback: Title-case enum name
        String raw = name().toLowerCase(Locale.ROOT).replace("_", " ");
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }
}
