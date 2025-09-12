package com.snazzyatoms.proshield.roles;

import java.util.LinkedHashMap;
import java.util.Map;

public class RolePermissions {

    private boolean canBuild = true;
    private boolean canContainers = false;
    private boolean canManageTrust = false;
    private boolean canUnclaim = false;

    /* -------------------------------------------------------
     * Getters
     * ------------------------------------------------------- */
    public boolean canBuild() { return canBuild; }
    public boolean canContainers() { return canContainers; }
    public boolean canManageTrust() { return canManageTrust; }
    public boolean canUnclaim() { return canUnclaim; }

    /* -------------------------------------------------------
     * Setters
     * ------------------------------------------------------- */
    public void setCanBuild(boolean v) { this.canBuild = v; }
    public void setCanContainers(boolean v) { this.canContainers = v; }
    public void setCanManageTrust(boolean v) { this.canManageTrust = v; }
    public void setCanUnclaim(boolean v) { this.canUnclaim = v; }

    /* -------------------------------------------------------
     * Serialization
     * ------------------------------------------------------- */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("canBuild", canBuild);
        m.put("canContainers", canContainers);
        m.put("canManageTrust", canManageTrust);
        m.put("canUnclaim", canUnclaim);
        return m;
    }

    public static RolePermissions fromMap(Map<String, Object> m) {
        RolePermissions p = new RolePermissions();
        if (m == null) return p;
        if (m.get("canBuild") instanceof Boolean b1) p.setCanBuild(b1);
        if (m.get("canContainers") instanceof Boolean b2) p.setCanContainers(b2);
        if (m.get("canManageTrust") instanceof Boolean b3) p.setCanManageTrust(b3);
        if (m.get("canUnclaim") instanceof Boolean b4) p.setCanUnclaim(b4);
        return p;
    }

    /* -------------------------------------------------------
     * Defaults
     * ------------------------------------------------------- */
    public static RolePermissions defaultsFor(String role) {
        RolePermissions p = new RolePermissions();
        if (role == null) return p;

        switch (role.toLowerCase()) {
            case "owner" -> {
                p.setCanBuild(true);
                p.setCanContainers(true);
                p.setCanManageTrust(true);
                p.setCanUnclaim(true);
            }
            case "co_owner", "co-owner" -> {
                p.setCanBuild(true);
                p.setCanContainers(true);
                p.setCanManageTrust(true);
                p.setCanUnclaim(false); // optional: disallow unclaim by default
            }
            case "moderator" -> {
                p.setCanBuild(true);
                p.setCanContainers(true);
                p.setCanManageTrust(true);
                p.setCanUnclaim(false);
            }
            case "builder" -> {
                p.setCanBuild(true);
                p.setCanContainers(true);
                p.setCanManageTrust(false);
                p.setCanUnclaim(false);
            }
            case "trusted" -> {
                p.setCanBuild(false);
                p.setCanContainers(false);
                p.setCanManageTrust(false);
                p.setCanUnclaim(false);
            }
            default -> {
                // fallback to trusted defaults
                p.setCanBuild(false);
                p.setCanContainers(false);
                p.setCanManageTrust(false);
                p.setCanUnclaim(false);
            }
        }
        return p;
    }
}
