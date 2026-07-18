# TacHUD 状态值美化 - 产品需求文档

## Overview
- **Summary**: 启用并完善 TacHUD 的状态值美化功能，包括生命值、饥饿值、饱和值、护甲值和氧气值的 COD 军事风格显示，并支持自定义 UI 位置和大小。
- **Purpose**: 将原版 Minecraft 的心形/鸡腿形状态图标替换为现代军事风格的进度条，提升游戏体验。
- **Target Users**: 使用 TacHUD 模组的 Minecraft 玩家。

## Goals
- 启用 HUD 美化功能，替换原版生命值、护甲值、饥饿值、饱和度显示
- 添加氧气值的美化支持（水下呼吸状态）
- 确保 Fabric 和 NeoForge 平台都能正常工作
- 提供配置选项让玩家自定义各状态条的外观
- 支持自定义各状态条的位置和大小（类似魔兽插件的 UI 自定义功能）

## Non-Goals (Out of Scope)
- 修改其他 HUD 元素（如指南针、弹药计数等）
- 添加新的状态类型支持
- 修改现有的颜色配置系统

## Background & Context
- 当前代码已包含 `VanillaHudOverlay.java` 实现了生命值、护甲值、饥饿值和饱和度的美化渲染
- 当前代码中的 `GuiMixin.java`（Fabric）和 `VanillaHudNeoForge.java`（NeoForge）处于禁用状态
- `HudRenderer.java` 中未调用 `VanillaHudOverlay.render()`
- 当前缺少氧气值的美化支持

## Functional Requirements
- **FR-1**: 启用 Fabric 平台的 HUD 美化功能（修改 `GuiMixin.isActive()` 返回 `true`）
- **FR-2**: 启用 NeoForge 平台的 HUD 美化功能（修改 `VanillaHudNeoForge.onRenderGuiLayerPre()`）
- **FR-3**: 在 `HudRenderer.java` 中添加 `VanillaHudOverlay.render()` 调用
- **FR-4**: 在 `VanillaHudOverlay.java` 中添加氧气值的渲染支持
- **FR-5**: 在 `TacHudConfig.java` 的 `VanillaHud` 类中添加氧气值相关配置项
- **FR-6**: 在 `TacHudConfigScreen.java` 中添加氧气值的配置界面
- **FR-7**: 为每个状态条添加位置偏移配置（X、Y 偏移）
- **FR-8**: 为每个状态条添加独立的宽度和高度配置
- **FR-9**: 在配置界面中添加位置和大小的自定义选项

## Non-Functional Requirements
- **NFR-1**: 状态条渲染性能良好，不影响游戏帧率
- **NFR-2**: 配置选项实时生效，无需重启游戏
- **NFR-3**: 与 AppleSkin 模组兼容（已有的自动检测机制）

## Constraints
- **Technical**: 需要支持 Fabric 和 NeoForge 两个平台
- **Dependencies**: Minecraft 版本 1.20.1-1.21.11

## Assumptions
- 玩家已经安装了 TacHUD 模组
- 配置文件格式与现有代码兼容
- 原版 HUD 隐藏机制在两个平台上都可用

## Acceptance Criteria

### AC-1: Fabric 平台 HUD 美化启用
- **Given**: 用户使用 Fabric 平台并启用了 TacHUD 的 HUD 美化选项
- **When**: 进入游戏并查看 HUD
- **Then**: 原版生命值、护甲值、饥饿值图标被隐藏，显示 COD 风格的进度条
- **Verification**: `human-judgment`

### AC-2: NeoForge 平台 HUD 美化启用
- **Given**: 用户使用 NeoForge 平台并启用了 TacHUD 的 HUD 美化选项
- **When**: 进入游戏并查看 HUD
- **Then**: 原版生命值、护甲值、饥饿值图标被隐藏，显示 COD 风格的进度条
- **Verification**: `human-judgment`

### AC-3: 氧气值美化显示
- **Given**: 用户启用了 HUD 美化并进入水下
- **When**: 玩家在水下消耗氧气
- **Then**: HUD 显示氧气值进度条（在生命值上方或附近）
- **Verification**: `human-judgment`

### AC-4: 配置界面支持氧气值设置
- **Given**: 用户打开 TacHUD 配置界面
- **When**: 切换到 "HUD美化" 标签页
- **Then**: 显示氧气值美化的开关和颜色配置选项
- **Verification**: `human-judgment`

### AC-5: 状态值文本显示
- **Given**: 用户启用了 HUD 美化
- **When**: 查看生命值和护甲值
- **Then**: 进度条旁边显示具体数值（如 "20/20" 生命值，"20" 护甲值）
- **Verification**: `human-judgment`

### AC-6: UI 位置自定义
- **Given**: 用户在配置界面调整了状态条的位置偏移
- **When**: 应用配置并返回游戏
- **Then**: 状态条的位置根据配置偏移显示
- **Verification**: `human-judgment`

### AC-7: UI 大小自定义
- **Given**: 用户在配置界面调整了状态条的宽度和高度
- **When**: 应用配置并返回游戏
- **Then**: 状态条的大小根据配置显示
- **Verification**: `human-judgment`

## Open Questions
- [ ] 氧气值进度条应该放在哪个位置？（建议放在护甲值上方）
- [ ] 氧气值的默认颜色应该是什么？（建议蓝色系）
