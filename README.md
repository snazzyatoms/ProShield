# 🛡️ ProShield
*A lightweight, polished land & plot protection plugin for Spigot & Paper servers.*

> Perfect for **Survival** or **SMP communities**, ProShield provides **multilingual support (20+ languages)**, intuitive GUIs, and powerful admin tools — without the heavy bloat of larger region plugins.  
> Everything is designed to be fully manageable through the **GUI menus**, so commands are entirely optional.

---

## ✨ Features

- 🏡 **Land Claiming** – Claim chunks instantly via the **ProShield Compass GUI** (no commands required).  
- ❌ **Unclaiming** – Release land through the GUI with a single click.  
- 🔒 **Protection System** – Prevent griefing: block break/place, containers, fire spread, TNT/creeper explosions, entity grief, and more — all toggleable in the GUI.  
- 🌐 **Multilingual Support** – Ships with 20+ language packs. Server owners can switch easily in `config.yml`.  
- 👥 **Trust System** – Manage trusted players from the GUI. Add/remove access with a click.  
- 🎭 **Claim Roles** *(since 1.2.1)* – Assign role-based access: Visitor, Member, Builder, Container, Moderator, Manager, Owner. Fully configurable.  
- 🎒 **Keep Items in Claims** *(1.2.2+)* – Prevent items dropped inside claims from despawning (toggled in GUI).  
- 🔄 **Ownership Transfer** *(1.2.3)* – Transfer claim ownership directly from the GUI.  
- ✨ **Claim Borders Preview** *(1.2.3)* – Visualize claim boundaries before confirming.  
- 🖥️ **Intuitive GUIs** – Every feature is available in menus: Claims, Flags, Roles, Expansions, Admin Tools.  
- ⏪ **Back & Exit Buttons** *(improved 1.2.5)* – Consistent navigation across all menus.  
- 🧭 **ProShield Compass** – Right-click to open the menu instantly (auto-given if enabled).  
- 🕒 **Claim Expiry** – Automatically remove claims from inactive players (optional, configurable).  
- ⚡ **Configurable Protections** – Doors, buttons, buckets, mobs, explosions, fire spread, container access, etc. All manageable in the GUI.  
- 🛑 **Spawn Guard** *(1.2.4)* – Prevent claiming near spawn radius.  
- 🧟 **Mob Control** *(enhanced 1.2.5)* – Repel mobs from claim borders, despawn hostile mobs, and block targeting inside safezones.  
- 📈 **Expansion Requests** *(1.2.5)* – Players request claim expansions directly in the GUI.  
- 📜 **Expansion History** *(1.2.5)* – Paginated GUI showing past approvals/denials.  
- 🌍 **World Controls** *(1.2.5)* – Admin-only GUI to toggle global protections (PvP, explosions, mob damage, fire, crops, etc.).  
- 🔧 **Reload & Debug Tools** *(improved 1.2.5)* – Admins can reload configs/messages directly through the GUI.  
- 🎨 **Refined GUIs** *(1.2.6)* –  
  - Removed all placeholder `#` tags  
  - Synced menu titles with `messages.yml`  
  - Unified Back/Exit buttons across all menus  
- 🧭 **Compass Sync** *(1.2.6)* – Unified CompassManager + GUIListener, ensuring consistent behavior.  
- 🌾 **New Claim Flag** *(1.2.6)* – Toggle **Crop Trample** in claims.  
- 🪐 **World Placeholders** *(1.2.6)* – Nether & End reserved in the World Controls menu.  
- 🛠️ **Bug Fixes** *(1.2.6)* – Fixed Admin Tools submenus (Reload, World Controls, Pending Requests).  

---

## ⚠️ Migration Notes (→ v1.2.6)

If upgrading from **v1.2.5 or earlier**, you **must regenerate configs** to sync with new features.

**What changed in 1.2.6:**  
- Back/Exit buttons finalized across all menus  
- Nether/End placeholders in World Controls  
- Crop Trample flag added  
- Expansion cooldown/expiry synced with GUI  

**Steps:**  
1. ⛔ Stop your server  
2. 📂 Backup + delete `/plugins/ProShield/`  
3. ▶️ Restart → new configs will be generated  
4. 📝 Reapply your custom changes  

---

## 🏡 Getting Started (GUI First!)

### 🔹 The ProShield Compass
- Automatically given on join *(if enabled)*  
- Or via `/proshield compass`  
- **Right-click** to open GUI:  
  - Claim land (slot 11)  
  - View Claim Info (slot 13)  
  - Unclaim (slot 15)  
  - Manage Trusted Players + Roles (slot 16)  
  - Claim Flags (slot 28)  
  - Request Expansion (slot 30)  
  - Admin Tools *(slot 32, if permitted)*  

💡 All features are accessible through **GUIs**. Commands exist, but they’re optional.

---

## 🔑 Permissions

| Node                             | Description                                      | Default |
|----------------------------------|--------------------------------------------------|---------|
| `proshield.player.access`        | Core player access (claims, compass, GUI)        | ✅ true |
| `proshield.compass`              | Receive/use the ProShield compass                | ✅ true |
| `proshield.player.claim`         | Create/manage own claims                         | ✅ true |
| `proshield.unlimited`            | Ignore max-claim limits                          | ❌ op   |
| `proshield.admin`                | Access Admin Tools GUI & commands                | ❌ op   |
| `proshield.admin.reload`         | Reload configs/messages (GUI or command)         | ❌ op   |
| `proshield.admin.debug`          | Toggle debug logging                             | ❌ op   |
| `proshield.admin.expansions`     | Review & manage claim expansions                 | ❌ op   |
| `proshield.admin.worldcontrols`  | Manage world-level protections (via GUI)         | ❌ op   |
| `proshield.admin.bypass`         | Toggle bypass protection                         | ❌ op   |

---

## 📖 Documentation
- 📜 [CHANGELOG.md](CHANGELOG.md) – Full version history  
- ❓ [FAQ.md](FAQ.md) – Common troubleshooting  

---

## 🚀 Roadmap
ProShield **2.0** (coming soon 🚧):  
- 🏘️ Towns & Shops  
- 💰 Economy Integration  
- ⚙️ Per-player permission overrides (fine-tuned via GUI)  
- 🎨 Entry/Exit claim messages  
- 🗺️ Dynmap / BlueMap overlays  
- 🌐 **Bungee Support** *(planned for 2.0 or later)*  

👉 See progress: [ROADMAP-2.0.md](ROADMAP-2.0.md)  

---

💡 *ProShield is ideal for small to medium SMP servers that want strong protection, multilingual support, and admin control — all fully managed through intuitive GUIs.*  
