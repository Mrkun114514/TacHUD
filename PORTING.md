# Porting TacHUD across Minecraft 1.20 – 1.21.x

The mod's **reference build targets MC 1.21.1**. Minecraft's client rendering and event
APIs changed several times across the 1.20 → 1.21 range, so a single set of `gradle.properties`
values cannot compile against every version. This document lists the concrete deltas and the
recommended way to cover the whole range.

> TL;DR: For one or two versions, just change `gradle.properties` and patch the handful of
> API call sites listed below. For the **entire 1.20–1.21.x matrix in one repo**, adopt
> **Stonecutter** (below) — it is purpose-built for exactly this.

---

## 1. Per-version toolchain values (`gradle.properties`)

| MC | Java | Fabric Loader | Fabric API (example) | NeoForge / Forge | Architectury API | Loom |
|---|---|---|---|---|---|---|
| 1.21.1 | 21 | 0.16.x | `0.116.x+1.21.1` | NeoForge `21.1.x` | `13.0.x` | `1.7.x` |
| 1.21.4 | 21 | 0.16.x | `0.119.x+1.21.4` | NeoForge `21.4.x` | `15.x` | `1.9.x` |
| 1.20.4 | 17 | 0.15–0.16 | `0.97.x+1.20.4` | NeoForge `20.4.x` | `11.x` | `1.6.x` |
| 1.20.1 | 17 | 0.15–0.16 | `0.92.x+1.20.1` | **Forge** `47.x` (NeoForge n/a) | `9.x` | `1.6.x` |

Notes:
- **Java 17** is required for 1.20.x; **Java 21** for 1.21.x. Set the toolchain accordingly.
- **1.20.1 has no NeoForge** — use MinecraftForge and Architectury's `forge()` DSL instead of `neoForge()`.
- Always confirm exact patch versions against the live maven metadata before building.

---

## 2. Source-level API deltas

The code is deliberately structured so version-sensitive calls are few and localized.

### 2.1 `ResourceLocation` construction  — files: `net/KillPayload.java`, `net/HitPayload.java`
- **1.21+**: `ResourceLocation.fromNamespaceAndPath(ns, path)`
- **1.20.x**: `new ResourceLocation(ns, path)`

### 2.2 Networking (custom packets)  — files: `net/*.java`, `net/ModNetworking.java`
- **1.20.5 / 1.21+**: `CustomPacketPayload` + `StreamCodec` + `ByteBufCodecs` (current code).
- **1.20.1 – 1.20.4**: no `CustomPacketPayload`. Use Architectury's older
  `NetworkManager.registerReceiver(side, ResourceLocation, (buf, ctx) -> ...)` and write/read
  the fields manually with `FriendlyByteBuf`. Replace the two payload records with a small
  `FriendlyByteBuf` encode/decode pair.

### 2.3 HUD render callback  — file: `client/TacHudClient.java`
- **1.21.x**: `ClientGuiEvent.RENDER_HUD.register((GuiGraphics, DeltaTracker) -> ...)` (current).
- **1.20.1**: the lambda's second argument is a `float` partial-tick, not `DeltaTracker`.
  Change the lambda signature only; the body is unaffected (we use wall-clock time for
  animation).

### 2.4 GuiGraphics API  — files: `client/hud/*.java`
- `fill`, `fillGradient`, `drawString`, `drawCenteredString`, `pose()` exist across the whole
  range. The horizontal-gradient helper (`Draw.gradientH`) is hand-rolled, so it needs no changes.
- 1.20.1's `drawString`/`drawCenteredString` overloads match; no change expected.

### 2.5 Platform damage hook  — files: `platform/**/DamageHooksImpl.java`
- **Fabric**: `ServerLivingEntityEvents.AFTER_DAMAGE` exists 1.20+ (current code works).
- **NeoForge 1.21.x**: `LivingDamageEvent.Post#getNewDamage()` (current code).
- **NeoForge 1.20.4**: `LivingDamageEvent#getAmount()` (single event, no `.Post`).
- **Forge 1.20.1**: `LivingDamageEvent#getAmount()` on the Forge event bus (`MinecraftForge.EVENT_BUS`).

### 2.6 Kill detection  — file: `TacHud.java`
- `EntityEvent.LIVING_DEATH` is stable across the whole range via Architectury. No change.

### 2.7 Item / inventory  — file: `client/hud/AmmoHudOverlay.java`
- `ProjectileWeaponItem`, `ItemTags.ARROWS`, `ItemStack#isDamageableItem/getMaxDamage/getDamageValue`,
  `Inventory#items/offhand` are stable across the range. No change expected.

---

## 3. Recommended: single-repo multi-version with Stonecutter

For maintaining **every** version in one branch, add
[**Stonecutter**](https://stonecutter.kikugie.dev/) on top of this Architectury project.
Stonecutter is a Gradle preprocessor that compiles the same sources against many MC versions
using version-guarded comments, e.g.:

```java
//? if >=1.21 {
var id = ResourceLocation.fromNamespaceAndPath(ns, path);
//?} else
/*var id = new ResourceLocation(ns, path);*/
```

Workflow:
1. Apply the Stonecutter settings plugin in `settings.gradle` and declare your target versions.
2. Wrap the ~6 call sites in section 2 with version comments.
3. `./gradlew chiseledBuild` produces a jar per (version × loader).

This keeps one source of truth while emitting the full 1.20–1.21.x × Fabric/NeoForge matrix.

---

## 4. Quick porting checklist

- [ ] Update `gradle.properties` (MC, Java, loader, API versions) from the table in §1.
- [ ] Adjust the Java toolchain in `build.gradle` (17 for 1.20.x).
- [ ] Patch the call sites in §2 that apply to your target version.
- [ ] For 1.20.1, switch `neoForge()` → `forge()` and swap the damage-hook bus.
- [ ] `./gradlew :fabric:build :neoforge:build` and test in a dev client.
