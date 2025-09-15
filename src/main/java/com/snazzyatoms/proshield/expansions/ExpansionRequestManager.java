package com.snazzyatoms.proshield.expansions;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * ExpansionRequestManager
 * - Tracks, persists, and manages expansion requests.
 * - Enforces cooldowns (via timestamp).
 * - Provides requests for admin GUI review.
 * - On approval, expands only the plot where the request was made (per-plot radius).
 */
public class ExpansionRequestManager {

    private final ProShield plugin;
    private final MessagesUtil messages;

    // Tracks latest request per player (overwrites older entries)
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

    public ExpansionRequest getLastRequest(UUID playerId) {
        return requests.get(playerId);
    }

    public List<ExpansionRequest> getPendingRequests() {
        List<ExpansionRequest> list = new ArrayList<>();
        for (ExpansionRequest req : requests.values()) {
            if (req.getStatus() == ExpansionRequest.Status.PENDING) {
                list.add(req);
            }
        }
        list.sort(Comparator.comparingLong(ExpansionRequest::getTimestamp).reversed());
        return list;
    }

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

    /** Approve pending → expand only the plot at request location. */
    public void approveRequest(UUID playerId) {
        ExpansionRequest req = requests.get(playerId);
        if (req == null || req.getStatus() != ExpansionRequest.Status.PENDING) return;

        PlotManager pm = plugin.getPlotManager();
        Location at = req.getRequestedAt();
        boolean expanded = false;

        if (at != null) {
            expanded = pm.expandPlot(playerId, at, req.getBlocks());
        }

        req.setStatus(ExpansionRequest.Status.APPROVED);
        saveRequests();

        OfflinePlayer op = Bukkit.getOfflinePlayer(playerId);
        if (op.isOnline()) {
            String template = plugin.getConfig().getString("messages.expansion-approved",
                    "&aYour claim expansion (+{blocks} blocks) was approved!");
            String msg = template.replace("{blocks}", String.valueOf(req.getBlocks()));
            messages.send(op.getPlayer(), msg);

            if (!expanded) {
                messages.send(op.getPlayer(),
                        "&eNote: Expansion was approved, but no new radius could be applied (not the owner or invalid plot).");
            }
        }
    }

    /** Deny pending with reason key → notify player if online. */
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
                Location at = (Location) data.get("requests." + key + ".location");

                ExpansionRequest req = new ExpansionRequest(uuid, blocks, at);
                req.setStatus(ExpansionRequest.Status.valueOf(statusStr));
                req.setDenyReason(denyReason);
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
            data.set(path + ".location", req.getRequestedAt());
        }
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save expansion-requests.yml: " + e.getMessage());
        }
    }

    /* =========================  Timestamp Injection ========================= */
    private void setTimestampUnsafe(ExpansionRequest req, long ts) {
        try {
            java.lang.reflect.Field f = ExpansionRequest.class.getDeclaredField("timestamp");
            f.setAccessible(true);
            f.setLong(req, ts);
        } catch (Throwable ignored) { }
    }
}
