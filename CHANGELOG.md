# 📜 ProShield Changelog

This file documents all notable changes to **ProShield**, starting from the first stable release.

---

## **1.1.9 — Quality of Life & Expanded Controls**
Released: 2025-09-07

### 🔑 New Features
- **Trust System**
  - `/proshield trust <player>` – allow another player to build in your claim.  
  - `/proshield untrust <player>` – revoke access.  
  - `/proshield trusted` – list trusted players.

- **Protection Expansion**
  - **Interactions**: blacklist/whitelist for doors, trapdoors, gates, buttons, levers, pressure plates, etc.  
  - **Explosions**: control creepers, TNT, withers, wither skulls, ender crystals, ender dragons.  
  - **Fire**: spread, ignite (flint & steel, lava, lightning), and burn.  
  - **Buckets**: toggle filling or emptying in claimed land.  
  - **Entity grief**: control Enderman, Ravagers, Silverfish, Ender Dragon, Wither grief.  
  - **Enderman teleport denial** inside claims.  
  - **Per-world overrides** for all of the above.

- **Claim Expiry**
  - Config option: expire claims after N days of inactivity.  
  - `/proshield expired list` – view expired claims.  
  - `/proshield expired restore <key>` – restore archived claim.  
  - `/proshield expired purge [days]` – purge expired claims.  
  - Expired claims moved to `claims_expired:` in config for safety.

- **Admin Features**
  - `/proshield bypass <on|off|toggle>` – toggle protection bypass.  
  - `/proshield reload` – reload configs and recache protection rules.  
  - Admin-only toggle: `/proshield settings adminUnlimited <on|off|toggle>` (guarded by `proshield.owner`).  
  - New permissions:
    - `proshield.admin.expired` – manage expired claims.  
    - `proshield.admin.settings` – change ProShield owner-level settings.

- **Compass Improvements**
  - Given automatically to OPs/admins with permission on join if missing.  
  - Right-click opens the ProShield GUI.  
  - Craftable with custom recipe (iron + redstone + compass).

---

## **1.1.8 — Stability & Polish**
Released: 2025-09-06

### 🛠 Fixes & Improvements
- Fixed GUI title checks (avoiding `getTitle()` issues).  
- Corrected listener wiring (GUI, compass, claims).  
- Join-compass logic improved:
  - Ensures only eligible OPs/admins receive compass.  
  - Prevents duplicate compasses in inventory.  
- Build improvements:
  - POM and JitPack locked to Java 17.
  - Paper API properly configured as `provided`.  
  - Clean and stable build artifacts.

---

## **1.1.7 — First Stable Public Build**
Released: 2025-09-05

### ✨ Features
- **Claims**
  - Players can claim chunks using `/proshield claim`.  
  - Unclaim land with `/proshield unclaim`.  
  - View claim info with `/
