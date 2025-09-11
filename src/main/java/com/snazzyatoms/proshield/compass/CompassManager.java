package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * CompassManager
 *
 * - Builds ProShield compass variants (Player vs Admin)
 * - Identifies ProShield compasses via PDC
 * - Gives/refreshes compass to players (avoids duplicates)
 * - Right-click behavior is handled by CompassListener
 *
 * Admin experience:
 *   • Same default right-click (opens player menu)
 *   • SHIFT + right-click opens Admin menu
 *   • Admin compass looks distinct (name + glow + lore)
 */
public final class CompassManager {

    public enum CompassType { PLAYER, ADMIN }

    private static final String PDC_KEY = "proshield_compass_type";
    private static NamespacedKey COMPASS_TYPE_KEY;

    private CompassManager() {}

    public static void init(ProShield plugin) {
        COMPASS_TYPE_KEY = new NamespacedKey(plugin, PDC_KEY);
    }

    /** Give appropriate compass (admin gets admin-styled), replacing older ones if requested. */
    public static void giveCompass(Player player, boolean replaceExisting) {
        if (COMPASS_TYPE_KEY == null) {
            // safety: initialize if missed
            init(ProShield.getInstance());
        }

        boolean isAdmin = player.hasPermission("proshield.admin");
        ItemStack built = buildCompass(isAdmin ? CompassType.ADMIN : CompassType.PLAYER);

        // Remove duplicates / replace old
        cleanupExistingCompasses(player, replaceExisting);

        // Put in hotbar if possible; otherwise add to inventory
        PlayerInventory inv = player.getInventory();
        int preferred = findPreferredSlot(inv);
        if (preferred >= 0) {
            inv.setItem(preferred, built);
        } else {
            inv.addItem(built);
        }
    }

    /** Build a ProShield compass ItemStack with proper metadata. */
    public static ItemStack buildCompass(CompassType type) {
        ItemStack it = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = it.getItemMeta();
        if (meta == null) return it;

        // Name + Lore
        if (type == CompassType.ADMIN) {
            meta.setDisplayName(ChatColor.GOLD + "ProShield " + ChatColor.RED + "Admin " + ChatColor.GOLD + "Compass");
        } else {
            meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
        }

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "Right-click: " + ChatColor.WHITE + "Open ProShield menu");
        if (type == CompassType.ADMIN) {
            lore.add(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "Shift + Right-click: " + ChatColor.WHITE + "Admin Menu");
            lore.add(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "You have " + ChatColor.GOLD + "Admin" + ChatColor.GRAY + " capabilities");
        } else {
            lore.add(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "Manage your claim flags, trust, roles, etc.");
        }
        meta.setLore(lore);

        // Glow for Admin
        if (type == CompassType.ADMIN) {
            meta.addEnchant(Enchantment.LUCK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        } else {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }

        // PDC tag
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(COMPASS_TYPE_KEY, PersistentDataType.STRING, type.name());

        it.setItemMeta(meta);
        return it;
    }

    /** Is this ItemStack one of our ProShield compasses? */
    public static boolean isProShieldCompass(ItemStack it) {
        if (it == null || it.getType() != Material.COMPASS || !it.hasItemMeta()) return false;
        ItemMeta meta = it.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String val = pdc.get(COMPASS_TYPE_KEY, PersistentDataType.STRING);
        return val != null && (Objects.equals(val, CompassType.PLAYER.name()) || Objects.equals(val, CompassType.ADMIN.name()));
    }

    /** Returns the compass type or null if not a ProShield compass. */
    public static CompassType getType(ItemStack it) {
        if (!isProShieldCompass(it)) return null;
        String s = it.getItemMeta().getPersistentDataContainer().get(COMPASS_TYPE_KEY, PersistentDataType.STRING);
        try {
            return CompassType.valueOf(s);
        } catch (Exception ignore) {
            return null;
        }
    }

    /** Open the correct menu based on type and whether the player is sneaking. */
    public static void openFromCompass(Player player, ItemStack it) {
        GUIManager gui = ProShield.getInstance().getGuiManager();
        CompassType type = getType(it);
        if (type == CompassType.ADMIN && player.isSneaking()) {
            // Admin quick-access (SHIFT + right-click)
            gui.openAdmin(player);
        } else {
            // Default: open the player/main menu
            gui.openMain(player);
        }
    }

    /** Remove duplicate ProShield compasses; optionally replace all existing. */
    private static void cleanupExistingCompasses(Player player, boolean replaceExisting) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack slot = inv.getItem(i);
            if (isProShieldCompass(slot)) {
                if (replaceExisting) {
                    inv.setItem(i, null);
                } else {
                    // Already has one, do nothing
                    return;
                }
            }
        }
    }

    /** Prefer slot 0 unless occupied; otherwise first empty hotbar slot. */
    private static int findPreferredSlot(PlayerInventory inv) {
        // Slot 0 preferred if empty or contains non-valuable
        if (inv.getItem(0) == null) return 0;
        // Find first empty in hotbar
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i) == null) return i;
        }
        return -1;
    }

    /** Convenience: ensure player holds compass in main hand (optional). */
    public static void ensureHeld(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        if (!isProShieldCompass(main)) return;
        // already holding
    }

    /** Utility: quietly refresh admin vs player style (e.g., after permissions change). */
    public static void refreshFor(Player player) {
        boolean isAdmin = player.hasPermission("proshield.admin");
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (isProShieldCompass(hand)) {
            CompassType cur = getType(hand);
            if ((isAdmin && cur != CompassType.ADMIN) || (!isAdmin && cur != CompassType.PLAYER)) {
                // Replace in-hand
                player.getInventory().setItemInMainHand(buildCompass(isAdmin ? CompassType.ADMIN : CompassType.PLAYER));
            }
        }
    }
}
