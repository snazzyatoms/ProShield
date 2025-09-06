# 📜 ProShield Changelog

All notable changes to **ProShield** will be documented in this file.  
This project follows [semantic versioning](https://semver.org/).

---

## [1.1.8] - 2025-09-06
### Added
- Admin compass auto-give on join for OPs.
- Configurable container interaction protection.
- Configurable PVP toggle in claims.
- Configurable mob griefing toggle.
- Whitelist/blacklist system for interactable blocks (buttons, doors, levers, etc.).

### Changed
- Improved synchronization between `GUIManager`, `GUIListener`, and `PlotManager`.
- Updated claim storage with trusted players list.
- Stability improvements across listeners.

### Fixed
- GUI menu sync issues (open/close actions now stable).
- Fixed constructor mismatches in listeners.
- Fixed duplicate class errors and build inconsistencies.

---

## [1.1.7] - 2025-09-06
### Added
- Admin compass crafting recipe (`iron + redstone + compass`).
- Command `/proshield compass` to give the compass manually.
- OPs automatically receive compass on join if missing.

### Changed
- Reorganized file structure (`plots/`, `gui/`, etc.) to avoid symbol errors.
- Improved claim persistence system with config-based storage.

### Fixed
- Symbol/class errors during JitPack builds.
- GUI click actions not wired to `PlotManager`.

---

## [1.1.6] - 2025-09-06
### Added
- `/proshield reload` command for live config reloading.
- `/proshield info` command to display plugin and claim info.
- Config options: `full-protection-mode`, `min-gap-between-claims`.
- Admin join compass auto-give logic.

### Changed
- Polished `plugin.yml` with synced commands/permissions.
- Improved listener registration in `ProShield.java`.

### Fixed
- Config file not generating on first run.
- Inconsistent claim saving between sessions.

---

## [1.1.5] - 2025-09-05
### Added
- OPs granted full bypass permissions for testing.
- Updated to Paper API.

### Fixed
- DiscordSRV dependency errors removed (softdepend instead).
- Build failures on JitPack due to conflicting dependencies.

---

## [1.1.4] - 2025-09-05
### Added
- Java 17 build target for JitPack.

### Fixed
- Snapshot dependency conflicts.
- Initial symbol errors.

---

## [1.0.0 - 1.1.3] - 2025-09-04
- Initial development builds.
- Basic claim/unclaim system.
- Core listeners (`BlockProtectionListener`, `PlayerJoinListener`).
- Early GUI system (compass-based menu).
