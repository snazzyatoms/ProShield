🛡️ ProShield

**ProShield** is an all-in-one land and plot protection plugin for Paper/Spigot servers.  
It provides a lightweight and modern alternative to heavy claim systems, offering **simple land protection, GUIs, and admin tools**.

---

## ✨ Features

- ✅ **Land Claiming** – Players can claim chunks of land with simple commands or via the ProShield GUI.  
- ✅ **Unclaiming** – Easily release land back for reuse.  
- ✅ **Protection System** – Prevents block breaking/placing in claimed areas for non-owners.  
- ✅ **GUI Menu** – Clean, intuitive inventory-based interface:  
  - 🌱 Create Claim  
  - 📜 Claim Info  
  - ⛔ Remove Claim  
- ✅ **Admin Compass** – Custom craftable tool that opens the ProShield menu.  
- ✅ **Automatic Data Saving** – Claims are persisted in `config.yml` and restored on restart.  
- ✅ **Admin/Operator Tools** – OPs receive a compass automatically on join if missing.  
- ✅ **Lightweight & Stable** – Minimal configuration, designed for survival/vanilla-style servers.  

---

## 📥 Installation

1. Download the latest `ProShield-x.x.x.jar` from [Releases](https://github.com/snazzyatoms/ProShield/releases).  
2. Place it into your server’s `plugins/` folder.  
3. Restart the server.  
4. Configuration (`config.yml`) will be auto-generated under `plugins/ProShield/`.



---

⌨️ Commands

Command	Description

/proshield	Shows plugin status.
/proshield claim	Claims the chunk at the player’s location.
/proshield unclaim	Unclaims the chunk (if owned by the player).



---

🔑 Permissions

Permission	Description	Default

proshield.use	Allows using ProShield commands and GUI.	true
proshield.admin	Grants admin compass recipe and OP join features.	op



---

🧭 Admin Compass

Crafting Recipe

I R I
R C R
I R I

I = Iron Ingot

R = Redstone

C = Compass


Behavior

Opens the ProShield GUI menu when right-clicked.

Operators automatically receive one on join if they don’t already have it.



---

📜 Changelog

v1.1.7 (Current Release)

Reworked & synchronized all core classes (ProShieldCommand, PlotManager, GUIManager, PlayerGUI, GUIListener, BlockProtectionListener, PlayerJoinListener).

Added Admin Compass recipe and auto-give for OPs.

Fixed constructor mismatches and duplicate class errors.

Synced GUI clicks with claim system logic.

Claims stored in config.yml, auto-loaded on enable, and saved on disable.

First stable public build!



---

🛠️ Technical Notes

Built against PaperMC API 1.18+ (backwards-compatible with Spigot).

Requires Java 17+.

Designed for survival servers with lightweight protection needs.
