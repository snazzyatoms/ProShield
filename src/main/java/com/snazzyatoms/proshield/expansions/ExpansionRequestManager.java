package com.snazzyatoms.proshield.expansions;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * ExpansionRequestManager
 * - Tracks, persists, and manages expansion requests.
 * - Enforces cooldowns (via timestamp).
 * - Provides requests for admin GUI review.
 * - On approval, expands only the plot where the request was made (per-plot radius).
 * - Fully integrated with GUIManager v1.2.5.
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

        boolean expanded = false;
        if (req.getRequestedAt() != null) {
            expanded = plugin.getPlotManager().expandPlot(playerId, req.getRequestedAt(), req.getBlocks());
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

    /* =========================  Admin GUI  ========================= */

    public void openRequestMenu(Player player) {
        String title = plugin.getConfig().getString("gui.menus.expansion-requests.title", "&eExpansion Requests");
        int size = plugin.getConfig().getInt("gui.menus.expansion-requests.size", 45);

        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        int slot = 0;
        for (ExpansionRequest req : getPendingRequests()) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(req.getRequester());

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(messages.color("&f" + (target.getName() != null ? target.getName() : req.getRequester().toString())));
                List<String> lore = new ArrayList<>();
                lore.add(messages.color("&7Blocks: &b" + req.getBlocks()));
                lore.add(messages.color("&7Requested: &f" + new Date(req.getTimestamp())));
                lore.add(messages.color("&aLeft-click: Approve"));
                lore.add(messages.color("&cRight-click: Deny"));
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        if (slot == 0) {
            // no requests
            ItemStack item = new ItemStack(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(messages.color("&cNo pending requests"));
                item.setItemMeta(meta);
            }
            inv.setItem(22, item);
        }

        player.openInventory(inv);
    }

    public void handleRequestClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = clicked.getItemMeta().getDisplayName();
        if (name == null) return;

        UUID targetUuid = null;
        for (ExpansionRequest req : getPendingRequests()) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(req.getRequester());
            String targetName = (op.getName() != null ? op.getName() : req.getRequester().toString());
            if (name.contains(targetName)) {
                targetUuid = req.getRequester();
                break;
            }
        }

        if (targetUuid == null) return;

        if (event.isLeftClick()) {
            approveRequest(targetUuid);
            messages.send(player, "&aApproved expansion for " + name);
            openRequestMenu(player);
        } else if (event.isRightClick()) {
            denyRequest(targetUuid, "custom-1"); // default deny reason, can expand into deny menu
            messages.send(player, "&cDenied expansion for " + name);
            openRequestMenu(player);
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
