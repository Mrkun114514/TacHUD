# TacHUD — Call of Duty style military HUD for Minecraft

A tactical, Call-of-Duty–flavored HUD overlay built with **Architectury**, so a single
codebase ships on both **Fabric** and **NeoForge** with identical behavior.

Reference target: **Minecraft 1.21.1 / Java 21**. See [PORTING.md](PORTING.md) for the
1.20 – 1.21.x version matrix and how to build the whole range.

---

## ✨ Features

| Feature | Where | Behavior |
|---|---|---|
| **Low-health warning** | screen edges | Pulsing red vignette when health ≤ threshold (default **8 HP / 4 hearts**). Intensity rises as you approach death; disappears instantly when you heal above the threshold. |
| **Kill feed** | top-right | Pop-up when *you* kill a player or entity, showing the victim's name (MW "+100" style, no score). Fade-in/hold/fade-out. Kills by non-players are never shown. Player vs. mob kills are color-coded. |
| **Ammo / durability** | bottom-left | Held bow/crossbow/projectile weapon → arrows remaining. Damageable tool/weapon → durability %, plus raw `remaining / max`. Turns red when low. |
| **Tactical compass** *(extra)* | top-center | Scrolling heading strip with N/E/S/W + intercardinals, degree ticks and a numeric bearing readout. |
| **Hitmarker** *(extra)* | crosshair | The iconic expanding "✕" flashes on every landed hit; a distinct color marks the killing blow. |

Everything is individually toggleable and repositionable via the config file.

---

## 🎮 Installation

1. Install **Java 21+**, the loader (Fabric or NeoForge) for **MC 1.21.1**, and:
   - Fabric: **Fabric API** + **Architectury API**
   - NeoForge: **Architectury API**
2. Drop `tachud-fabric-<version>.jar` **or** `tachud-neoforge-<version>.jar` into `mods/`.
3. For the kill feed & hitmarker in multiplayer, the mod must be installed on the **server**
   too (they are server-authoritative). Singleplayer works out of the box.

---

## ⚙️ Configuration

A file is generated at `config/tachud.json` on first launch. Edit it and press the
**Reload TacHUD Config** keybind (unbound by default — set it in Controls) to apply changes
live, no restart required.

```jsonc
{
  "masterEnabled": true,
  "lowHealth": {
    "enabled": true,
    "thresholdHp": 8.0,        // trigger at/below this many HP (2 HP = 1 heart)
    "depthFraction": 0.18,     // how far the vignette bleeds in
    "pulseSpeed": 1.0,
    "maxOpacity": 0.85,
    "color": "#FFFF2A25"       // ARGB hex
  },
  "killFeed": {
    "enabled": true,
    "marginX": 6, "marginY": 6,
    "showMobKills": true,      // false = only show player kills
    "maxEntries": 4,
    "holdMs": 2600, "fadeMs": 320,
    "playerColor": "#FFFF4D4D",
    "mobColor": "#FFF0F0F0",
    "labelColor": "#FFB8B8B8"
  },
  "ammoHud": {
    "enabled": true,
    "marginX": 8, "marginY": 8,
    "showDurabilityPercent": true,
    "useOffhandFallback": true,
    "accentColor": "#FFFFC400",
    "lowColor": "#FFFF3B30",
    "textColor": "#FFFFFFFF"
  },
  "compass": {
    "enabled": true,
    "marginY": 4,
    "width": 180,
    "fov": 120.0,
    "color": "#FFFFFFFF",
    "accentColor": "#FFFFC400"
  },
  "hitMarker": {
    "enabled": true,
    "size": 5.0, "gap": 2.0,
    "durationMs": 260,
    "color": "#FFFFFFFF",
    "killColor": "#FFFF2A25"
  }
}
```

Colors are `#AARRGGBB` (or `#RRGGBB`, assumed fully opaque). Positions are in GUI-scaled pixels.

---

## 🔨 Building from source

Requires **JDK 21** (the toolchain is pinned to 21; the Gradle wrapper is 8.10.2 because
Architectury Loom 1.7 is not compatible with Gradle 9).

```bash
# point Gradle at a JDK 21
export JAVA_HOME=/path/to/jdk-21

./gradlew build              # builds both loaders
./gradlew :fabric:build      # Fabric only
./gradlew :neoforge:build    # NeoForge only
```

Output jars land in `fabric/build/libs/` and `neoforge/build/libs/`
(use the file **without** the `-dev`/`-sources` classifier).

For a dev client:

```bash
./gradlew :fabric:runClient
./gradlew :neoforge:runClient
```

---

## 🗂️ Project layout

```
common/     # loader-agnostic logic: config, networking, kill detection, all HUD rendering
fabric/     # Fabric entrypoints + Fabric-specific damage hook
neoforge/   # NeoForge entrypoints + NeoForge-specific damage hook
```

Nearly all code lives in `common`. Only the mod entrypoints and the one platform-specific
"damage dealt" hook (`DamageHooksImpl`, resolved via Architectury `@ExpectPlatform`) differ
per loader — which is exactly why Fabric and NeoForge stay in lockstep.

## License

MIT.
