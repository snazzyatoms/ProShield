// inside GUIManager.java

    /* ==========================
     * Roles menu (with pagination)
     * ========================== */
    private void openRolesMenu(Player player) {
        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        if (plot == null) {
            plugin.getMessagesUtil().send(player, "&cYou must stand inside a claim to manage roles.");
            return;
        }
        if (!plot.isOwner(player.getUniqueId())) {
            plugin.getMessagesUtil().send(player, "&cOnly the claim owner can manage trusted players.");
            return;
        }

        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus.roles");
        String title = "&bTrusted Players";
        int size = 27;
        if (menuSec != null) {
            title = menuSec.getString("title", title);
            size = menuSec.getInt("size", size);
        }
        title = ChatColor.translateAlternateColorCodes('&', title);

        // pagination
        openRolesPage(player, plot, 0, size, title);
    }

    /**
     * Open a paginated roles view
     */
    private void openRolesPage(Player player, Plot plot, int page, int size, String title) {
        Inventory inv = Bukkit.createInventory(null, size, title + " (Page " + (page+1) + ")");

        // Config buttons (add/remove/back)
        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus.roles");
        if (menuSec != null) {
            ConfigurationSection itemsSec = menuSec.getConfigurationSection("items");
            if (itemsSec != null) {
                for (String slotStr : itemsSec.getKeys(false)) {
                    int slot = parseIntSafe(slotStr, -1);
                    if (slot < 0) continue;
                    ConfigurationSection itemSec = itemsSec.getConfigurationSection(slotStr);
                    if (itemSec == null) continue;

                    Material mat = Material.matchMaterial(itemSec.getString("material", "STONE"));
                    if (mat == null) mat = Material.STONE;

                    ItemStack stack = new ItemStack(mat);
                    ItemMeta meta = stack.getItemMeta();
                    if (meta == null) continue;

                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemSec.getString("name", "")));
                    List<String> lore = itemSec.getStringList("lore");
                    if (lore != null && !lore.isEmpty()) {
                        lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s));
                        meta.setLore(lore);
                    }
                    stack.setItemMeta(meta);
                    inv.setItem(slot, stack);
                }
            }
        }

        // Trusted player heads
        ClaimRoleManager rm = plugin.getRoleManager();
        Map<String, String> trusted = rm.getTrusted(plot.getId());
        List<Map.Entry<String,String>> list = new ArrayList<>(trusted.entrySet());

        int[] slots = headFillPattern(size);
        int perPage = slots.length;
        int start = page * perPage;
        int end = Math.min(start + perPage, list.size());

        for (int i = start; i < end; i++) {
            int slot = slots[i - start];
            Map.Entry<String, String> e = list.get(i);
            inv.setItem(slot, createPlayerHead(e.getKey(), e.getValue(), plot.getId()));
        }

        // Navigation buttons if needed
        if (page > 0) {
            inv.setItem(size-9, simple(Material.ARROW, ChatColor.YELLOW + "Previous Page",
                    List.of(ChatColor.GRAY + "Go to page " + page)));
        }
        if (end < list.size()) {
            inv.setItem(size-1, simple(Material.ARROW, ChatColor.YELLOW + "Next Page",
                    List.of(ChatColor.GRAY + "Go to page " + (page+2))));
        }

        awaitingRolePlot.put(player.getUniqueId(), plot.getId());
        player.openInventory(inv);
    }

    /* Extend handleClick to handle pagination */
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        String title = event.getView().getTitle();
        String name = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        // Roles menu pagination support
        if (title.contains("Trusted Players")) {
            Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
            if (plot == null) return;

            if (name.equalsIgnoreCase("Next Page")) {
                int page = parsePage(title);
                openRolesPage(player, plot, page+1, event.getInventory().getSize(), stripPage(title));
                return;
            }
            if (name.equalsIgnoreCase("Previous Page")) {
                int page = parsePage(title);
                openRolesPage(player, plot, page-1, event.getInventory().getSize(), stripPage(title));
                return;
            }
        }

        // ... rest of your existing handleClick
    }

    private int parsePage(String title) {
        if (!title.contains("(Page")) return 0;
        try {
            String sub = title.substring(title.indexOf("(Page")+6, title.indexOf(")"));
            return Integer.parseInt(sub.trim())-1;
        } catch (Exception e) {
            return 0;
        }
    }

    private String stripPage(String title) {
        return title.replaceAll("\\(Page.*\\)", "").trim();
    }
