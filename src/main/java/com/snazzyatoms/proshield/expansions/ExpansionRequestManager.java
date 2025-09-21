// src/main/java/com/snazzyatoms/proshield/expansions/ExpansionRequestManager.java
package com.snazzyatoms.proshield.expansions;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ExpansionRequestManager (ProShield v1.2.6-polished)
 *
 * Handles:
 *  - Creating new expansion requests
 *  - Approving / denying requests with reasons
 *  - Browsing history and pending lists
 *  - Persistent storage in expansions.yml (survives crash/restore)
 *  - Expiry system with daily cleanup task
 *  - Added compatibility shims for GUIManager 1.2.6
 */
public class ExpansionRequestManager {

    private final ProShield plugin;

    /** Requests indexed by requester UUID */
    private final Map<UUID, List<ExpansionRequest>> requests = new ConcurrentHashMap<>();
    /** Requests indexed by requestId */
    private final Map<UUID, ExpansionRequest> byId = new ConcurrentHashMap<>();

    private File storeFile;
    private YamlConfiguration store;

    /** Expiry duration (7 days default, configurable later) */
    private static final Duration EXPIRY_DURATION = Duration.ofDays(7);

    public ExpansionRequestManager(ProShield plugin) {
        this.plugin = plugin;
        initStore();
        load();
        scheduleExpirySweep();
    }

    /* -------------------
     * Init & Store
     * ------------------- */

    private void initStore() {
        File data = plugin.getDataFolder();
        if (!data.exists()) data.mkdirs();

        storeFile = new File(data, "expansions.yml");
        if (!storeFile.exists()) {
            try {
                storeFile.createNewFile();
            } catch (IOException ex) {
                plugin.getLogger().warning("[ProShield] Failed to create expansions.yml: " + ex.getMessage());
            }
        }
        store = YamlConfiguration.loadConfiguration(storeFile);
    }

    /* -------------------
     * Request Lifecycle
     * ------------------- */

    /** Create a new request (default status PENDING). */
    public ExpansionRequest createRequest(UUID requester, int amount) {
        ExpansionRequest req = new ExpansionRequest(
                UUID.randomUUID(),
                requester,
                amount,
                ExpansionRequest.Status.PENDING,
                null,
                null,
                null,
                Instant.now()
        );
        requests.computeIfAbsent(requester, k -> new ArrayList<>()).add(req);
        byId.put(req.getId(), req);
        save();
        return req;
    }

    public List<ExpansionRequest> getRequests(UUID requester) {
        return new ArrayList<>(requests.getOrDefault(requester, Collections.emptyList()));
    }

