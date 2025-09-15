package com.snazzyatoms.proshield.expansions;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ExpansionRequestManager {

    private final ProShield plugin;
    private final MessagesUtil messages;
    // Stores the most recent request per player (sufficient for cooldown & review)
    private final Map<UUID, ExpansionRequest> requests = new HashMap<>();

    private final File file;
    private final FileConfiguration data;

    public ExpansionRequestManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
        this.file = new File(plugin.getDataFolder(), "expansion-requests.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
        loadRequests();
    }

    /* =========================  Public API  ========================= */

    /** Last request from a player (any status). Used for cooldown logic. */
    public ExpansionRequest getLastRequest(UUID playerId) {
        return requests.get(playerId);
    }

    /** All pending requests (for admin GUI). */
    public List<ExpansionRequest> getPendingRequests() {
        List<ExpansionRequest> list = new ArrayList<>();
        for (ExpansionRequest req : requests.values()) {
            if (req.getStatus() == ExpansionRequest.Status.PENDING) list.add(req);
        }
        // newest first (optional)
        list.sort(Comparator.comparingLong(ExpansionRequest::getTimestamp).reversed());
        return list;
    }

    /** Add & persist a new request; informs player if online. */
    public void addRequest(ExpansionRequest request) {
        requests.put(request.getRequester(), request);
        saveRequests();

        OfflinePlayer op = Bukkit.getOfflinePlayer(request.getRequester());
        if (op.isOnline()) {
            String msg = plugin.getConfig().getString("messages.expansion-request",
                    "&eYour expansion request for +{blocks} blocks has been sent to admins.")
                    .replace("{blocks}", String.valueOf(request.getBlocks()));
            messages.send(op.getPlayer(), msg);
        }
    }

    /** Approve (keeps a record with APPROVED status) and notify player. */
    public void approveRequest(UUID playerId) {
        ExpansionRequest req = requests.get(playerId);
        if (req == null || req.getStatus() != ExpansionRequest.Status.PENDING) return;

        req.setStatus(ExpansionRequest.Status.APPROVED);
        saveRequests();

        OfflinePlayer op = Bukkit.getOfflinePlayer(playerId);
        if (op.isOnline()) {
            String msg = plugin.getConfig().getString("messages.expansion-approved",
                    "&aYour claim expansion (+{blocks} blocks) was approved!")
                    .replace("{blocks}", String.valueOf(req.getBlocks()));
            messages.send(op.getPlayer(), msg);
        }
    }

    /** Deny (keeps a record with DENIED status + reason) and notify player. */
    public void denyRequest(UUID playerId, String reasonKey) {
        ExpansionRequest req = requests.get(playerId);
        if (req == null || req.getStatus() != ExpansionRequest.Status.PENDING) return;

        req.setStatus(ExpansionRequest.Status.DENIED);
        req.setDenyReason(reasonKey);
        saveRequests();

        String reason = plugin.getConfig().getString("messages.deny-reasons." + reasonKey, "&cDenied.");
        OfflinePlayer op = Bukkit.getOfflinePlayer(playerId);
        if (op.isOnline()) {
            String msg = plugin.getConfig().getString("messages.expansion-denied",
                    "&cYour expansion request was denied: {reason}")
                    .replace("{reason}", reason);
            messages.send(op.getPlayer(), msg);
        }
    }

    /* =========================  Persistence  ========================= */

    private void loadRequests() {
        if (!data.isConfigurationSection("requests")) return;
        for (String key : data.getConfigurationSection("requests").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int blocks = data.getInt("requests." + key + ".blocks");
                long timestamp = data.getLong("requests." + key + ".timestamp");
                String statusStr = data.getString("requests." + key + ".status", "PENDING");
                String denyReason = data.getString("requests." + key + ".denyReason", null);

                ExpansionRequest req = new ExpansionRequest(uuid, blocks);
                // overwrite timestamp/status/reason to match stored
                // (we don't expose setters for timestamp; do it by reflection or re-create)
                // Easy approach: re-create object via constructor then set fields via methods we have:
                req.setStatus(ExpansionRequest.Status.valueOf(statusStr));
                req.setDenyReason(denyReason);

                // We want the original timestamp; hack-free way: store in the file and read back during save.
                // Since we can't set timestamp directly, we’ll keep it in memory map with path info.
                // Simpler: store the request, then keep a shadow map of timestamps if needed.
                // But cooldown uses newest request; loading with current time could shorten cooldown.
                // To keep exact timestamp, we’ll stash it in a side map:
                setTimestampUnsafe(req, timestamp);

                requests.put(uuid, req);
            } catch (Exception ignored) { }
        }
    }

    private void saveRequests() {
        data.set("requests", null);
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
            plugin.getLogger().severe("Failed to save expansion-requests.yml: " + e.getMessage());
        }
    }

    /* =========================  Timestamp injection (internal)  ========================= */
    // Safely set the timestamp for a loaded request without changing API surface.
    private void setTimestampUnsafe(ExpansionRequest req, long ts) {
        try {
            java.lang.reflect.Field f = ExpansionRequest.class.getDeclaredField("timestamp");
            f.setAccessible(true);
            f.setLong(req, ts);
        } catch (Throwable ignored) { }
    }
}