// inside GUIManager.java

    /* ==========================
     * Roles menu (with pagination)
     * ========================== */
    private void openRolesMenu(Player player) {
        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        if (plot == null) {
            plugin.getMessagesUtil().send(player, "&cYou must stand inside a claim to manage roles.");
            return;
        }
        if (!plot.isOwner(player.getUniqueId())) {
            plugin.getMessagesUtil().send(player, "&cOnly the claim owner can manage trusted players.");
            return;
        }

        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus.roles");
        String title = "&bTrusted Players";
        int size = 27;
        if (menuSec != null) {
            title = menuSec.getString("title", title);
            size = menuSec.getInt("size", size);
        }
        title = ChatColor.translateAlternateColorCodes('&', title);

        // pagination
        openRolesPage(player, plot, 0, size, title);
    }

    /**
     * Open a paginated roles view
     */
    private void openRolesPage(Player player, Plot plot, int page, int size, String title) {
        Inventory inv = Bukkit.createInventory(null, size, title + " (Page " + (page+1) + ")");

        // Config buttons (add/remove/back)
        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus.roles");
        if (menuSec != null) {
            ConfigurationSection itemsSec = menuSec.getConfigurationSection("items");
            if (itemsSec != null) {
                for (String slotStr : itemsSec.getKeys(false)) {
                    int slot = parseIntSafe(slotStr, -1);
                    if (slot < 0) continue;
                    ConfigurationSection itemSec = itemsSec.getConfigurationSection(slotStr);
                    if (itemSec == null) continue;

                    Material mat = Material.matchMaterial(itemSec.getString("material", "STONE"));
                    if (mat == null) mat = Material.STONE;

                    ItemStack stack = new ItemStack(mat);
                    ItemMeta meta = stack.getItemMeta();
                    if (meta == null) continue;

                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemSec.getString("name", "")));
                    List<String> lore = itemSec.getStringList("lore");
                    if (lore != null && !lore.isEmpty()) {
                        lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s));
                        meta.setLore(lore);
                    }
                    stack.setItemMeta(meta);
                    inv.setItem(slot, stack);
                }
            }
        }

        // Trusted player heads
        ClaimRoleManager rm = plugin.getRoleManager();
        Map<String, String> trusted = rm.getTrusted(plot.getId());
        List<Map.Entry<String,String>> list = new ArrayList<>(trusted.entrySet());

        int[] slots = headFillPattern(size);
        int perPage = slots.length;
        int start = page * perPage;
        int end = Math.min(start + perPage, list.size());

        for (int i = start; i < end; i++) {
            int slot = slots[i - start];
            Map.Entry<String, String> e = list.get(i);
            inv.setItem(slot, createPlayerHead(e.getKey(), e.getValue(), plot.getId()));
        }

        // Navigation buttons if needed
        if (page > 0) {
            inv.setItem(size-9, simple(Material.ARROW, ChatColor.YELLOW + "Previous Page",
                    List.of(ChatColor.GRAY + "Go to page " + page)));
        }
        if (end < list.size()) {
            inv.setItem(size-1, simple(Material.ARROW, ChatColor.YELLOW + "Next Page",
                    List.of(ChatColor.GRAY + "Go to page " + (page+2))));
        }

        awaitingRolePlot.put(player.getUniqueId(), plot.getId());
        player.openInventory(inv);
    }

    /* Extend handleClick to handle pagination */
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        String title = event.getView().getTitle();
        String name = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        // Roles menu pagination support
        if (title.contains("Trusted Players")) {
            Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
            if (plot == null) return;

            if (name.equalsIgnoreCase("Next Page")) {
                int page = parsePage(title);
                openRolesPage(player, plot, page+1, event.getInventory().getSize(), stripPage(title));
                return;
            }
            if (name.equalsIgnoreCase("Previous Page")) {
                int page = parsePage(title);
                openRolesPage(player, plot, page-1, event.getInventory().getSize(), stripPage(title));
                return;
            }
        }

        // ... rest of your existing handleClick
    }

    private int parsePage(String title) {
        if (!title.contains("(Page")) return 0;
        try {
            String sub = title.substring(title.indexOf("(Page")+6, title.indexOf(")"));
            return Integer.parseInt(sub.trim())-1;
        } catch (Exception e) {
            return 0;
        }
    }

    private String stripPage(String title) {
        return title.replaceAll("\\(Page.*\\)", "").trim();
    }
