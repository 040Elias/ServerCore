# ServerCore

ServerCore is a Folia-focused core plugin that provides common server utility features such as spawns, moderation tools, messaging, gamemode management, and configurable chat formatting.  
Built for Minecraft 1.21.x with Folia-safe scheduling throughout.

## Requirements

- Minecraft: 1.21.x
- Server: Folia (Paper-compatible)
- Java: 21

## Features

### Utilities
- Named spawns:
    - `/spawn [name]` with configurable teleport delay and cooldown
    - Movement-cancel while teleporting
    - `/setspawn <n>` and `/delspawn <n>`
    - Spawns are stored in `plugins/ServerCore/data/spawns.yml`
- `/invsee <player>` — read-only inventory snapshot GUI (clicks, drags blocked)
- `/ping [player]`

### Gamemode Aliases
- `/gm <mode> [player]` — generic alias supporting `0/1/2/3`, `survival/creative/adventure/spectator`
- `/gmc [player]`, `/gms [player]`, `/gma [player]`, `/gmsp [player]` — quick aliases
- No vanilla gamemode message; fully custom messages via `messages.yml`
- Targeting another player requires `servercore.gamemode.other`

### Media GUI
- `/media` — opens a 27-slot (3-row) inventory GUI
- Configurable action item in the centre slot (slot 13)
- All other slots filled with a configurable filler item (optional)
- Fully exploit-proof: clicks, drags, shift-clicks, number-key swaps, hopper transfers and drops are all blocked
- Action item executes a configurable command on click (as player or as console)
- Supports `{player}` placeholder in the command
- Leather armor items can be given a custom dye color via `media.item.leather-color`

### Communication
- Private messages: `/msg <player> <message>` and `/reply <message>` (`/r`)
- `/discord` — configurable multi-line output, links become clickable automatically
- `/live <url>` — validates http/https URLs, configurable cooldown, broadcasts clickable stream link to all players

### Moderation
- Freeze system:
    - `/freeze <player>` and `/unfreeze <player>`
    - Prevents movement and command usage while frozen
    - Staff notifications on frozen player leave/rejoin
    - Frozen state persists via Persistent Data Container (PDC) across restarts
- Night vision toggle: `/nightvision` (`/nv`) — persists via PDC, reapplied on join/respawn
- `/whois <player>` — shows UUID, join dates, playtime, ping; IP/client/location gated behind `servercore.whois.sensitive`

### Chat Formatting
- Custom join/leave/first-join messages (vanilla messages suppressed)
- Death message formatting: configurable prefix, message color, and suffix

### Maintenance
- `/servercore reload` — hot-reloads `config.yml` and `messages.yml` without restart

---

## Installation

1. Download the latest jar from the Releases tab.
2. Drop it into your server's `plugins/` folder.
3. Start the server once to generate default config files.
4. Edit `plugins/ServerCore/config.yml` and `messages.yml` to your liking.
5. Restart or run `/servercore reload`.

---

## Commands & Permissions

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/spawn [name]` | Teleport to a spawn | `servercore.spawn.use` | everyone |
| `/setspawn <n>` | Create a named spawn | `servercore.spawn.setspawn` | op |
| `/delspawn <n>` | Delete a named spawn | `servercore.spawn.delspawn` | op |
| `/invsee <player>` | Read-only inventory snapshot | `servercore.invsee` | op |
| `/broadcast <message>` | Broadcast title + chat message | `servercore.broadcast` | op |
| `/nightvision` (`/nv`) | Toggle night vision | `servercore.nightvision` | op |
| `/ping [player]` | Show own ping | `servercore.ping` | everyone |
| `/ping <player>` | Show another player's ping | `servercore.ping.other` | op |
| `/freeze <player>` | Freeze a player | `servercore.freeze` | op |
| `/unfreeze <player>` | Unfreeze a player | `servercore.unfreeze` | op |
| `/whois <player>` | Basic player info | `servercore.whois.use` | op |
| `/whois <player>` | + IP / client / location | `servercore.whois.sensitive` | op |
| `/msg <player> <message>` | Private message | `servercore.msg` | everyone |
| `/reply <message>` (`/r`) | Reply to last partner | `servercore.msg` | everyone |
| `/discord` | Show Discord info | `servercore.discord` | everyone |
| `/live <url>` | Broadcast a stream link | `servercore.live` | op |
| `/gm <mode> [player]` | Generic gamemode alias | `servercore.gamemode` | op |
| `/gmc [player]` | Switch to Creative | `servercore.gamemode` | op |
| `/gms [player]` | Switch to Survival | `servercore.gamemode` | op |
| `/gma [player]` | Switch to Adventure | `servercore.gamemode` | op |
| `/gmsp [player]` | Switch to Spectator | `servercore.gamemode` | op |
| `/gm* <player>` | Target another player | `servercore.gamemode.other` | op |
| `/media` | Open the Media GUI | `servercore.media` | everyone |
| `/servercore reload` | Reload configs | `servercore.reload` | op |

### Additional Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `servercore.gamemode.survival` | Allow only Survival mode | op |
| `servercore.gamemode.creative` | Allow only Creative mode | op |
| `servercore.gamemode.adventure` | Allow only Adventure mode | op |
| `servercore.gamemode.spectator` | Allow only Spectator mode | op |
| `servercore.freeze.bypass` | Use commands while frozen | false |
| `servercore.freeze.notify` | Receive staff freeze alerts | op |
| `servercore.join.silent` | Join/leave without message | false |

---

## Configuration

### config.yml

#### Global
```yaml
global:
  error-sound: "ENTITY.VILLAGER.NO"
