// path: src/main/java/com/snazzyatoms/proshield/plots/ClaimRoleManager.java
package com.snazzyatoms.proshield.plots;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal roles manager for claims.
 * Maps: ownerUUID -> (memberUUID -> roleName)
 */
public class ClaimRoleManager {

    private final Map<UUID, Map<UUID, String>> roles = new ConcurrentHashMap<>();

    /**
     * Returns the role name for 'member' within 'owner' claims, or "trusted" if none set.
     */
    public String getRoleName(UUID owner, UUID member) {
        Map<UUID, String> m = roles.get(owner);
        if (m == null) return "trusted";
        return m.getOrDefault(member, "trusted");
    }

    /** Assign/overwrite a role for a member within an owner's claims. */
    public void setRole(UUID owner, UUID member, String roleName) {
        roles.computeIfAbsent(owner, k -> new ConcurrentHashMap<>()).put(member, roleName);
    }

    /** Remove a role entry. */
    public void clearRole(UUID owner, UUID member) {
        Map<UUID, String> m = roles.get(owner);
        if (m != null) {
            m.remove(member);
            if (m.isEmpty()) roles.remove(owner);
        }
    }

    /** Read-only snapshot of a member->role map for an owner. */
    public Map<UUID, String> getRolesForOwner(UUID owner) {
        Map<UUID, String> m = roles.get(owner);
        return (m == null) ? Collections.emptyMap() : Collections.unmodifiableMap(m);
    }
}
