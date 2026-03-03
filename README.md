# ServerCore

Most core plugins were built for Bukkit. ServerCore was built for **Folia**.

Plugins like EssentialsX were designed around a single main thread — an assumption that is fundamentally incompatible with Folia's region-thread model. Adapting legacy code to that constraint is non-trivial, and the gaps tend to surface under load rather than in testing.

ServerCore is designed for **performance-oriented Folia networks** that prefer minimal, predictable core utilities over feature-heavy monoliths. Every scheduling decision is explicit, every thread context is intentional, and the feature set is limited to what a server actually needs.

---

## Why ServerCore?

| | Legacy core plugins | ServerCore                                         |
|---|---|----------------------------------------------------| 
| Folia support | Incompatible or partial | Built for Folia from the start                     |
| Threading model | Single main thread assumed | EntityScheduler + GlobalRegionScheduler throughout |
| Codebase | Years of accumulated legacy | Written for 1.21.11 from scratch                   |
| Feature scope | Everything + kitchen sink | Focused — only what a server actually needs        |
| Chat moderation | Basic or absent | Built-in, async-safe, configurable per-check       |
| Config reload | Varies | Full hot-reload including moderation state         |

---

## Compatibility

| |                                                   |
|---|---------------------------------------------------|
| **Minecraft** | 1.21.11                                           |
| **Server software** | Folia · Paper (compatible)                        |
| **Java** | 21+                                               |
| **Permission plugins** | LuckPerms or almost any Bukkit permissions Plugin |

---

## What's included

### Spawns
Named spawn points with configurable teleport delay, movement cancellation, and per-player cooldowns. Multiple spawns supported out of the box — `/spawn lobby`, `/spawn pvp`, etc.

### Warps
Named warp points with the same UX as spawns — configurable teleport delay, movement cancellation, per-player cooldowns, and action bar countdown. Warps are stored separately in `data/warps.yml` and can be created, overwritten, or deleted at runtime. `/warp` with no argument falls back to the `warp.default-warp` config key.

### Chat Moderation
Five independent, toggleable checks — each with its own bypass permission:

- **Message cooldown** — minimum interval between chat messages per player
- **Similarity filter** — normalized Levenshtein comparison blocks near-identical repeat messages, with a length-diff early exit to prevent async thread saturation
- **Uppercase limit** — percentage-based, letter-only (emojis and numbers ignored)
- **Word blacklist** — pre-compiled `CONTAINS` or `WHOLE_WORD` matching, case-insensitive option
- **Command cooldowns** — global baseline with per-command overrides; namespace-stripped so `/minecraft:spawn` and `/spawn` share the same cooldown bucket

All checks run in the correct thread context. Chat checks run on the async chat thread with no entity access. Command checks are re-dispatched onto the player's entity thread before any permission lookup.

### Gamemode Aliases
`/gm`, `/gmc`, `/gms`, `/gma`, `/gmsp` with per-mode permission granularity and full custom messaging.

### Player Tools
`/invsee` read-only inventory snapshot, `/ping`, `/whois` with sensitive data gated behind a separate permission, night vision toggle persisted via PDC.

### Communication
Private messaging (`/msg`, `/reply`), `/discord` with clickable links, `/live` stream announcements with cooldown and URL validation.

### Staff Tools
Full freeze system — blocks movement and commands, persists across restarts via PDC, notifies online staff on frozen player leave/rejoin.

### Media GUI
Configurable 27-slot GUI with a centred action item, optional filler, leather armor dye color support, and full exploit protection (shift-click, hotbar swap, hopper transfer all blocked).

### Maintenance
`/servercore reload` hot-reloads `config.yml`, `messages.yml`, and all moderation state without a restart.

---

## Thread Safety

Every operation is dispatched on the scheduler that owns it:

| Operation | Scheduler |
|-----------|-----------|
| Player messages, sounds, titles, inventory | `player.getScheduler().run()` |
| Teleport countdown ticks | `player.getScheduler().runAtFixedRate()` |
| Server-wide broadcasts, freeze staff alerts | `getGlobalRegionScheduler().execute()` |
| Async chat moderation checks | Native async — no entity access |
| Command cooldown permission checks | Re-dispatched to entity thread before lookup |
| Spawn file I/O | Dedicated `ExecutorService`, flushed on shutdown |

