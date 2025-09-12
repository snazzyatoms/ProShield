package com.snazzyatoms.proshield.roles;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RolePermissions
 * Defines per-role permission toggles.
 */
public class RolePermissions {

    private boolean canBuild = false;
    private boolean canContainers = false;
    private boolean canManageTrust = false;
    private boolean canUnclaim = false;
    private boolean canInteract = false;

    public boolean canBuild() { return canBuild; }
    public boolean canContainers() { return canContainers; }
    public boolean canManageTrust() { return canManageTrust; }
    public boolean canUnclaim() { return canUnclaim; }
    public boolean canInteract() { return canInteract; }

    public void setCanBuild(boolean v) { this.canBuild = v; }
    public void setCanContainers(boolean v) { this.canContainers = v; }
    public void setCanManageTrust(boolean v) { this.canManageTrust = v; }
    public void setCanUnclaim(boolean v) { this.canUnclaim = v; }
    public void setCanInteract(boolean v) { this.canInteract = v; }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("canBuild", canBuild);
        m.put("canContainers", canContainers);
        m.put("canManageTrust", canManageTrust);
        m.put("canUnclaim", canUnclaim);
        m.put("canInteract", canInteract);
        return m;
    }

    public static RolePermissions defaultsFor(String role) {
        RolePermissions p = new RolePermissions();
        switch (role.toLowerCase()) {
            case "visitor" -> {
                p.setCanInteract(false);
            }
            case "member" -> {
                p.setCanInteract(true);
            }
            case "trusted" -> {
                p.setCanInteract(true);
            }
            case "builder" -> {
                p.setCanBuild(true);
                p.setCanContainers(true);
                p.setCanInteract(true);
            }
            case "container" -> {
                p.setCanContainers(true);
                p.setCanInteract(true);
            }
            case "moderator" -> {
                p.setCanBuild(true);
                p.setCanContainers(true);
                p.setCanManageTrust(true);
                p.setCanInteract(true);
            }
            case "manager" -> {
                p.setCanBuild(true);
                p.setCanContainers(true);
                p.setCanManageTrust(true);
                p.setCanUnclaim(true);
                p.setCanInteract(true);
            }
            case "owner" -> {
                p.setCanBuild(true);
                p.setCanContainers(true);
                p.setCanManageTrust(true);
                p.setCanUnclaim(true);
                p.setCanInteract(true);
            }
            default -> {
                // NONE or unknown roles → no permissions
            }
        }
        return p;
    }

    public static RolePermissions fromMap(Map<String, Object> m) {
        RolePermissions p = new RolePermissions();
        if (m == null) return p;
        if (m.containsKey("canBuild")) p.setCanBuild((boolean) m.get("canBuild"));
        if (m.containsKey("canContainers")) p.setCanContainers((boolean) m.get("canContainers"));
        if (m.containsKey("canManageTrust")) p.setCanManageTrust((boolean) m.get("canManageTrust"));
        if (m.containsKey("canUnclaim")) p.setCanUnclaim((boolean) m.get("canUnclaim"));
        if (m.containsKey("canInteract")) p.setCanInteract((boolean) m.get("canInteract"));
        return p;
    }
}
