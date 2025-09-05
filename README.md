# 🛡️ ProShield  
*A lightweight land protection plugin for Paper/Spigot servers.*

![Build Status](https://jitpack.io/v/snazzyatoms/ProShield.svg)  
[![](https://img.shields.io/badge/Java-17+-blue.svg)](https://adoptium.net/)  
[![](https://img.shields.io/badge/MC-Paper%20%2F%20Spigot%201.20+-brightgreen.svg)]()  

---

## ✨ Features
- ✅ **Claim Protection** – Prevent griefing by claiming plots.  
- ✅ **Admin Compass** – Operators receive a special compass on join to test menus.  
- ✅ **Interactive GUI** – Easy-to-use menu for managing plots.  
- ✅ **Permissions System** – Works with Bukkit’s native system and supports permission managers (LuckPerms, PermissionsEx, etc.).  
- ✅ **Reload Command** – Quickly reload configuration without restarting.  
- ✅ **Info Command** – View plugin version & basic information.  
- ✅ **Configurable** – Automatic `ProShield/` folder with editable `.yml` configs.  

---

## 📥 Installation
1. Download the latest release from the [Releases page](https://github.com/snazzyatoms/ProShield/releases).  
2. Place the `.jar` in your server’s `plugins/` folder.  
3. Restart your server.  
4. Configure settings in the new `plugins/ProShield/` folder.  

---

## ⚙️ Commands
| Command | Description |
|---------|-------------|
| `/proshield` | Base command, shows help. |
| `/proshield reload` | Reloads the plugin config. |
| `/proshield info` | Displays plugin info. |

---

## 🔑 Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `proshield.*` | Grants access to all ProShield commands | OP |
| `proshield.use` | Allows using basic commands (claim, manage plots, etc.) | true |
| `proshield.admin` | Grants admin-level commands (reload, reset, bypass, etc.) | OP |

---

## 📜 Changelog  

### v1.1.6 (Latest)
- Added `/proshield reload` command.  
- Added `/proshield info` command.  
- Operators automatically receive a **ProShield Compass** on join.  
- Config system improvements – config files auto-generate in `plugins/ProShield/`.  
- Improved error handling & stability fixes.  
- Unified branding and consistent plugin.yml.  

### v1.1.5
- Added OP-level permission fallback (server operators get full access).  
- Minor bug fixes and internal improvements.  

---

## 🏗️ Build with JitPack
If you want to include ProShield as a dependency in your own project:

```xml
<repository>
  <id>jitpack.io</id>
  <url>https://jitpack.io</url>
</repository>

<dependency>
  <groupId>com.github.snazzyatoms</groupId>
  <artifactId>ProShield</artifactId>
  <version>1.1.6</version>
</dependency>

[![](https://jitpack.io/v/snazzyatoms/ProShield.svg)](https://jitpack.io/#snazzyatoms/ProShield)

