package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

/**
 * Central GUI Manager for ProShield
 * - Builds menus from config (gui.menus.*)
 * - Enforces item permissions
 * - Supports actions (open/back/close/command/flag-toggle/trustPrompt/denyReasonPrompt)
 * - Tracks chat flows (deny reason, role add/remove)
 * - Maintains back-stack per-player
 */
public class GUIManager {

    private final ProShield plugin;
    private final MessagesUtil messages;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    // Chat-input trackers
    private final Map<UUID, Boolean> awaitingReason = new HashMap<>();
    private final Map<UUID, Boolean> awaitingRoleAction = new HashMap<>();

    // Menu back stacks: player -> stack of menu keys
    private final Map<UUID, Deque<String>> menuHistory = new HashMap<>();

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
        this.plotManager = plugin.getPlotManager();
        this.roleManager = plugin.getRoleManager();
    }

    // -------------------------------------------------
    // Public API used by listeners/commands
    // -------------------------------------------------
    public void openMenu(Player player, String menuKey) {
        if (player == null || menuKey == null) return;

        Inventory inv = buildMenu(player, menuKey);
        if (inv == null) {
            messages.send(player, "&cMenu not found: &f" + menuKey);
            return;
        }

        // push into history stack
        Deque<String> stack = menuHistory.computeIfAbsent(player.getUniqueId(), id -> new ArrayDeque<>());
        if (stack.isEmpty() || !menuKey.equalsIgnoreCase(stack.peek())) {
            stack.push(menuKey);
        }

        player.openInventory(inv);
    }

    /** Return to previous menu, if any */
    public void openPreviousMenu(Player player) {
        if (player == null) return;
        Deque<String> stack = menuHistory.get(player.getUniqueId());
        if (stack == null || stack.isEmpty()) {
            player.closeInventory();
            return;
        }
        // pop current
        stack.pop();
        if (stack.isEmpty()) {
            player.closeInventory();
            return;
        }
        // open previous
        String prev = stack.peek();
        Inventory inv = buildMenu(player, prev);
        if (inv == null) {
            player.closeInventory();
            return;
        }
        player.openInventory(inv);
    }

    /**
     * Global click handler, called by GUIListener.
     * Detects which configured item was clicked and executes its action.
     */
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        // Identify which menu this inventory is (by title look-up)
        String menuKey = resolveMenuKeyByTitle(event.getView().getTitle());
        if (menuKey == null) return; // not a ProShield menu

        FileConfiguration cfg = plugin.getConfig();
        String base = "gui.menus." + menuKey;

        // Which slot?
        int slot = event.getRawSlot();
        String itemPath = base + ".items." + slot;
        if (!cfg.isConfigurationSection(itemPath)) return;

        // Permission gate
        String perm = cfg.getString(itemPath + ".permission", "");
        if (perm != null && !perm.isEmpty() && !player.hasPermission(perm)) {
            messages.send(player, "&cYou lack permission: &f" + perm);
            return;
        }

        // What action?
        String action = cfg.getString(itemPath + ".action", "");
        if (action == null) action = "";

        // Dispatch
        if (action.equalsIgnoreCase("close")) {
            player.closeInventory();
            return;
        }

        if (action.equalsIgnoreCase("back")) {
            openPreviousMenu(player);
            return;
        }

        if (action.startsWith("open:")) {
            String target = action.substring("open:".length()).trim();
            openMenu(player, target);
            return;
        }

        if (action.startsWith("command:")) {
            String cmd = action.substring("command:".length()).trim();
            if (cmd.startsWith("/")) cmd = cmd.substring(1);
            player.closeInventory();
            // run as player
            Bukkit.getScheduler().runTask(plugin, () -> player.performCommand(cmd));
            return;
        }

        if (action.startsWith("flag-toggle:")) {
            String flagKey = action.substring("flag-toggle:".length()).trim();
            toggleFlag(player, flagKey);
            // refresh menu to show updated state
            Bukkit.getScheduler().runTask(plugin, () -> openMenu(player, menuKey));
            return;
        }

        if (action.equalsIgnoreCase("trustPrompt")) {
            setAwaitingRoleAction(player, true);
            player.closeInventory();
            messages.send(player, "&eType '&fplayerName [role]&e' in chat to trust.");
            return;
        }

        if (action.equalsIgnoreCase("denyReasonPrompt")) {
            setAwaitingReason(player, true);
            player.closeInventory();
            messages.send(player, "&eType a reason in chat to deny the expansion.");
            return;
        }

        // Unknown → no-op but let the user know
        messages.send(player, "&eNothing configured for this item.");
    }

    // -------------------------------------------------
    // Menu building
    // -------------------------------------------------
    private Inventory buildMenu(Player player, String menuKey) {
        FileConfiguration cfg = plugin.getConfig();
        String base = "gui.menus." + menuKey;
        ConfigurationSection sec = cfg.getConfigurationSection(base);
        if (sec == null) return null;

        String title = color(sec.getString("title", "&3ProShield"));
        int size = Math.max(9, Math.min(54, sec.getInt("size", 27)));

        Inventory inv = Bukkit.createInventory(null, size, title);

        // Items
        ConfigurationSection items = sec.getConfigurationSection("items");
        if (items != null) {
            for (String slotKey : items.getKeys(false)) {
                int slot;
                try {
                    slot = Integer.parseInt(slotKey);
                } catch (NumberFormatException nfe) {
                    continue;
                }
                if (slot < 0 || slot >= size) continue;

                String iBase = base + ".items." + slotKey;
                String perm = cfg.getString(iBase + ".permission", "");
                String matName = cfg.getString(iBase + ".material", "STONE");
                String name = cfg.getString(iBase + ".name", "Unnamed");
                List<String> lore = cfg.getStringList(iBase + ".lore");

                ItemStack item = icon(matName, name, lore, perm);
                inv.setItem(slot, item);
            }
        }

        // Optional auto "Back" button if configured
        if (sec.getBoolean("auto-back-button", true)) {
            // If not root, drop a back item in last slot
            Deque<String> stack = menuHistory.get(player.getUniqueId());
            boolean showBack = stack != null && stack.size() > 0 && !isRootOf(stack, menuKey);
            if (showBack) {
                int backSlot = Math.min(size - 1, Math.max(0, sec.getInt("back-slot", size - 1)));
                ItemStack back = icon("ARROW", "&e&lBack", Collections.singletonList("&7Return to previous menu"), "");
                inv.setItem(backSlot, back);
                // Provide default action for last slot if not configured
                // (If you also configured an item in that slot, your explicit config wins)
            }
        }

        return inv;
    }

    private boolean isRootOf(Deque<String> stack, String menuKey) {
        // Root if this key is the bottom of the stack
        if (stack == null || stack.isEmpty()) return true;
        Iterator<String> it = stack.descendingIterator();
        return it.hasNext() && menuKey.equalsIgnoreCase(it.next());
    }

    private String resolveMenuKeyByTitle(String titleColored) {
        if (titleColored == null) return null;
        String title = ChatColor.stripColor(titleColored);

        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection menus = cfg.getConfigurationSection("gui.menus");
        if (menus == null) return null;

        for (String key : menus.getKeys(false)) {
            String t = cfg.getString("gui.menus." + key + ".title", key);
            if (ChatColor.stripColor(color(t)).equalsIgnoreCase(title)) {
                return key;
            }
        }
        return null;
    }

    // -------------------------------------------------
    // Actions
    // -------------------------------------------------
    private void toggleFlag(Player player, String flagKey) {
        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, plugin.getMessagesConfig().getString("flags.no-claim", "&cNo claim found here to toggle flags."));
            return;
        }
        // Owner or admin required to toggle
        if (!plot.getOwner().equals(player.getUniqueId()) && !player.hasPermission("proshield.admin")) {
            messages.send(player, "&cOnly the owner (or admin) can toggle flags here.");
            return;
        }
        boolean current = plot.getFlag(flagKey, false);
        plot.setFlag(flagKey, !current);

        String state = (!current) ? "&aENABLED" : "&cDISABLED";
        messages.send(player,
                plugin.getMessagesConfig().getString("flags.toggle", "&eFlag &b{flag}&e is now {state} for {claim}.")
                        .replace("{flag}", flagKey)
                        .replace("{state}", state)
                        .replace("{claim}", plot.getWorld() + ":" + plot.getX() + "," + plot.getZ()));
    }

    // -------------------------------------------------
    // Chat flows
    // -------------------------------------------------
    public boolean isAwaitingReason(Player player) {
        return awaitingReason.getOrDefault(player.getUniqueId(), false);
    }

    public void setAwaitingReason(Player player, boolean waiting) {
        awaitingReason.put(player.getUniqueId(), waiting);
    }

    public void provideManualReason(Player player, String reason) {
        awaitingReason.remove(player.getUniqueId());
        messages.send(player, "&cExpansion denied. &7Reason: &f" + reason);
        // Hook ExpansionManager if/when added
    }

    public boolean isAwaitingRoleAction(Player player) {
        return awaitingRoleAction.getOrDefault(player.getUniqueId(), false);
    }

    public void setAwaitingRoleAction(Player player, boolean waiting) {
        awaitingRoleAction.put(player.getUniqueId(), waiting);
    }

    public void handleRoleChatInput(Player player, String message) {
        awaitingRoleAction.remove(player.getUniqueId());
        // Delegate to ClaimRoleManager parser (supports: "<player> [role]")
        roleManager.assignRoleViaChat(player, message);
    }

    // -------------------------------------------------
    // Utilities
    // -------------------------------------------------
    public File getGuiDataFile() {
        return new File(plugin.getDataFolder(), "guis.yml");
    }

    private ItemStack icon(String materialName, String displayName, List<String> rawLore, String perm) {
        Material mat = Material.matchMaterial(materialName);
        if (mat == null) mat = Material.STONE;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(displayName));

            List<String> lore = new ArrayList<>();
            if (rawLore != null && !rawLore.isEmpty()) {
                for (String l : rawLore) lore.add(color(l));
            }
            if (perm != null && !perm.isEmpty()) {
                lore.add(color("&8Requires: &7" + perm));
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String color(String s) {
        return s == null ? "" : ChatColor.translateAlternateColorCodes('&', s);
    }

    // Optional helper (if you need to force-close any ProShield inventory)
    public void closeIfProShieldMenu(HumanEntity viewer) {
        String key = resolveMenuKeyByTitle(viewer.getOpenInventory().getTitle());
        if (key != null) viewer.closeInventory();
    }
}
