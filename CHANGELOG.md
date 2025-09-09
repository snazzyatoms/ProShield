# 📜 ProShield Changelog

This file documents all notable changes to **ProShield**, starting from the first stable release.

---

## **1.2.3 — Polish, Flexibility & Ownership Tools**
Released: 2025-09-09

### 🔑 New Features
- **Claim Borders Preview**  
  - `/proshield preview` → shows a particle outline of the claim before confirming.  
  - Helps players visualize claim boundaries.  

- **Claim Ownership Transfer**  
  - `/proshield transfer <player>` → transfers a claim to another player.  
  - Useful for moving ownership between friends or admins.  

- **Back Button in GUIs**  
  - Added a **Back button** to all submenus (player & admin).  
  - No need to close GUIs with `Esc` to return to the main menu.  

- **Debug Logging Toggle**  
  - `/proshield debug <on|off>` → enable detailed debug logs.  
  - Permission: `proshield.admin.debug`.  
  - Easier troubleshooting for server owners.  

- **Better Messages & Compass UI Polish**  
  - More informative feedback messages for claiming, trusting, and admin actions.  
  - Compass GUI layout polished with clearer icons and tooltips.  

### 🛠 Fixes & Improvements
- **Configuration Migration Polish**  
  - Ensures config always shows the correct version (`1.2.3` for this release).  
  - Safer reloads with validation to prevent YAML errors.  

- **Item Protection Expansion**  
  - Improved handling of item-keep in claims to ensure stability.  
  - Still **off by default**, configurable in `config.yml` or via Admin GUI.  

- **Role Management Tuning**  
  - Smoother assignment of roles via commands & GUI.  
  - Prevents duplicate or invalid entries.  

- **Compass Join Logic**  
  - Improved handling when inventory is full (items go to overflow or dropped safely).  

- **UI Spam Prevention**  
  - Blocked rapid GUI click-spamming from causing errors or lag.  

- **Maintenance**  
  - General code cleanup and listener organization.  
  - Deprecated API calls refactored for long-term stability.  

---

## **1.2.2 — Item Keep & GUI Enhancements**
Released: 2025-09-08

### 🔑 New Features
- **Keep Items in Claims (NEW)**  
  - Items dropped inside claims can optionally be protected from despawning.  
  - Configurable in `config.yml` under `claims.keep-items`.  
  - Default: **off** (server owners must enable it).  
  - Adjustable despawn delay: default `900s` (15 min), configurable between `300–900s`.  
  - Admin GUI toggle available (requires `proshield.admin.keepdrops`).  

- **GUI Enhancements**
  - Added a **Help slot (31)** → Shows only commands available to the player’s permissions/roles.  
  - Wired **Admin slot (33)** → Opens the admin GUI instead of placeholder text.  
  - More polished layout for clarity.  

- **Config Improvements**
  - Added version header: now shows **1.2.2** on fresh installs.  
  - Clearer comments and grouping for protection toggles and claim options.  
  - Automatic migration snippet ensures `claims.keep-items` is added if missing.  

### 🛠 Fixes & Improvements
- Fixed constructor mismatch between `ProShield.java` and `GUIManager.java`.  
- General cleanup of listeners and event checks.  
- Minor performance improvements when handling trusted players and role lookups.  
- Compass distribution logic further hardened to prevent edge-case duplication.
