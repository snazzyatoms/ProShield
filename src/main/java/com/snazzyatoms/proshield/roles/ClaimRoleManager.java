package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClaimRoleManager {

    private final ProShield plugin;
    // claimId -> (playerUUID -> roleId)
    private final Map<UUID, Map<UUID, String>> roleAssignments = new HashMap<>();
    // claimId -> (roleId -> RolePermissions)
    private final Map<UUID, Map<String, RolePermissions>> rolePermissions = new HashMap<>();

    private File file;
    private FileConfiguration data;

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        file = new File(plugin.getDataFolder(), "roles.yml");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        data = YamlConfiguration.loadConfiguration(file);
        loadAll();
    }

    @SuppressWarnings("unchecked")
    private void loadAll() {
        roleAssignments.clear();
        rolePermissions.clear();

        if (!data.contains("claims")) return;
        for (String claimKey : data.getConfigurationSection("claims").getKeys(false)) {
            UUID claimId = UUID.fromString(claimKey);
            Map<UUID, String> assignMap = new HashMap<>();
            Map<String, RolePermissions> permMap = new HashMap<>();

            // assignments
            if (data.contains("claims." + claimKey + ".assignments")) {
                for (String pu : data.getConfigurationSection("claims." + claimKey + ".assignments").getKeys(false)) {
                    assignMap.put(UUID.fromString(pu), data.getString("claims." + claimKey + ".assignments." + pu, ""));
                }
            }
            // permissions
            if (data.contains("claims." + claimKey + ".roles")) {
                for (String role : data.getConfigurationSection("claims." + claimKey + ".roles").getKeys(false)) {
                    Map<String, Object> m = data.getConfigurationSection("claims." + claimKey + ".roles." + role).getValues(false);
                    permMap.put(role, RolePermissions.fromMap(m));
                }
            }
            roleAssignments.put(claimId, assignMap);
            rolePermissions.put(claimId, permMap);
        }
    }

    private void saveAll() {
        data.set("claims", null); // wipe
        for (Map.Entry<UUID, Map<UUID, String>> e : roleAssignments.entrySet()) {
            String claimKey = e.getKey().toString();
            // assignments
            for (Map.Entry<UUID, String> a : e.getValue().entrySet()) {
                data.set("claims." + claimKey + ".assignments." + a.getKey(), a.getValue() == null ? "" : a.getValue());
            }
            // role perms
            Map<String, RolePermissions> rp = rolePermissions.getOrDefault(e.getKey(), new HashMap<>());
            for (Map.Entry<String, RolePermissions> rpe : rp.entrySet()) {
                data.createSection("claims." + claimKey + ".roles." + rpe.getKey(), rpe.getValue().toMap());
            }
        }
        try { data.save(file); } catch (IOException ignored) {}
    }

    /* ===================================================
     * Public API used by GUIs/listeners
     * =================================================== */

    public synchronized void assignRole(UUID claimId, UUID playerId, String roleId) {
        Map<UUID, String> m = roleAssignments.computeIfAbsent(claimId, k -> new HashMap<>());
        if (roleId == null) roleId = "";
        m.put(playerId, roleId);
        // ensure role has a permission bucket
        rolePermissions.computeIfAbsent(claimId, k -> new HashMap<>())
                       .computeIfAbsent(roleId.isEmpty() ? "trusted" : roleId, RolePermissions::defaultsFor);
        saveAll();
    }

    public synchronized void clearRole(UUID claimId, UUID playerId) {
        Map<UUID, String> m = roleAssignments.computeIfAbsent(claimId, k -> new HashMap<>());
        m.put(playerId, "");
        saveAll();
    }

    public String getRole(UUID claimId, UUID playerId) {
        Map<UUID, String> m = roleAssignments.get(claimId);
        if (m == null) return "";
        return m.getOrDefault(playerId, "");
    }

    public RolePermissions getRolePermissions(UUID claimId, String roleId) {
        String key = (roleId == null || roleId.isEmpty()) ? "trusted" : roleId.toLowerCase();
        Map<String, RolePermissions> map = rolePermissions.computeIfAbsent(claimId, k -> new HashMap<>());
        return map.computeIfAbsent(key, RolePermissions::defaultsFor);
    }

    public synchronized void savePermissions(UUID claimId, String roleId, RolePermissions perms) {
        String key = (roleId == null || roleId.isEmpty()) ? "trusted" : roleId.toLowerCase();
        rolePermissions.computeIfAbsent(claimId, k -> new HashMap<>()).put(key, perms);
        saveAll();
    }

    /* ===== Permission checks used by protections ===== */

    public boolean canBuild(UUID claimId, UUID playerId) {
        String role = getRole(claimId, playerId);
        return getRolePermissions(claimId, role).canBuild();
    }

    public boolean canUseContainers(UUID claimId, UUID playerId) {
        String role = getRole(claimId, playerId);
        return getRolePermissions(claimId, role).canContainers();
    }

    public boolean canManageTrust(UUID claimId, UUID playerId) {
        String role = getRole(claimId, playerId);
        return getRolePermissions(claimId, role).canManageTrust();
    }

    public boolean canUnclaim(UUID claimId, UUID playerId) {
        String role = getRole(claimId, playerId);
        return getRolePermissions(claimId, role).canUnclaim();
    }
}
