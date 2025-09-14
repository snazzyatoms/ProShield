// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.ExpansionRequest;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GUIManager implements Listener {

    private final ProShield plugin;
    private final MessagesUtil messages;

    // Track admin denial context
    private final Map<UUID, ExpansionRequest> denyContext = new HashMap<>();
    private final Map<UUID, ExpansionRequest> customDenyWaiting = new HashMap<>();

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
    }

    // === GENERIC MENU LOADER ===
    private void loadConfiguredItems(Inventory inv, String path, ExpansionRequest context) {
        ConfigurationSection items = plugin.getConfig().getConfigurationSection(path + ".items");
        if (items == null) return;

        for (String slotKey : items.getKeys(false)) {
            int slot = Integer.parseInt(slotKey);
            String matName = plugin.getConfig().getString(path + ".items." + slotKey + ".material", "BARRIER");
            String displayName = plugin.getConfig().getString(path + ".items." + slotKey + ".name", "");
            List<String> lore = plugin.getConfig().getStringList(path + ".items." + slotKey + ".lore");

            ItemStack stack = new ItemStack(Material.matchMaterial(matName), 1);
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
                List<String> loreLines = new ArrayList<>();
                for (String line : lore) {
                    loreLines.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(loreLines);
                stack.setItemMeta(meta);
            }
            inv.setItem(slot, stack);
        }
    }

    private String getActionFromItem(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta() || !stack.getItemMeta().hasDisplayName()) return null;
        // Simplified: action encoded in displayName OR config (for now just use name mapping)
        String name = ChatColor.stripColor(stack.getItemMeta().getDisplayName());
        return name.toLowerCase().replace(" ", "");
    }

    // === MAIN OPEN METHODS ===
    public void openExpansionRequests(Player admin) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.AQUA + "Expansion Requests");

        List<ExpansionRequest> requests = plugin.getExpansionQueue().getAll();
        int slot = 0;
        for (ExpansionRequest req : requests) {
            if (slot >= 45) break; // max visible
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            if (meta != null) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(req.getPlayerId());
                meta.setDisplayName(ChatColor.YELLOW + "Request from " + target.getName());
                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "Requested size: +" + req.getExtraBlocks() + " blocks",
                        ChatColor.GRAY + "Status: " + req.getStatus()
                ));
                paper.setItemMeta(meta);
            }
            inv.setItem(slot++, paper);
        }

        // Load static items (Back + Teaser)
        loadConfiguredItems(inv, "gui.menus.expansion-requests", null);
        admin.openInventory(inv);
    }

    public void openDenyReasons(Player admin, ExpansionRequest req) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.RED + "Select Denial Reason");
        loadConfiguredItems(inv, "gui.menus.deny-reasons", req);
        admin.openInventory(inv);
        denyContext.put(admin.getUniqueId(), req);
    }

    // === EVENT HANDLING ===
    @EventHandler
    public void onExpansionClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        String title = ChatColor.stripColor(e.getView().getTitle());
        if (!title.equalsIgnoreCase("Expansion Requests")) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String action = getActionFromItem(clicked);
        if (action == null) return;

        // If clicked on a request (paper)
        if (clicked.getType() == Material.PAPER) {
            int slot = e.getSlot();
            List<ExpansionRequest> requests = plugin.getExpansionQueue().getAll();
            if (slot < requests.size()) {
                ExpansionRequest req = requests.get(slot);

                // Open submenu: approve/deny
                Inventory sub = Bukkit.createInventory(null, 27,
                        ChatColor.GOLD + "Approve/Deny: " + Bukkit.getOfflinePlayer(req.getPlayerId()).getName());
                // Approve
                ItemStack green = new ItemStack(Material.LIME_WOOL);
                ItemMeta gMeta = green.getItemMeta();
                gMeta.setDisplayName(ChatColor.GREEN + "Approve Expansion");
                green.setItemMeta(gMeta);
                sub.setItem(11, green);
                // Deny
                ItemStack red = new ItemStack(Material.RED_WOOL);
                ItemMeta rMeta = red.getItemMeta();
                rMeta.setDisplayName(ChatColor.RED + "Deny Expansion");
                red.setItemMeta(rMeta);
                sub.setItem(15, red);

                player.openInventory(sub);
                denyContext.put(player.getUniqueId(), req);
            }
            return;
        }

        if (action.equals("menu:admin")) {
            plugin.getGuiManager().openAdminMenu(player);
        }
    }

    @EventHandler
    public void onApproveDenyClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        String title = ChatColor.stripColor(e.getView().getTitle());
        if (!title.startsWith("Approve/Deny:")) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ExpansionRequest req = denyContext.get(player.getUniqueId());
        if (req == null) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (name.equalsIgnoreCase("Approve Expansion")) {
            plugin.getPlotManager().expandPlot(req.getPlayerId(), req.getExtraBlocks());
            denyContext.remove(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Expansion approved instantly!");
            plugin.getExpansionQueue().remove(req);
            openExpansionRequests(player);
        } else if (name.equalsIgnoreCase("Deny Expansion")) {
            openDenyReasons(player, req);
        }
    }

    @EventHandler
    public void onDenyReasonClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        String title = ChatColor.stripColor(e.getView().getTitle());
        if (!title.equalsIgnoreCase("Select Denial Reason")) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ExpansionRequest req = denyContext.get(player.getUniqueId());
        if (req == null) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        switch (name.toLowerCase()) {
            case "too large" -> denyRequest(req, "Request too large");
            case "abuse/grief risk" -> denyRequest(req, "Abuse or griefing risk");
            case "not eligible yet" -> denyRequest(req, "Player not eligible yet");
            case "custom reason" -> {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Type a custom denial reason in chat:");
                customDenyWaiting.put(player.getUniqueId(), req);
                return;
            }
            case "back" -> openExpansionRequests(player);
        }
        denyContext.remove(player.getUniqueId());
        openExpansionRequests(player);
    }

    @EventHandler
    public void onCustomDenyChat(AsyncPlayerChatEvent e) {
        UUID adminId = e.getPlayer().getUniqueId();
        if (!customDenyWaiting.containsKey(adminId)) return;

        e.setCancelled(true);
        ExpansionRequest req = customDenyWaiting.remove(adminId);
        String reason = e.getMessage();

        denyRequest(req, reason);
        e.getPlayer().sendMessage(ChatColor.RED + "Denied with custom reason: " + reason);

        Bukkit.getScheduler().runTask(plugin, () -> openExpansionRequests(e.getPlayer()));
    }

    private void denyRequest(ExpansionRequest req, String reason) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(req.getPlayerId());
        if (target.isOnline()) {
            ((Player) target).sendMessage(ChatColor.RED + "Your claim expansion was denied: " + reason);
        }
        plugin.getExpansionQueue().remove(req);
    }

    // Placeholder for opening admin menu
    public void openAdminMenu(Player admin) {
        admin.sendMessage(ChatColor.GRAY + "Admin menu placeholder (expand here).");
    }
}
