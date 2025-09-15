package com.snazzyatoms.proshield.expansions;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ExpansionRequestManager {

    private final ProShield plugin;
    private final Map<UUID, ExpansionRequest> requests = new HashMap<>();
    private final File file;
    private final FileConfiguration data;

    public ExpansionRequestManager(ProShield plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "expansion-requests.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
        loadRequests();
    }

    public void addRequest(ExpansionRequest request) {
        requests.put(request.getRequester(), request);
        saveRequests();
        Bukkit.getPlayer(request.getRequester()).sendMessage(
                plugin.getMessages().get("expansion-request")
                        .replace("{blocks}", String.valueOf(request.getBlocks()))
        );
    }

    public void approveRequest(UUID playerId) {
        ExpansionRequest req = requests.get(playerId);
        if (req != null && req.getStatus() == ExpansionRequest.Status.PENDING) {
            req.setStatus(ExpansionRequest.Status.APPROVED);
            saveRequests();
            Bukkit.getPlayer(playerId).sendMessage(
                    plugin.getMessages().get("expansion-approved")
                            .replace("{blocks}", String.valueOf(req.getBlocks()))
            );
        }
    }

    public void denyRequest(UUID playerId, String reasonKey) {
        ExpansionRequest req = requests.get(playerId);
        if (req != null && req.getStatus() == ExpansionRequest.Status.PENDING) {
            req.setStatus(ExpansionRequest.Status.DENIED);
            req.setDenyReason(reasonKey);
            saveRequests();
            String denyMsg = plugin.getMessages().get("deny-reasons." + reasonKey);
            if (denyMsg == null) denyMsg = "&cYour expansion request was denied.";
            Bukkit.getPlayer(playerId).sendMessage(
                    plugin.getMessages().get("expansion-denied")
                            .replace("{reason}", denyMsg)
            );
        }
    }

    public List<ExpansionRequest> getPendingRequests() {
        List<ExpansionRequest> list = new ArrayList<>();
        for (ExpansionRequest req : requests.values()) {
            if (req.getStatus() == ExpansionRequest.Status.PENDING) {
                list.add(req);
            }
        }
        return list;
    }

    private void loadRequests() {
        if (!data.isConfigurationSection("requests")) return;
        for (String key : data.getConfigurationSection("requests").getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            int blocks = data.getInt("requests." + key + ".blocks");
            long timestamp = data.getLong("requests." + key + ".timestamp");
            String statusStr = data.getString("requests." + key + ".status", "PENDING");
            String denyReason = data.getString("requests." + key + ".denyReason", null);

            ExpansionRequest req = new ExpansionRequest(uuid, blocks);
            req.setStatus(ExpansionRequest.Status.valueOf(statusStr));
            req.setDenyReason(denyReason);
            requests.put(uuid, req);
        }
    }

    private void saveRequests() {
        data.set("requests", null); // clear old
        for (Map.Entry<UUID, ExpansionRequest> entry : requests.entrySet()) {
            ExpansionRequest req = entry.getValue();
            String path = "requests." + entry.getKey();
            data.set(path + ".blocks", req.getBlocks());
            data.set(path + ".timestamp", req.getTimestamp());
            data.set(path + ".status", req.getStatus().name());
            data.set(path + ".denyReason", req.getDenyReason());
        }
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
