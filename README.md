# ServerCore
Folia-native core plugin built for modern Minecraft servers.

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.x-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![Platform](https://img.shields.io/badge/Platform-Folia-green)
![License](https://img.shields.io/badge/License-Custom-red)
[![CodeFactor](https://www.codefactor.io/repository/github/040elias/servercore/badge)](https://www.codefactor.io/repository/github/040elias/servercore)

[Download](https://github.com/040Elias/ServerCore/releases) · [Wiki](https://github.com/040Elias/ServerCore/wiki) · [Issues](https://github.com/040Elias/ServerCore/issues)

---

**A Folia-native core plugin. No legacy baggage. No compromises.**

Most core plugins were built for Bukkit and bolted onto Folia after the fact. ServerCore was written for Folia from the ground up — every scheduler call, every thread context, every async operation is intentional. The result is a plugin that behaves correctly under Folia's region-thread model, not just one that doesn't immediately crash.

---

## Why ServerCore?

| | Typical legacy core plugins | ServerCore |
|---|---|---|
| Folia support | Incompatible or partial | Built for Folia from day one |
| Threading model | Single main thread assumed | `EntityScheduler` + `GlobalRegionScheduler` throughout |
| Codebase | Years of accumulated legacy | Written for 1.21 from scratch |
| Feature scope | Everything + kitchen sink | Focused — only what a server actually needs |
| Chat moderation | Basic or absent | Built-in, async-safe, five independent checks |
| Config reload | Varies | Full hot-reload, no restart required |

---

## Features

- **Spawns & Warps**
  Named teleport points with configurable delay, action bar countdown, and movement cancellation. Folia-safe async teleport throughout. Data persisted in YAML with dedicated async IO.

- **Chat Moderation**
  Five independent toggleable checks with per-check bypass permissions:
    - Message cooldown
    - Similarity filter (Levenshtein)
    - Uppercase cap
    - Word blacklist
    - Command cooldowns with per-command overrides

- **Staff Tools**
  Full freeze system that blocks movement and commands, persists across restarts via PDC, and notifies online staff when a frozen player disconnects or rejoins.

- **Player Tools**
  `/invsee` read-only inventory snapshot, `/ping`, `/whois` with sensitive data behind a separate permission, persistent night vision via PDC.

- **Communication**
  Private messaging with `/msg` and `/reply`, `/discord` with clickable links, `/live` stream announcements with cooldown and URL validation.

- **Gamemode Aliases**
  `/gm`, `/gmc`, `/gms`, `/gma`, `/gmsp` with per-mode permission granularity.

- **Media GUI**
  Configurable 27-slot GUI with full exploit protection (shift-click, hotbar swap, hopper transfer all blocked).

- **Maintenance**
  `/servercore reload` hot-reloads config, messages, and moderation state without a restart.

---

## Core Commands

| Command | Description |
|---|---|
| `/spawn <n>` | Teleport to a named spawn |
| `/setspawn <n>` · `/delspawn <n>` | Create or delete a spawn |
| `/warp <n>` | Teleport to a named warp |
| `/setwarp <n>` · `/delwarp <n>` | Create or delete a warp |
| `/freeze <player>` · `/unfreeze <player>` | Freeze or unfreeze a player |
| `/heal [player]` | Restore health and hunger |
| `/invsee <player>` | Read-only inventory snapshot |
| `/msg <player> <message>` · `/reply` | Private messaging |
| `/ping [player]` | Show ping |
| `/whois <player>` | Player info |
| `/gm` · `/gmc` · `/gms` · `/gma` · `/gmsp` | Gamemode aliases |
| `/servercore reload` | Hot-reload config and messages |

Full permission nodes and defaults are in the [Wiki](../../wiki).

---

## Compatibility

| |                                                      |
|---|------------------------------------------------------|
| **Minecraft** | 1.21.x                                               |
| **Platforms** | Folia (native) · Paper (may work but not supported)  |
| **Java** | 21                                                   |
| **Permissions** | LuckPerms or any Bukkit-compatible permissions plugin |

---

## Installation

1. Download the latest jar from the [Releases](https://github.com/040Elias/ServerCore/releases/) tab.
2. Drop it into your `plugins/` folder.
3. Start the server once to generate `config.yml` and `messages.yml`.
4. Configure to taste.
5. Reload live with `/servercore reload` or restart.

---

## Documentation

Full command references, permission nodes, configuration keys, and message placeholders are in the [Wiki](https://github.com/040Elias/ServerCore/wiki).

---

## License

Source-available. Commercial use, redistribution, resale, and rebranding are prohibited. See [`LICENSE.txt`](LICENSE.txt) for full terms.