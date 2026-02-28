# ServerCore

ServerCore is a Folia-focused core plugin that provides common server utility features such as spawns, moderation tools, messaging, and configurable chat formatting. 
It is built for Minecraft 1.21.x and uses Folia-safe scheduling patterns.

## Requirements

- Minecraft: 1.21.11
- Server: Folia
- Java: 21

## Features

### Utilities
- Named spawns:
  - `/spawn [name]` with configurable teleport delay and cooldown
  - Movement-cancel while teleporting
  - `/setspawn <name>` and `/delspawn <name>`
  - Spawns are stored in `plugins/ServerCore/data/spawns.yml`
- `/invsee <player>`:
  - Read-only inventory snapshot GUI
  - Click and drag are blocked
- `/ping [player]`

### Communication
- Private messages:
  - `/msg <player> <message>`
  - `/reply <message>` (`/r`)
- `/discord`:
  - Configurable multi-line output
  - Links become clickable automatically
- `/live <url>`:
  - Validates http/https URLs
  - Configurable cooldown
  - Broadcasts clickable stream link messages to all online players

### Moderation
- Freeze system:
  - `/freeze <player>` and `/unfreeze <player>`
  - Prevents movement and command usage
  - Staff notifications if a frozen player leaves/rejoins
  - Frozen state persists via Persistent Data Container (PDC)
- Night vision toggle:
  - `/nightvision` (`/nv`)
  - Persists via PDC and is reapplied on join/respawn

### Chat Formatting
- Custom join/leave messages (vanilla messages suppressed)
- Death message formatting:
  - Configurable prefix, message color, and suffix

### Maintenance
- `/servercore reload` reloads `config.yml` and `messages.yml`

## Installation

1. Download the latest jar from the Releases Tab.
2. Put the jar into your server `plugins/` folder.
3. Start the server once to generate default files.
4. Configure `config.yml` and `messages.yml`.
5. Restart the server (recommended). Alternatively use `/servercore reload`.

## Commands

| Command | Description | Permission |
|--------|-------------|------------|
| `/spawn [name]` | Teleport to a spawn (uses default spawn if omitted) | `servercore.spawn.use` |
| `/setspawn <name>` | Create a spawn at your location | `servercore.spawn.setspawn` |
| `/delspawn <name>` | Delete a spawn | `servercore.spawn.delspawn` |
| `/invsee <player>` | Open a read-only inventory snapshot | `servercore.invsee` |
| `/broadcast <message>` | Broadcast a title + chat message | `servercore.broadcast` |
| `/nightvision` (`/nv`) | Toggle night vision | `servercore.nightvision` |
| `/ping [player]` | Show ping | `servercore.ping` (`servercore.ping.other` for others) |
| `/freeze <player>` | Freeze a player | `servercore.freeze` |
| `/unfreeze <player>` | Unfreeze a player | `servercore.unfreeze` |
| `/whois <player>` | Show player info | `servercore.whois.use` (`servercore.whois.sensitive` for sensitive info) |
| `/msg <player> <message>` | Send a private message | `servercore.msg` |
| `/reply <message>` (`/r`) | Reply to the last message partner | `servercore.msg` |
| `/discord` | Show the Discord information message | `servercore.discord` |
| `/live <url>` | Broadcast a live/stream link | `servercore.live` |
| `/servercore reload` | Reload configuration | `servercore.reload` |

## Configuration

### config.yml

- `global.error-sound`: sound played for error feedback
- `spawn.teleport-delay-seconds`: teleport countdown duration
- `spawn.cooldown-seconds`: cooldown for `/spawn`
- `spawn.teleporting-sound`: sound during teleport countdown
- `spawn.default-spawn`: default spawn name for `/spawn`
- `invsee.title`: GUI title for invsee
- `broadcast.sound`: broadcast sound
- `discord.sound`: sound played when using `/discord`
- `discord.lines`: list of lines printed by `/discord`
- `live.cooldown-minutes`: cooldown for `/live`
- `live.sound`: sound played for `/live` broadcasts
- `live.lines`: broadcast message lines (supports `%player%` and `%link%`)
- `death.prefix`: prefix for death messages (empty to disable)
- `death.message-color`: color applied to the vanilla death message (empty to keep vanilla)
- `death.suffix`: suffix for death messages

### messages.yml

All user-facing messages can be customized in `messages.yml`.  
The plugin supports `&` color codes and hex colors.

## License

This repository is source-available.

Commercial use, redistribution, resale, or rebranding of this software or any modified versions is strictly prohibited.  
See `LICENSE.txt` for full terms.