The distinction matters: claiming "Folia compatible" is easy. Correctly separating entity-thread operations from global-region operations, and handling async events without unsafe entity access, is the actual work.

---

## Installation

1. Download the latest jar from the Releases tab.
2. Drop it into `plugins/`.
3. Start the server once to generate config files.
4. Edit `plugins/ServerCore/config.yml` and `messages.yml`.
5. Reload live with `/servercore reload` or restart.

---

## Commands & Permissions

| Command                      | Description                       | Permission | Default |
|------------------------------|-----------------------------------|------------|---------|
| `/spawn <n>`                 | Teleport to a spawn               | `servercore.spawn.use` | everyone |
| `/setspawn <n>`              | Create a named spawn              | `servercore.spawn.setspawn` | op |
| `/delspawn <n>`              | Delete a named spawn              | `servercore.spawn.delspawn` | op |
| `/warp <n>`                  | Teleport to a warp                | `servercore.warp.use` | everyone |
| `/setwarp <n>`               | Create or overwrite a named warp  | `servercore.warp.setwarp` | op |
| `/delwarp <n>`               | Delete a named warp               | `servercore.warp.delwarp` | op |
| `/invsee <player>`           | Read-only inventory snapshot      | `servercore.invsee` | op |
| `/broadcast <message>`       | Broadcast title + chat message    | `servercore.broadcast` | op |
| `/nightvision` (`/nv`)       | Toggle night vision               | `servercore.nightvision` | op |
| `/ping [player]`             | Show own or another player's ping | `servercore.ping` / `servercore.ping.other` | everyone / op |
| `/freeze <player>`           | Freeze a player                   | `servercore.freeze` | op |
| `/unfreeze <player>`         | Unfreeze a player                 | `servercore.unfreeze` | op |
| `/whois <player>`            | Player info                       | `servercore.whois.use` | op |
| `/msg <player> <message>`    | Private message                   | `servercore.msg` | everyone |
| `/reply <message>` (`/r`)    | Reply to last DM                  | `servercore.msg` | everyone |
| `/discord`                   | Show Discord info                 | `servercore.discord` | everyone |
| `/live <url>`                | Broadcast stream link             | `servercore.live` | op |
| `/gm <mode> [player]`        | Gamemode alias                    | `servercore.gamemode` | op |
| `/gmc` `/gms` `/gma` `/gmsp` | Quick gamemode shortcuts          | `servercore.gamemode` | op |
| `/media`                     | Open Media GUI                    | `servercore.media` | everyone |
| `/servercore <subcommand>`   | Acces to Main Command             | `servercore.use` | op |

### Moderation Bypass Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `servercore.moderation.bypass.cooldown` | Bypass chat message cooldown | op |
| `servercore.moderation.bypass.similarity` | Bypass repeat message filter | op |
| `servercore.moderation.bypass.uppercase` | Bypass uppercase filter | op |
| `servercore.moderation.bypass.blacklist` | Bypass word blacklist | op |
| `servercore.moderation.bypass.command` | Bypass all command cooldowns | op |

### Other Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `servercore.freeze.bypass` | Use commands while frozen | false |
| `servercore.freeze.notify` | Receive staff freeze alerts | op |
| `servercore.join.silent` | Join/leave silently | false |
| `servercore.gamemode.survival/creative/adventure/spectator` | Per-mode granularity | op |
| `servercore.gamemode.other` | Change another player's gamemode | op |
| `servercore.whois.sensitive` | See IP, client, location in /whois | op |

---

## Configuration

### config.yml

