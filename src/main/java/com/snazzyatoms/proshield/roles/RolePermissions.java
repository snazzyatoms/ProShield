package com.snazzyatoms.proshield.roles;

import java.util.LinkedHashMap;
import java.util.Map;

public class RolePermissions {

    private boolean canBuild = true;
    private boolean canContainers = false;
    private boolean canManageTrust = false;
    private boolean canUnclaim = false;

    public boolean canBuild() { return canBuild; }
    public boolean canContainers() { return canContainers; }
    public boolean canManageTrust() { return canManageTrust; }
    public boolean canUnclaim() { return canUnclaim; }

    public void setCanBuild(boolean v) { this.canBuild = v; }
    public void setCanContainers(boolean v) { this.canContainers = v; }
    public void setCanManageTrust(boolean v) { this.canManageTrust = v; }
    public void setCanUnclaim(boolean v) { this.canUnclaim = v; }

    // Serialize for YAML
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("canBuild", canBuild);
        m.put("canContainers", canContainers);
        m.put("canManageTrust", canManageTrust);
        m.put("canUnclaim", canUnclaim);
        return m;
    }

    // Defaults for a named role (owner adjustable later in GUI)
    public static RolePermissions defaultsFor(String role) {
        RolePermissions p = new RolePermissions();
        switch (role.toLowerCase()) {
            case "builder" -> {
                p.setCanBuild(true);
                p.setCanContainers(true);
                p.setCanManageTrust(false);
                p.setCanUnclaim(false);
            }
            case "moderator" -> {
                p.setCanBuild(true);
                p.setCanContainers(true);
                p.setCanManageTrust(true);
                p.setCanUnclaim(false);
            }
            default -> { /* trusted baseline already set */ }
        }
        return p;
    }

    public static RolePermissions fromMap(Map<String, Object> m) {
        RolePermissions p = new RolePermissions();
        if (m == null) return p;
        Object a;
        if ((a = m.get("canBuild")) != null) p.setCanBuild((boolean)a);
        if ((a = m.get("canContainers")) != null) p.setCanContainers((boolean)a);
        if ((a = m.get("canManageTrust")) != null) p.setCanManageTrust((boolean)a);
        if ((a = m.get("canUnclaim")) != null) p.setCanUnclaim((boolean)a);
        return p;
    }
}
