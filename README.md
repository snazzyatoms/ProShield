# 🛡️ ProShield

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

## ⚙️ Configuration

Default `config.yml`:

```yaml
# ProShield default configuration
messages:
  prefix: "&3[ProShield]&r"

# Claims are stored automatically by PlotManager
claims: {}
