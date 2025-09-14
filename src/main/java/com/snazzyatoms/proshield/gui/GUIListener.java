@EventHandler
public void onInventoryClick(InventoryClickEvent event) {
    String title = event.getView().getTitle();
    if (title.contains("ProShield")
            || title.contains("Claim")
            || title.contains("Flags")
            || title.contains("Trusted Players")
            || title.contains("Expansion Requests")) {

        event.setCancelled(true);
        guiManager.handleClick(event);
    }
}
