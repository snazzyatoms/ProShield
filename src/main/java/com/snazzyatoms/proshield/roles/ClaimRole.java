// src/main/java/com/snazzyatoms/proshield/roles/ClaimRole.java
package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;

import java.util.*;

/**
 * ClaimRole
 * - Defines claim roles, hierarchy, and permissions
 * - v1.2.6: Display names + lore now check messages.yml first
 * - v1.2.6-polished: Added compatibility shims for GUIManager calls
 * - v1.2.6-enhanced: Supports multi-line lore + aliases
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

    /* -------------------
     * Permission Checks
     * ------------------- */
    public boolean canInteract() { return canInteract; }
    public boolean canBuild() { return canBuild; }
    public boolean canOpenContainers() { return canOpenContainers; }
    public boolean canModifyFlags() { return canModifyFlags; }
    public boolean canManageRoles() { return canManageRoles; }
    public boolean canTransferClaim() { return canTransferClaim; }

    public int getRank() { return rank; }

    /* -------------------
     * Role Logic
     * ------------------- */

    /** Compare hierarchy (this >= other). */
    public boolean isAtLeast(ClaimRole other) {
        return this.rank >= other.rank;
    }

    // Aliases for convenience (config, commands, etc.)
    private static final Map<String, ClaimRole> ALIASES = Map.ofEntries(
        Map.entry("visitor", VISITOR),
        Map.entry("member", MEMBER),
        Map.entry("trusted", TRUSTED),
        Map.entry("builder", BUILDER),
        Map.entry("container", CONTAINER),
        Map.entry("mod", MODERATOR),
        Map.entry("moderator", MODERATOR),
        Map.entry("mgr", MANAGER),
        Map.entry("manager", MANAGER),
        Map.entry("admin", MANAGER),
        Map.entry("owner", OWNER)
    );

    /** Resolve role by name or alias (case-insensitive). */
    public static ClaimRole fromName(String name) {
        if (name == null || name.isBlank()) return NONE;
        String key = name.trim().toLowerCase(Locale.ROOT);
        if (ALIASES.containsKey(key)) return ALIASES.get(key);
        try {
            return ClaimRole.valueOf(key.toUpperCase(Locale.ROOT));
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

        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[ProShield] No display name found for role: "
                    + this.name() + " (using fallback)");
        }

        String raw = name().toLowerCase(Locale.ROOT).replace("_", " ");
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }

    /* -------------------
     * Compatibility Shims (for GUIManager 1.2.6)
     * ------------------- */

    /** GUI expects getId() */
    public String getId() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    /** GUI expects getDescription() (single-line fallback) */
    public String getDescription() {
        ProShield plugin = ProShield.getInstance();
        String key = "messages.roles.lore." + this.name().toLowerCase(Locale.ROOT);
        String custom = plugin.getMessagesUtil().getOrNull(key);
        if (custom != null && !custom.isBlank()) {
            return plugin.getMessagesUtil().color(custom);
        }
        return "Role: " + getDisplayName();
    }

    /** Enhanced: Multi-line lore for GUIs */
    public List<String> getLore() {
        ProShield plugin = ProShield.getInstance();
        String key = "messages.roles.lore." + this.name().toLowerCase(Locale.ROOT);
        List<String> list = plugin.getMessagesUtil().getListOrNull(key);
        if (list != null && !list.isEmpty()) {
            return plugin.getMessagesUtil().color(list);
        }
        return List.of("Role: " + getDisplayName());
    }

    /** GUI expects canContainers() */
    public boolean canContainers() { return canOpenContainers; }

    /** GUI expects canSwitches() */
    public boolean canSwitches() { return canInteract; }

    /** GUI expects canDamageMobs() */
    public boolean canDamageMobs() { return this.rank >= MODERATOR.rank; }

    /** GUI expects canPlaceFluids() */
    public boolean canPlaceFluids() { return this.rank >= TRUSTED.rank; }
}
