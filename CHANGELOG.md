# 📜 ProShield Changelog

This file documents all notable changes to **ProShield**, starting from the first stable release.

---

## **1.2.6 — Compass Sync, Crop Trample & GUI Polish**
Released: 2025-09-26

### 🔑 New Features
- **Refined GUIs (NEW)**  
  - All placeholder `#` tags removed from buttons, lore, and info.  
  - Synced GUI titles with `messages.yml`.  
  - Back/Exit buttons are now fully standardized across all menus.  

- **Compass Sync (NEW)**  
  - Unified `CompassManager` + `GUIListener` logic.  
  - Safer detection of ProShield compass via a single display name constant.  
  - Fixed issues with double-right-click and off-hand behavior.  

- **Crop Trample Flag (NEW)**  
  - Added `crop-trample` claim flag to toggle farmland trampling.  
  - Default: **enabled** (prevent trample).  
  - Configurable in `config.yml` and toggleable in the Claim Flags GUI.  

- **World Placeholders (NEW)**  
  - Added Nether & End placeholders in **World Controls** GUI.  
  - Reserved for future per-dimension support (v2.0+).  

- **Multilingual Support (NEW)**  
  - All messages are loaded from `messages.yml`.  
  - Owners can freely customize or translate ProShield into any language.  
  - No need to wait for updates — every line of text is configurable.  

### 🛠 Fixes & Improvements
- **Admin Tools Polish**  
  - Fixed non-functional **Reload Config**, **World Controls**, and **Expansion Requests** buttons.  
  - All submenus now open and respond properly.  

- **Navigation Stability**  
  - Improved GUI stack handling when menus are closed/reopened.  
  - Prevented accidental nav resets when switching between ProShield menus.  

- **Performance & Safety**  
  - Reduced redundant checks in compass and GUI listeners.  
  - Smoothed event handling for faster clicks and less spam.  

- **Migration Note**  
  ⚠️ On first upgrade to **1.2.6**, you **must regenerate your ProShield configs** to sync new features:  
  1. Stop your server.  
  2. Backup and delete `/plugins/ProShield/`.  
  3. Restart → new `config.yml` + `messages.yml` are generated.  
  4. Reapply any custom edits (translations, flags, roles).  

---

## **1.2.5 — Expansions, World Controls & Safezone Mobs**
Released: 2025-09-17

### 🔑 New Features
- **Expansion Requests (NEW)**  
  - Players can request claim expansions through a dedicated GUI.  
  - Requests are queued for admin review.  
  - Configurable defaults in `config.yml`.  

- **Admin Expansion Review (NEW)**  
  - Admin GUI menu to **approve** or **deny** requests.  
  - Deny reasons are fully configurable in `messages.yml`.  
  - Uses hidden tags (UUID + timestamp) to ensure secure approval/denial.  

- **Expansion History (NEW)**  
  - Paginated GUI showing past requests (approved, denied, expired).  
  - Includes reason details for denied requests.  
  - Easy navigation with **Previous / Next page** buttons.  

- **World Controls Menu (NEW)**  
  - Admin-only GUI to toggle global protections such as:  
    - Fire spread  
    - TNT/explosions  
    - Mob damage  
  - Pulled dynamically from `config.yml` → synced with `messages.yml`.  

- **Safezone Mob Protection (NEW)**  
  - Monsters are repelled from safezone borders.  
  - Hostile mobs inside safezones automatically despawn.  
  - Prevents mobs from pathing/targeting players inside safezones.  
  - Configurable under `protection.mobs.*`.  

- **GUI Navigation Overhaul (IMPROVED)**  
  - **Back** and **Exit** buttons now appear consistently across *all* GUIs.  
  - Fully functional (no placeholders).  

### 🛠 Fixes & Improvements
- **Messages System Upgrade**  
  - Added support for fallback values via `messages.getOrDefault`.  
  - Admin and deny menus pull text dynamically from `messages.yml`.  
  - More flexible configuration for server owners.  

- **Admin Tools Polish**  
  - Added reload of **configs**, **messages.yml**, and **expansions** via GUI.  
  - Debug toggle and bypass toggle polished with clearer feedback.  

- **GUI Functionality Restored**  
  - All GUI menus (Trusted, Roles, Flags, Expansions, Admin) are now fully clickable and functional.  
  - Previously placeholder-only menus now execute their intended actions.  

- **Performance**  
  - Reduced redundant lookups in expansion and role GUIs.  
  - Smoothed event handling and inventory navigation.  

- **Migration Note**  
  ⚠️ On first upgrade to **1.2.5**, you **must regenerate your ProShield config and messages.yml**:  
  1. Stop your server.  
  2. Backup and delete `/plugins/ProShield/`.  
  3. Restart → new configs & messages are generated.  
  4. Reapply your custom edits.  

---

## **1.2.4 — Stability, Admin Reload & Mob Repel**
Released: 2025-09-09

... (unchanged, see previous entries) ...
