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

    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("canBuild", canBuild);
        m.put("canContainers", canContainers);
        m.put("canManageTrust", canManageTrust);
        m.put("canUnclaim", canUnclaim);
        return m;
    }

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
        }
        return p;
    }

    public static RolePermissions fromMap(Map<String, Object> m) {
        RolePermissions p = new RolePermissions();
        if (m == null) return p;
        if (m.containsKey("canBuild")) p.setCanBuild((boolean)m.get("canBuild"));
        if (m.containsKey("canContainers")) p.setCanContainers((boolean)m.get("canContainers"));
        if (m.containsKey("canManageTrust")) p.setCanManageTrust((boolean)m.get("canManageTrust"));
        if (m.containsKey("canUnclaim")) p.setCanUnclaim((boolean)m.get("canUnclaim"));
        return p;
    }
}
