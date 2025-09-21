package com.snazzyatoms.proshield.expansions;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ExpansionRequestManager (ProShield v1.2.6)
 *
 * Handles:
 *  - Creating new expansion requests
 *  - Approving / denying requests with reasons
 *  - Auto-expiring requests after configurable days
 *  - Browsing history and pending lists
 *  - Persistent storage in expansions.yml (survives crash/restore)
 */
public class ExpansionRequestManager {

    private final ProShield plugin;

    /** Requests indexed by requester UUID */
    private final Map<UUID, List<ExpansionRequest>> requests = new ConcurrentHashMap<>();
    /** Requests indexed by requestId */
    private final Map<UUID, ExpansionRequest> byId = new ConcurrentHashMap<>();

    private File storeFile;
    private YamlConfiguration store;

    public ExpansionRequestManager(ProShield plugin) {
        this.plugin = plugin;
        initStore();
        load(); // auto-load from disk
        scheduleExpiryTask(); // daily expiry sweep
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

    /** Get all requests from one player. */
    public List<ExpansionRequest> getRequests(UUID requester) {
        return new ArrayList<>(requests.getOrDefault(requester, Collections.emptyList()));
    }

    /** Get all requests globally. */
    public List<ExpansionRequest> getAllRequests() {
        return byId.values().stream()
                .sorted(Comparator.comparing(ExpansionRequest::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /** Get only pending requests globally. */
    public List<ExpansionRequest> getPendingRequests() {
        return getAllRequests().stream()
                .filter(r -> r.getStatus() == ExpansionRequest.Status.PENDING)
                .collect(Collectors.toList());
    }

    /** Get only pending requests for one player. */
    public List<ExpansionRequest> getPendingRequestsFor(UUID requester) {
        return getRequests(requester).stream()
                .filter(r -> r.getStatus() == ExpansionRequest.Status.PENDING)
                .collect(Collectors.toList());
    }

    /** Get non-pending history for one player. */
    public List<ExpansionRequest> getHistoryFor(UUID requester) {
        return getRequests(requester).stream()
                .filter(r -> r.getStatus() != ExpansionRequest.Status.PENDING)
                .sorted(Comparator.comparing(ExpansionRequest::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public ExpansionRequest getById(UUID id) {
        return byId.get(id);
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

        // notify if online
        Player p = Bukkit.getPlayer(req.getRequester());
        if (p != null && p.isOnline()) {
            MessagesUtil mu = plugin.getMessagesUtil();
            mu.send(p, mu.getOrDefault("messages.expansion.approved",
                    "&aYour claim expansion (+{blocks} blocks) was approved!")
                    .replace("{blocks}", String.valueOf(req.getAmount())));
        }

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

        // notify if online
        Player p = Bukkit.getPlayer(req.getRequester());
        if (p != null && p.isOnline()) {
            MessagesUtil mu = plugin.getMessagesUtil();
            mu.send(p, mu.getOrDefault("messages.expansion.denied",
                    "&cYour expansion request was denied: {reason}")
                    .replace("{reason}", reason));
        }

        plugin.getLogger().info("[ProShield] Denied expansion request from "
                + req.getRequester() + " (Reason: " + reason + ")");
    }

    /* -------------------
     * Expiry System
     * ------------------- */

    /** Run once per day to expire old requests. */
    public void scheduleExpiryTask() {
        long ticksPerDay = 20L * 60L * 60L * 24L;
        Bukkit.getScheduler().runTaskTimer(plugin, this::runExpirySweep, ticksPerDay, ticksPerDay);
    }

    /** Expire pending requests older than expire-days. */
    public void runExpirySweep() {
        int expireDays = plugin.getConfig().getInt("claims.expansion.expire-days", 30);
        Instant cutoff = Instant.now().minusSeconds(expireDays * 86400L);

        for (ExpansionRequest req : getPendingRequests()) {
            if (req.getCreatedAt().isBefore(cutoff)) {
                req.setStatus(ExpansionRequest.Status.EXPIRED);
                req.setReviewedAt(Instant.now());
                save();

                // notify requester if online
                Player p = Bukkit.getPlayer(req.getRequester());
                if (p != null && p.isOnline()) {
                    MessagesUtil mu = plugin.getMessagesUtil();
                    mu.send(p, mu.getOrDefault("messages.expansion.expired",
                            "&eYour expansion request has expired after {days} days without review.")
                            .replace("{days}", String.valueOf(expireDays)));
                }

                plugin.getLogger().info("[ProShield] Expired expansion request from "
                        + req.getRequester() + " (+" + req.getAmount() + ")");
            }
        }
    }

    /* -------------------
     * Utilities
     * ------------------- */

    public OfflinePlayer getOfflinePlayer(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }

    /** Reload from disk (overwrites in-memory). */
    public void reload() {
        initStore();
        load();
    }

    /** Clear in-memory (not persisted). */
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
        store.set("requests", null); // wipe old

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
}
