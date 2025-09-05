# Changelog
All notable changes to **ProShield** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),  
and this project adheres to [Semantic Versioning](https://semver.org/).

---

## [1.1.6] - 2025-09-05
### Added
- **AdminJoinListener**: Server operators (OPs) now automatically receive the ProShield compass when they join, even if they don’t own a plot. This allows admins to preview the menu system.
- **Java 17 build support**: Locked CI/CD builds to Java 17 for consistent compilation across environments.
- **Improved directory structure**: Fixed missing `PlotManager.java` and listener class issues in CI.
- **Release automation improvements**: Updated `pom.xml` and JitPack configuration for stable Paper API dependencies.

### Fixed
- Resolved snapshot dependency errors with Paper API by switching to stable `1.20.6-R0.1`.
- Fixed multiple “cannot find symbol” errors caused by mismatched imports and missing files.
- Corrected plugin.yml so all commands and listeners register properly.

---

## [1.1.5] - 2025-08-XX
### Added
- **Command improvements**: Polished `/proshield` base command handling.
- **Internal cleanup**: Refactored listeners and managers for stability.

### Fixed
- Fixed minor issues with chat messages and color handling.
- Corrected inconsistent permission checks.

---

## [1.1.4] - 2025-07-XX
### Added
- **PlotProtectionListener**: Initial protection mechanics for plots (block breaking, placement restrictions, etc.).
- **Color-coded messages**: Added clearer feedback for players using chat color formatting.

---

## [1.1.3] - 2025-06-XX
### Added
- **ProShieldCommand.java**: Introduced base command handling for plugin interaction.

---

## [1.1.2] - 2025-05-XX
### Added
- **ProShield.java**: Main plugin class with event registration and core setup.

---

## [1.1.1] - 2025-04-XX
### Added
- **plugin.yml**: Basic plugin configuration (name, version, commands).
- **Initial repository setup**.

---

## [1.1.0] - 2025-04-XX
### Added
- First public release of **ProShield**.
- Basic framework for future plot protection and admin tools.
