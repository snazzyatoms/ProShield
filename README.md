🛡️ ProShield

ProShield is a lightweight land and plot protection plugin for Spigot & Paper servers.
Perfect for survival or SMP communities, it provides simple claiming, intuitive GUIs, and powerful admin tools without the bloat.

✨ Features

✅ Land Claiming – Players can protect chunks with one command or the ProShield menu.

✅ Unclaiming – Release land instantly when no longer needed.

✅ Protection System – Prevents griefing (block break/place, containers, interactions, fire spread, TNT/creeper explosions, entity grief).

✅ Trust System – Owners can /proshield trust <player> to allow friends access.

✅ Claim Roles (since 1.2.1) – Define granular access levels: Visitor, Member, Container, Builder, Moderator, Manager, Owner.

✅ Keep Items in Claims (1.2.2+) – Optional toggle to prevent items dropped in claims from despawning (configurable, off by default).

✅ Claim Ownership Transfer (1.2.3) – Owners can hand over claims to another player.

✅ Claim Borders Preview (1.2.3) – Visualize claim boundaries before confirming.

✅ GUI Menu – Clean and simple inventory menu for claiming, info, unclaiming, flags, roles, expansions, and admin tools.

✅ Back & Exit Buttons in GUIs (IMPROVED 1.2.5) – Consistent navigation across all menus.

✅ Admin Compass – Special compass item that opens the ProShield GUI. Auto-given to ops (configurable).

✅ Claim Expiry – Automatically removes claims of inactive players (optional, fully configurable).

✅ Configurable Protections – Containers, doors, buttons, buckets, fire, mobs, explosions, Enderman teleport, and more.

✅ Spawn Guard (1.2.4) – Block claiming within a configurable radius around world spawn.

✅ Mob Control (ENHANCED 1.2.5) – Repel mobs from claim borders, despawn hostile mobs inside claims, and block mob pathing/targeting in safezones.

✅ Expansion Requests (1.2.5) – Players can request claim expansions, admins approve/deny via GUI.

✅ Admin Expansion Review (1.2.5) – Approve or deny with configurable deny reasons.

✅ Expansion History (1.2.5) – Paginated GUI to view past requests and decisions.

✅ World Controls (1.2.5) – Admin GUI to toggle global protections like fire, explosions, or mob damage.

✅ Admin Reload & Debug Tools (IMPROVED 1.2.5) – Reload configs, messages, and expansions via GUI or commands.

✅ Refined GUIs (NEW in 1.2.6) –

Removed all placeholder #-tags from items and lore.

All menus polished with proper titles synced to messages.yml.

Back/Exit buttons fully consistent across all menus.

✅ Compass Sync (1.2.6) – Unified CompassManager + GUIListener handling.

✅ Bug Fixes (1.2.6) – Fixed Admin Tools submenus (Reload Config, World Controls, Expansion Requests) not opening.

✅ New Claim Flags (1.2.6) – Crop Trample toggle added to GUI and configs.

✅ World Placeholders (1.2.6) – Nether & End added to world controls as “coming soon” placeholders.

✅ Lightweight & Polished – Version 1.2.6 finalizes all GUI navigation, config syncing, and message handling.

⚠️ Migration Note (v1.2.6)

If you are upgrading from v1.2.5 or earlier, you must regenerate your ProShield config and messages files.
This ensures proper syncing for:

GUI navigation (Back/Exit)

World Controls placeholders (Nether/End)

Expansion Requests and cooldown/expiry

Crop Trample claim flag

Steps:

Stop your server.

Backup and delete the /plugins/ProShield/ folder.

Restart the server → new configs & messages.yml will be generated.

Reapply any custom changes you had made.

🏡 Getting Started: How to Claim Land

Players can claim land in two ways:

🔹 Option 1: The ProShield Compass (GUI)

Operators/admins automatically receive it on join.

Players with permissions can get one via /proshield compass.

Right-click to open the menu:

Slot 11 → Claim current chunk

Slot 13 → View claim info (owner, coords, radius, flags)

Slot 15 → Unclaim chunk

Slot 16 → Trusted Players (manage roles)

Slot 28 → Claim Flags

Slot 30 → Request Expansion (if enabled)

Slot 32 → Admin Tools (for players with admin perms)

🔹 Option 2: Commands

/proshield claim – Claim your current chunk

/proshield unclaim – Remove your claim

/proshield info – View claim info (owner, trusted players)

/proshield trust <player> [role] – Grant access with a role (Visitor/Member/Builder/etc.)

/proshield untrust <player> – Remove access

/proshield trusted – List trusted players

⚡ Tip: Use the Compass GUI for ease, or commands for precision.

⌨️ Commands

/proshield – Main command + help

/proshield claim – Claim your current chunk

/proshield unclaim – Remove your claim

/proshield info – Show claim info (owner, trusted players)

/proshield trust <player> [role] – Grant access to a player with optional role

/proshield untrust <player> – Remove trust

/proshield trusted – List trusted players

/proshield compass – Give yourself the ProShield compass

/proshield bypass – Toggle admin bypass

/proshield reload – Reload configuration + messages + expansions

/proshield debug – Toggle debug logging

/proshield admin – Open Admin Tools GUI (reload, expansions, world controls)

🔑 Permissions
Node	Description	Default
proshield.player.access	Access player features (claims, compass, GUI)	✅ true
proshield.admin	Access admin tools (compass, bypass, force unclaim, purge)	❌ op
proshield.compass	Receive/use ProShield compass	✅ true
proshield.bypass	Toggle bypass protection	❌ op
proshield.unlimited	Ignore max-claims limit	❌ op
proshield.admin.reload	Use /proshield reload	❌ op
proshield.admin.debug	Toggle debug logging	❌ op
proshield.admin.expansions	Review/approve/deny expansion requests	❌ op
proshield.admin.worldcontrols	Manage world control toggles (fire, explosions, mobs)	❌ op