#### Chat Moderation
```yaml
moderation:

  chat-cooldown:
    enabled: true
    cooldown-millis: 1000                          # Minimum ms between chat messages
    bypass-permission: "servercore.moderation.bypass.cooldown"

  similarity:
    enabled: true
    threshold: 0.85                                # 0.0–1.0, higher = stricter
    max-compare-length: 100                        # String cap for performance
    time-window-millis: 30000                      # Only compare within this window
    bypass-permission: "servercore.moderation.bypass.similarity"

  uppercase:
    enabled: true
    max-uppercase-percent: 70                      # Letters only, ignores numbers/emoji
    min-letters-to-check: 8                        # Minimum letters before check applies
    bypass-permission: "servercore.moderation.bypass.uppercase"

  blacklist:
    enabled: true
    match-mode: "CONTAINS"                         # CONTAINS | WHOLE_WORD
    ignore-case: true
    bypass-permission: "servercore.moderation.bypass.blacklist"
    words:
      - "badword1"
      - "badword2"

  command-cooldown:
    bypass-permission: "servercore.moderation.bypass.command"
    global:
      enabled: true
      cooldown-millis: 500
    per-command:                                   # Overrides global, key = command name (no slash)
      spawn: 10000
      msg: 2000
      reply: 2000
```

#### Spawn
```yaml
spawn:
  teleport-delay-seconds: 5
  teleporting-sound: "BLOCK.NOTE_BLOCK.PLING"
```

#### Warp
```yaml
warp:
  teleport-delay-seconds: 5
  teleporting-sound: "BLOCK.NOTE_BLOCK.PLING"
```

#### Other
```yaml
global:
  error-sound: "ENTITY.VILLAGER.NO"
  teleport-success-sound: "ENTITY.PLAYER.LEVELUP"
```

broadcast:
sound: "BLOCK.NOTE_BLOCK.BELL"

discord:
sound: "BLOCK.NOTE_BLOCK.PLING"
lines:
- "&#38c1fcJoin our community"
- "https://discord.gg/yourserver"

live:
cooldown-minutes: 15
sound: "BLOCK.NOTE_BLOCK.BELL"
lines:
- "  &#38c1fc&l%player% is now live!"
- "  &fWatch: %link%"

death:
prefix: "&#ff0000☠ "
message-color: "&#ff0000"
suffix: "&#ff0000."

invsee:
title: "&fInvSee: &#38c1fc%target%"
```

---

### messages.yml

Supports `&` color codes and `&#RRGGBB` hex colors throughout.

| Key | Placeholders | Description |
|-----|-------------|-------------|
| `moderation-chat-cooldown` | `%remaining_time%` (seconds) | Chat cooldown feedback |
| `moderation-similarity` | — | Repeat message blocked |
| `moderation-uppercase` | — | Too many caps |
| `moderation-blacklist` | — | Blacklisted word detected |
| `moderation-command-cooldown` | `%time%` (seconds) | Command cooldown feedback |
| `spawn-teleport-success` | `%spawn_name%` | Teleport success |
| `spawn-teleport-actionbar` | `%spawn_name%`, `%spawn_teleport_time_remaining%` | Countdown action bar |
| `warp-teleport-success` | `%warp_name%` | Warp teleport success |
| `warp-teleport-actionbar` | `%warp_teleport_time_remaining%` | Warp countdown action bar |
| `warp-not-found` | — | Warp name not found |
| `warp-move` | — | Warp cancelled — player moved |
| `warp-already-exists` | `%warp_name%` | Warp name already taken |
| `setwarp-success` | `%warp_name%` | Warp created |
| `delwarp-success` | `%warp_name%` | Warp deleted |
| `gamemode.self` | `%player%`, `%gamemode%` | Own gamemode changed |
| `gamemode.other` | `%player%`, `%gamemode%` | Another player's gamemode changed |
| `msg-sender` | `%receiver%`, `%message%` | Private message — sender |
| `msg-receiver` | `%sender%`, `%message%` | Private message — receiver |
| `ping-self` | `%ping%` | Own ping |
| `ping-other` | `%player%`, `%ping%` | Another player's ping |
| `freeze-frozen` | `%player%` | Freeze confirmation |
| `freeze-unfrozen` | `%player%` | Unfreeze confirmation |
| `join` | `%player%` | Join message |
| `leave` | `%player%` | Leave message |
| `first-join` | `%player%` | First join message |
| `live-cooldown` | `%remaining%` | `/live` cooldown |

---

## License

This repository is source-available.

Commercial use, redistribution, resale, or rebranding of this software or any modified versions is strictly prohibited.  
See `LICENSE.txt` for full terms.