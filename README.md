🛡️ ProShield

**ProShield** is an all-in-one land and plot protection plugin for Paper/Spigot servers.  
It provides a lightweight and modern alternative to heavy claim systems, offering **simple land protection, GUIs, and admin tools**.

---

## ✨ Features

- ✅ **Land Claiming** – Players can claim chunks of land with simple commands or via the ProShield GUI.  
- ✅ **Unclaiming** – Easily release land back for reuse.  
- ✅ **Protection System** – Prevents block breaking/placing in claimed areas for non-owners.  
- ✅ **Container & Interaction Protection** – Chests, furnaces, doors, buttons, levers, etc. with configurable whitelist/blacklist.  
- ✅ **Trust System** – Claim owners can trust/untrust other players.  
- ✅ **GUI Menu** – Clean, intuitive inventory-based interface:  
  - 🌱 Create Claim  
  - 📜 Claim Info  
  - ⛔ Remove Claim  
- ✅ **Admin Compass** – Custom craftable tool that opens the ProShield menu. OPs auto-receive it on join if missing.  
- ✅ **Automatic Data Saving** – Claims are persisted in `config.yml` and restored on restart.  
- ✅ **Admin/Operator Tools** – Includes bypass mode, teleport to claims, and force-unclaim options.  
- ✅ **Lightweight & Stable** – Minimal configuration, designed for survival/vanilla-style servers.  

---

## 📥 Installation

1. Download the latest `ProShield-1.1.8.jar` from [Releases](https://github.com/snazzyatoms/ProShield/releases).  
2. Place it into your server’s `plugins/` folder.  
3. Restart the server.  
4. Configuration (`config.yml` + `admin.yml`) will be auto-generated under `plugins/ProShield/`.

---

⌨️ Commands

Command	Description

/proshield	Shows plugin status.
/proshield claim	Claims the chunk at the player’s location.
/proshield unclaim	Unclaims the chunk (if owned).
/proshield info	Shows claim info at your location.
/proshield trust <player>	Trusts another player in your claim.
/proshield untrust <player>	Removes trust from a player.
/proshield compass	Gives you the ProShield compass.
/proshield reload	Reloads configs (config.yml + admin.yml).



---

🔑 Permissions

Permission	Description	Default

proshield.use	Allows using ProShield commands and GUI	true
proshield.admin	Grants access to admin features	op
proshield.compass	Allows receiving and using the compass	op
proshield.bypass	Allows toggling protection bypass	op
proshield.unlimited	Ignore max-claims limit	op
proshield.admin.tp	Allows teleporting to claims	op
proshield.admin.reload	Allows reloading configs	op



---

🧭 Admin Compass

Crafting Recipe

I = Iron Ingot

R = Redstone

C = Compass


Behavior

Opens the ProShield GUI when right-clicked.

Operators automatically receive one on join if missing.



---

📜 Changelog

v1.1.8 (Current Release)

✅ Added admin.yml file for admin-only settings.

✅ Added auto-give compass on join (configurable).

✅ Fixed permission issues for /proshield compass.

✅ Reworked GUI Manager + Listener with synchronized claim logic.

✅ Added trust/untrust system for claims.

✅ Added interaction protection (doors, levers, buttons) with whitelist/blacklist.

✅ Improved block protection logic (mob grief, TNT, creepers).

✅ Fixed constructor mismatches and duplicate errors.

✅ Synced config folder creation (ProShield folder auto-generates).

✅ General stability and bug fixes.



---

🛠️ Technical Notes

Built against PaperMC API 1.18+ (backwards-compatible with Spigot).

Requires Java 17+.

Designed for survival servers with lightweight protection needs.
