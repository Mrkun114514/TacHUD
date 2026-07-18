# TacHUD 状态值美化 - 实现计划

## [x] Task 1: 在配置中添加氧气值和 UI 自定义选项
- **Priority**: high
- **Depends On**: None
- **Description**: 
  - 在 `TacHudConfig.java` 的 `VanillaHud` 类中添加氧气值启用开关
  - 添加氧气值颜色配置项（前景色和背景色）
  - 为每个状态条（生命值、护甲值、氧气值、饥饿值、饱和度）添加独立的位置偏移配置（X、Y）
  - 为每个状态条添加独立的宽度和高度配置
- **Acceptance Criteria Addressed**: AC-4, AC-6, AC-7
- **Test Requirements**:
  - `programmatic` TR-1.1: 配置类编译通过，新字段能被正确序列化/反序列化
  - `human-judgement` TR-1.2: 配置字段命名合理，默认值符合预期
- **Notes**: 默认颜色建议使用蓝色系（如 #FF00BFFF 作为前景色）

## [x] Task 2: 在配置界面添加氧气值和 UI 自定义设置
- **Priority**: high
- **Depends On**: Task 1
- **Description**: 
  - 在 `TacHudConfigScreen.java` 的 "HUD美化" 标签页中添加氧气值美化开关
  - 添加氧气值颜色选择器
  - 为每个状态条添加位置偏移（X、Y）配置输入框
  - 为每个状态条添加宽度和高度配置输入框
- **Acceptance Criteria Addressed**: AC-4, AC-6, AC-7
- **Test Requirements**:
  - `programmatic` TR-2.1: 配置界面编译通过，无语法错误
  - `human-judgement` TR-2.2: 配置界面布局合理，选项组织清晰
- **Notes**: 可以考虑将位置和大小配置分组显示

## [x] Task 3: 在 VanillaHudOverlay 中添加氧气值渲染并支持 UI 自定义
- **Priority**: high
- **Depends On**: Task 1
- **Description**: 
  - 在 `VanillaHudOverlay.java` 的 `render()` 方法中添加氧气值进度条渲染逻辑
  - 氧气值进度条放在护甲值上方
  - 获取玩家的氧气值（air）和最大氧气值（maxAir）
  - 修改渲染逻辑，使用配置的位置偏移（X、Y）计算每个状态条的位置
  - 修改渲染逻辑，使用配置的宽度和高度渲染每个状态条
- **Acceptance Criteria Addressed**: AC-3, AC-5, AC-6, AC-7
- **Test Requirements**:
  - `programmatic` TR-3.1: 编译通过，无语法错误
  - `human-judgement` TR-3.2: 氧气值进度条在水下正确显示，数值准确，位置和大小可自定义
- **Notes**: 玩家在水中时才显示氧气值进度条

## [x] Task 4: 在 HudRenderer 中调用 VanillaHudOverlay
- **Priority**: high
- **Depends On**: None
- **Description**: 
  - 在 `HudRenderer.java` 的 `render()` 方法中添加 `VanillaHudOverlay.render()` 调用
  - 确保在其他覆盖层渲染完成后调用
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-3, AC-5
- **Test Requirements**:
  - `programmatic` TR-4.1: 编译通过，无语法错误
  - `human-judgement` TR-4.2: 游戏运行时 HUD 美化功能正常显示
- **Notes**: 调用应放在 KillFeedOverlay.render() 之后

## [x] Task 5: 启用 Fabric 平台的 HUD 美化功能
- **Priority**: high
- **Depends On**: None
- **Description**: 
  - 修改 `GuiMixin.java` 的 `isActive()` 方法，使其检查配置并返回正确值
  - 修改 `isFoodCancelled()` 方法，使其检查配置
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-5.1: 编译通过，无语法错误
  - `human-judgement` TR-5.2: Fabric 平台下原版 HUD 被正确隐藏
- **Notes**: 需要从配置管理器获取配置状态

## [x] Task 6: 启用 NeoForge 平台的 HUD 美化功能
- **Priority**: high
- **Depends On**: None
- **Description**: 
  - 修改 `VanillaHudNeoForge.java` 的 `onRenderGuiLayerPre()` 方法
  - 添加逻辑取消原版生命值、护甲值、饥饿值、氧气值的渲染
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `programmatic` TR-6.1: 编译通过，无语法错误
  - `human-judgement` TR-6.2: NeoForge 平台下原版 HUD 被正确隐藏
- **Notes**: 需要检查 RenderGuiLayerEvent 的图层类型

## [x] Task 7: 测试和验证
- **Priority**: medium
- **Depends On**: Task 1-6
- **Description**: 
  - 编译项目确保无错误
  - 检查所有配置选项是否正常工作
  - 验证各状态条渲染位置和颜色正确
  - 验证 UI 位置和大小自定义功能正常工作
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-3, AC-4, AC-5, AC-6, AC-7
- **Test Requirements**:
  - `programmatic` TR-7.1: 项目编译成功
  - `human-judgement` TR-7.2: 所有状态值美化功能正常显示和配置，UI 位置和大小自定义功能正常
- **Notes**: 需要在游戏中实际测试各功能