    public List<ExpansionRequest> getAllRequests() {
        return byId.values().stream()
                .sorted(Comparator.comparing(ExpansionRequest::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<ExpansionRequest> getAllRequestsFor(UUID requester) {
        return getRequests(requester).stream()
                .sorted(Comparator.comparing(ExpansionRequest::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public ExpansionRequest getLatestFor(UUID requester) {
        return getRequests(requester).stream()
                .max(Comparator.comparing(ExpansionRequest::getCreatedAt))
                .orElse(null);
    }

    public List<ExpansionRequest> getPendingRequests() {
        return getAllRequests().stream()
                .filter(ExpansionRequest::isPending)
                .collect(Collectors.toList());
    }

    public List<ExpansionRequest> getPendingRequestsFor(UUID requester) {
        return getRequests(requester).stream()
                .filter(ExpansionRequest::isPending)
                .collect(Collectors.toList());
    }

    public List<ExpansionRequest> getHistoryFor(UUID requester) {
        return getRequests(requester).stream()
                .filter(r -> !r.isPending())
                .sorted(Comparator.comparing(ExpansionRequest::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public ExpansionRequest getById(UUID id) {
        return byId.get(id);
    }

    public ExpansionRequest findById(UUID id) {
        return getById(id);
    }

    /* -------------------
     * Approve / Deny
     * ------------------- */

    public void approveRequest(ExpansionRequest req, UUID reviewer) {
        if (req == null) return;
        req.setStatus(ExpansionRequest.Status.APPROVED);
        req.setReviewedBy(reviewer);
        req.setReviewedAt(Instant.now());
        req.setDenialReason(null);
        save();
        plugin.getLogger().info("[ProShield] Approved expansion request from "
                + req.getRequester() + " for +" + req.getAmount());
    }

    public void denyRequest(ExpansionRequest req, UUID reviewer, String reason) {
        if (req == null) return;
        req.setStatus(ExpansionRequest.Status.DENIED);
        req.setReviewedBy(reviewer);
        req.setReviewedAt(Instant.now());
        req.setDenialReason(reason);
        save();
        plugin.getLogger().info("[ProShield] Denied expansion request from "
                + req.getRequester() + " (Reason: " + reason + ")");
    }

    /* -------------------
     * Expiry System
     * ------------------- */

    private void scheduleExpirySweep() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::sweepExpiredRequests,
                20L * 60 * 60 * 24,   // every 24h
                20L * 60 * 60 * 24);
    }

    private synchronized void sweepExpiredRequests() {
        Instant now = Instant.now();
        int expiredCount = 0;

        for (ExpansionRequest req : getAllRequests()) {
            if (req.isPending() && Duration.between(req.getCreatedAt(), now).compareTo(EXPIRY_DURATION) > 0) {
                req.setStatus(ExpansionRequest.Status.EXPIRED);
                req.setReviewedAt(now);
                req.setDenialReason("Expired (no action taken)");
                expiredCount++;
            }
        }

        if (expiredCount > 0) {
            save();
            plugin.getLogger().info("[ProShield] Expired " + expiredCount + " old expansion requests.");
        }
    }

    /* -------------------
     * Utilities
     * ------------------- */

    public OfflinePlayer getOfflinePlayer(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public void reload() {
        initStore();
        load();
    }

    public void clear() {
        requests.clear();
        byId.clear();
    }

    /* -------------------
     * Persistence
     * ------------------- */

    private void load() {
        clear();
        if (store == null) initStore();

        ConfigurationSection root = store.getConfigurationSection("requests");
        if (root == null) return;

        for (String idStr : root.getKeys(false)) {
            try {
                UUID id = UUID.fromString(idStr);
                ConfigurationSection sec = root.getConfigurationSection(idStr);
                if (sec == null) continue;

                UUID requester = UUID.fromString(Objects.requireNonNull(sec.getString("requester")));
                int amount = sec.getInt("amount", 0);
                ExpansionRequest.Status status = ExpansionRequest.Status.valueOf(
                        sec.getString("status", "PENDING"));

                UUID reviewedBy = null;
                if (sec.isString("reviewedBy")) {
                    String rb = sec.getString("reviewedBy");
                    if (rb != null && !rb.isBlank()) reviewedBy = UUID.fromString(rb);
                }

                String denialReason = sec.getString("denialReason", null);

                Instant createdAt = Instant.ofEpochMilli(sec.getLong("createdAt", System.currentTimeMillis()));
                Instant reviewedAt = sec.isLong("reviewedAt")
                        ? Instant.ofEpochMilli(sec.getLong("reviewedAt"))
                        : null;

                ExpansionRequest req = new ExpansionRequest(id, requester, amount, status,
                        reviewedBy, reviewedAt, denialReason, createdAt);

                requests.computeIfAbsent(requester, k -> new ArrayList<>()).add(req);
                byId.put(id, req);
            } catch (Exception ex) {
                plugin.getLogger().warning("[ProShield] Failed to parse expansion request: " + ex.getMessage());
            }
        }
    }

    public synchronized void save() {
        if (store == null) initStore();
        store.set("requests", null);

        for (ExpansionRequest req : getAllRequests()) {
            String base = "requests." + req.getId();
            store.set(base + ".requester", req.getRequester().toString());
            store.set(base + ".amount", req.getAmount());
            store.set(base + ".status", req.getStatus().name());
            store.set(base + ".createdAt", req.getCreatedAt() != null ? req.getCreatedAt().toEpochMilli() : System.currentTimeMillis());
            store.set(base + ".reviewedAt", req.getReviewedAt() != null ? req.getReviewedAt().toEpochMilli() : null);
            store.set(base + ".reviewedBy", req.getReviewedBy() != null ? req.getReviewedBy().toString() : null);
            store.set(base + ".denialReason", req.getDenialReason());
        }

        try {
            store.save(storeFile);
        } catch (IOException e) {
            plugin.getLogger().warning("[ProShield] Failed to save expansions.yml: " + e.getMessage());
        }
    }

    /* -------------------
     * Compatibility Shims (for GUIManager 1.2.6)
     * ------------------- */

    /** GUI expects createRequest(UUID, UUID, int, String) */
    public ExpansionRequest createRequest(UUID requester, UUID reviewer, int amount, String reason) {
        ExpansionRequest req = createRequest(requester, amount);
        if (reason != null && !reason.isBlank()) {
            req.setDenialReason(reason); // optional metadata
        }
        if (reviewer != null) {
            req.setReviewedBy(reviewer);
        }
        return req;
    }

    /** GUI expects approve(String id, UUID reviewer, String note) */
    public void approve(String id, UUID reviewer, String note) {
        try {
            UUID reqId = UUID.fromString(id);
            ExpansionRequest req = byId.get(reqId);
            if (req != null) {
                approveRequest(req, reviewer);
                if (note != null && !note.isBlank()) {
                    req.setDenialReason(note); // treat as moderator note
                }
                save();
            }
        } catch (IllegalArgumentException ignored) {}
    }

    /** GUI expects deny(String id, UUID reviewer, String reason) */
    public void deny(String id, UUID reviewer, String reason) {
        try {
            UUID reqId = UUID.fromString(id);
            ExpansionRequest req = byId.get(reqId);
            if (req != null) {
                denyRequest(req, reviewer, reason);
            }
        } catch (IllegalArgumentException ignored) {}
    }
}