```

#### Spawn
```yaml
spawn:
  teleport-delay-seconds: 5
  cooldown-seconds: 10
  teleporting-sound: "BLOCK.NOTE_BLOCK.PLING"
  default-spawn: "1"
```

#### InvSee
```yaml
invsee:
  title: "&fInvSee: &#38c1fc%target%"   # %target% = player name
```

#### Broadcast / Discord / Live
```yaml
broadcast:
  sound: "BLOCK.NOTE_BLOCK.BELL"

discord:
  sound: "BLOCK.NOTE_BLOCK.PLING"
  lines:
    - "&#38c1fcJoin our community"
    - "https://discord.gg/yourserver"   # URLs become clickable automatically

live:
  cooldown-minutes: 15
  sound: "BLOCK.NOTE_BLOCK.BELL"
  lines:
    - "  &#38c1fc&l%player% is now live!"
    - "  &fWatch: %link%"               # %player% and %link% are supported
```

#### Death Messages
```yaml
death:
  prefix: "&#ff0000☠ "
  message-color: "&#ff0000"
  suffix: "&#ff0000."
```

#### Media GUI
```yaml
media:
  title: "&#38c1fc&lMedia"

  filler:
    enabled: true
    material: "GRAY_STAINED_GLASS_PANE"
    name: " "
    lore: []

  item:
    material: "LEATHER_HELMET"
    name: "&#38c1fc&lClick here!"
    lore:
      - ""
      - "&7Visit our social media"
    # Hex dye color for leather armor items — ignored for non-leather materials.
    # Accepted formats:  "#RRGGBB"  |  "RRGGBB"  |  "&#RRGGBB"
    leather-color: "#FF4400"
    # Command run when the item is clicked. {player} = clicking player's name.
    command: "discord"
    # true = dispatch as console, false = player executes the command
    runAsConsole: false
```

> **Leather armor — supported materials:**  
> `LEATHER_HELMET`, `LEATHER_CHESTPLATE`, `LEATHER_LEGGINGS`, `LEATHER_BOOTS`, `LEATHER_HORSE_ARMOR`
>
> Example — dyed orange Leather Cap that opens a stream link:
> ```yaml
> media:
>   item:
>     material: "LEATHER_HELMET"
>     name: "&6&lWatch us live!"
>     leather-color: "#FF6A00"
>     command: "live https://twitch.tv/yourchannel"
>     runAsConsole: false
> ```

---

### messages.yml

All user-facing messages live in `messages.yml`.  
Supports `&` color codes and hex colors (`&#RRGGBB`).

| Key | Placeholders | Description |
|-----|-------------|-------------|
| `no-permission` | — | No permission message |
| `only-players` | — | Console-only-players guard |
| `player-not-found` | — | Target player not online |
| `spawn-teleport-success` | `%spawn_name%` | After successful teleport |
| `spawn-teleport-actionbar` | `%spawn_name%`, `%spawn_teleport_time_remaining%` | Countdown action bar |
| `spawn-move` | `%spawn_name%` | Teleport cancelled due to movement |
| `cooldown-active` | `%cooldown_remaining%` | Spawn cooldown still active |
| `gamemode.self` | `%player%`, `%gamemode%` | Sent to the player whose mode changed |
| `gamemode.other` | `%player%`, `%gamemode%`, `%sender%` | Sent to the admin who changed it |
| `msg-sender` | `%receiver%`, `%message%` | Private message — sender side |
| `msg-receiver` | `%sender%`, `%message%` | Private message — receiver side |
| `live-cooldown` | `%remaining%` | `/live` on cooldown |
| `ping-self` | `%ping%` | Own ping |
| `ping-other` | `%player%`, `%ping%` | Another player's ping |
| `freeze-frozen` | `%player%` | Freeze confirmation |
| `freeze-unfrozen` | `%player%` | Unfreeze confirmation |
| `join` | `%player%` | Join message |
| `leave` | `%player%` | Leave message |
| `first-join` | `%player%` | First-time join message |

---

## Folia Notes

All entity/world operations are dispatched on the correct scheduler:

| Operation | Scheduler used |
|-----------|---------------|
| Gamemode change, inventory open, sound | `player.getScheduler().run()` |
| Teleport countdown | `player.getScheduler().runAtFixedRate()` |
| Join/leave broadcast, freeze staff alerts | `getServer().getGlobalRegionScheduler().execute()` |
| NightVision death poller | `getGlobalRegionScheduler().runAtFixedRate()` |
| Media GUI console command dispatch | `getGlobalRegionScheduler().execute()` |
| Spawn file I/O | Dedicated `ExecutorService`, flushed on `onDisable()` |

---

## License

This repository is source-available.

Commercial use, redistribution, resale, or rebranding of this software or any modified versions is strictly prohibited.  
See `LICENSE.txt` for full terms.