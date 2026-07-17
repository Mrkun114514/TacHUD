# TacHUD — 使命召唤风格战术 HUD

一个为 Minecraft 打造的 Call-of-Duty 风战术 HUD 模组，基于 **Architectury** 多加载器框架开发，
单套代码同时发布 **Fabric** 与 **NeoForge** 两个版本，行为完全一致。

> **定位**：TacHUD 是一个**客户端 HUD 附属增强模组**——本身不修改世界生成、不改变游戏玩法，
> 仅在客户端叠加战术化信息显示。击杀/命中通知在多人游戏下需服务端也安装（服务端权威）。

---

## 📋 版本信息

| 项目 | 内容 |
|---|---|
| **模组版本** | `1.0.0` |
| **Minecraft 版本** | `1.21.8` |
| **模组加载器** | **Fabric** / **NeoForge**（二选一，按你用的加载器下载对应 jar） |
| **Java 要求** | Java 21+ |
| **运行前置（附属依赖）** | **Architectury API**（Fabric 与 NeoForge 均需）；Fabric 端另需 **Fabric API** |

> TacHUD 作为附属模组，**必须**安装上述前置 API 才能启动；无需其它主模组。

---

## ✨ 功能一览

| 功能 | 位置 | 说明 |
|---|---|---|
| **低血量警告** | 屏幕边缘 | 生命值 ≤ 阈值（默认 **8 HP / 4 心**）时脉冲式红色暗角，越接近死亡越强，回血后立即消失。 |
| **击杀通知** | 右上角 | 击杀玩家或实体时弹出受击者名称（MW「+100」风格，无分数），淡入/停留/淡出；非玩家击杀不显示；玩家与生物击杀颜色区分。 |
| **弹药 / 耐久** | 左下角 | 持弓/弩/投射武器 → 剩余箭矢数；可损坏工具/武器 → 耐久 % 与 `剩余/最大`，过低变红。 |
| **战术罗盘** | 顶部中央 | 滚动式航向条，N/E/S/W + 间位方位、刻度与数字读数。 |
| **命中标记** | 准星 | 每次命中闪现标志性展开「✕」，击杀一击用独立颜色标记。 |
| **UI 自适应缩放** *(新)* | 全局 | 所有 HUD 元素按屏幕高度自动贴合缩放，并叠加手动倍数（0.5–3.0），不同分辨率都协调。 |
| **击中 / 击杀音效** *(新)* | 音频 | 命中与击杀分别播放可配置音效（默认用原版 note_block 音，无需额外音频文件），音量/音调/击杀独立音可调。 |
| **击杀反馈** *(新)* | 屏幕中央 | 击杀时收缩式锁定框 + 放大「KILL」字样 + 连杀计数，颜色/时长可配。 |
| **经验弹出** *(新)* | 准星下方 | 吸收经验球时弹出淡入淡出的「+N XP」，并可显示瞬时 XP/s 速率。 |
| **模组设置 GUI** *(新)* | 全屏界面 | 按 **G** 打开五标签页设置界面（通用 / 命中音效 / 击杀反馈 / 经验弹出 / 模块开关），改完实时写入 `config/tachud.json`，无需重启。 |

所有功能均可单独开关，并通过 GUI 或配置文件调整。

---

## 🎮 安装

1. 安装 **Java 21+**，以及 **Minecraft 1.21.8** 对应加载器（Fabric 或 NeoForge），并装好前置：
   - **Fabric**：Fabric API + Architectury API
   - **NeoForge**：Architectury API
2. 把 `tachud-fabric-1.0.0.jar` **或** `tachud-neoforge-1.0.0.jar`（二选一）放进 `mods/`。
3. 多人游戏下若要击杀通知与命中标记生效，**服务端也需安装**本模组（它们由服务端权威判定）；
   单人游戏开箱即用。

---

## ⚙️ 配置

首次启动会在 `config/tachud.json` 生成配置文件。两种调节方式：

- **GUI（推荐）**：游戏内按 **G** 打开设置界面，所见即所得，关闭即保存。
- **文件 + 重载**：直接编辑 `config/tachud.json`，然后按 **R** 热重载，无需重启。

```jsonc
{
  "masterEnabled": true,
  "uiScale": 1.0,                 // 全局缩放倍数，再叠加按屏幕高度的自适应
  "lowHealth":   { "enabled": true, "thresholdHp": 8.0, "color": "#FFFF2A25", /* ... */ },
  "killFeed":    { "enabled": true, "holdMs": 2600, "fadeMs": 320, /* ... */ },
  "ammoHud":     { "enabled": true, "accentColor": "#FFFFC400", /* ... */ },
  "compass":     { "enabled": true, "width": 180, "fov": 120.0, /* ... */ },
  "hitMarker":   { "enabled": true, "color": "#FFFFFFFF", "killColor": "#FFFF2A25", /* ... */ },
  "hitSound":    {                 // 命中/击杀音效
    "enabled": true, "sound": "minecraft:block.note_block.hat",
    "volume": 0.7, "pitch": 1.0, "killDistinct": true,
    "killSound": "minecraft:block.note_block.bass"
  },
  "killConfirm": {                 // 居中击杀反馈
    "enabled": true, "size": 1.0, "durationMs": 900,
    "showKillstreak": true, "streakResetMs": 4000,
    "color": "#FFFF2A25", "text": "KILL"
  },
  "xpPop":       {                 // 准星下方经验弹出
    "enabled": true, "size": 1.0, "holdMs": 1100, "fadeMs": 450,
    "color": "#FF7CFC00", "showRate": true
  }
}
```

颜色为 `#AARRGGBB`（或 `#RRGGBB`，默认不透明）。按键可在「控制 → TacHUD」分类下改绑。

---

## 🔨 从源码构建

需要 **JDK 21**（工具链锁定 21；Gradle wrapper 为 8.14.3，因 Architectury Loom 1.10 暂不兼容 Gradle 9）。

```bash
# 让 Gradle 用到 JDK 21
export JAVA_HOME=/path/to/jdk-21

./gradlew build              # 同时构建 Fabric + NeoForge
./gradlew :fabric:build      # 仅 Fabric
./gradlew :neoforge:build    # 仅 NeoForge
```

产物在 `fabric/build/libs/` 与 `neoforge/build/libs/`（用**不带** `-dev` / `-sources` 分类符的那个 jar）。

开发客户端：

```bash
./gradlew :fabric:runClient
./gradlew :neoforge:runClient
```

---

## 🗂️ 项目结构

```
common/     # 加载器无关逻辑：配置、网络、击杀检测、全部 HUD 渲染、设置 GUI
fabric/     # Fabric 入口 + Fabric 伤害钩子
neoforge/   # NeoForge 入口 + NeoForge 伤害钩子
```

几乎所有代码都在 `common`。仅入口类与唯一的平台伤害钩子（`DamageHooksImpl`，经 Architectury
`@ExpectPlatform` 解析）按加载器不同——这正是 Fabric 与 NeoForge 始终保持一致的原因。

## License

MIT。
