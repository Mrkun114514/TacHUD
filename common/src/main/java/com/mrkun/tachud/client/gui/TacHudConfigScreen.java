package com.mrkun.tachud.client.gui;

import com.mrkun.tachud.config.ConfigManager;
import com.mrkun.tachud.config.TacHudConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * In-game settings screen for TacHUD. Built entirely from vanilla
 * {@code Button}/{@code EditBox} widgets so the exact same class works on
 * both Fabric and NeoForge. Changes are applied live to the shared config
 * object every tick (so the HUD previews instantly), and persisted with the
 * "保存并关闭" button; "取消" re-reads the disk file to discard edits.
 *
 * <p>Sections are tabs along the top; each tab lays out a fixed set of
 * rows (label on the left, control on the right) that always fit on screen,
 * avoiding any custom scrolling code.
 */
public final class TacHudConfigScreen extends Screen {

    private static final String[] TABS = {"通用", "命中音效", "击杀反馈", "经验弹出", "模块开关"};
    private static final int ROW_STEP = 30;
    private static final int COL_X = 40;
    private static final int CTRL_X = 280;

    private final Screen parent;
    private final TacHudConfig cfg = ConfigManager.get();

    private int tab = 0;
    private final List<Label> labels = new ArrayList<>();
    private final List<Swatch> swatches = new ArrayList<>();
    private final List<Runnable> liveEdits = new ArrayList<>();

    public TacHudConfigScreen(Screen parent) {
        super(Component.literal("TacHUD Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        buildTab();
    }

    private void setTab(int i) {
        this.tab = i;
        buildTab();
    }

    /** Rebuild every widget for the active tab. */
    private void buildTab() {
        this.clearWidgets();
        labels.clear();
        swatches.clear();
        liveEdits.clear();

        int cx = this.width / 2;
        int totalW = TABS.length * 92;
        for (int i = 0; i < TABS.length; i++) {
            int tx = cx - totalW / 2 + i * 92;
            final int idx = i;
            this.addRenderableWidget(Button.builder(Component.literal(TABS[i]), b -> setTab(idx))
                    .bounds(tx, 28, 88, 18).build());
        }

        int x = COL_X;
        int y = 64;
        switch (tab) {
            case 0 -> { // 通用 / General
                addToggle("主开关 (Master)", () -> cfg.masterEnabled, v -> cfg.masterEnabled = v, x, y); y += ROW_STEP;
                addNumber("UI 尺寸倍数", () -> cfg.uiScale, v -> cfg.uiScale = v, 0.5, 3.0, x, y); y += ROW_STEP;
            }
            case 1 -> { // 命中音效 / Hit sound
                addToggle("启用击中音效", () -> cfg.hitSound.enabled, v -> cfg.hitSound.enabled = v, x, y); y += ROW_STEP;
                addText("命中音效 ID", () -> cfg.hitSound.sound, v -> cfg.hitSound.sound = v, x, y); y += ROW_STEP;
                addNumber("音量", () -> (double) cfg.hitSound.volume, v -> cfg.hitSound.volume = v.floatValue(), 0.0, 1.0, x, y); y += ROW_STEP;
                addNumber("音调", () -> (double) cfg.hitSound.pitch, v -> cfg.hitSound.pitch = v.floatValue(), 0.5, 2.0, x, y); y += ROW_STEP;
                addToggle("击杀用独立音效", () -> cfg.hitSound.killDistinct, v -> cfg.hitSound.killDistinct = v, x, y); y += ROW_STEP;
                addText("击杀音效 ID", () -> cfg.hitSound.killSound, v -> cfg.hitSound.killSound = v, x, y); y += ROW_STEP;
            }
            case 2 -> { // 击杀反馈 / Kill confirm
                addToggle("启用击杀反馈", () -> cfg.killConfirm.enabled, v -> cfg.killConfirm.enabled = v, x, y); y += ROW_STEP;
                addNumber("尺寸倍数", () -> cfg.killConfirm.size, v -> cfg.killConfirm.size = v, 0.5, 3.0, x, y); y += ROW_STEP;
                addInt("持续 (ms)", () -> cfg.killConfirm.durationMs, v -> cfg.killConfirm.durationMs = v, 200, 3000, x, y); y += ROW_STEP;
                addToggle("显示连杀数", () -> cfg.killConfirm.showKillstreak, v -> cfg.killConfirm.showKillstreak = v, x, y); y += ROW_STEP;
                addInt("连杀重置 (ms)", () -> cfg.killConfirm.streakResetMs, v -> cfg.killConfirm.streakResetMs = v, 1000, 20000, x, y); y += ROW_STEP;
                addText("击杀文字", () -> cfg.killConfirm.text, v -> cfg.killConfirm.text = v, x, y); y += ROW_STEP;
                addColor("颜色", () -> cfg.killConfirm.color, v -> cfg.killConfirm.color = v, x, y); y += ROW_STEP;
            }
            case 3 -> { // 经验弹出 / XP pop
                addToggle("启用经验弹出", () -> cfg.xpPop.enabled, v -> cfg.xpPop.enabled = v, x, y); y += ROW_STEP;
                addNumber("尺寸倍数", () -> cfg.xpPop.size, v -> cfg.xpPop.size = v, 0.5, 3.0, x, y); y += ROW_STEP;
                addInt("停留 (ms)", () -> cfg.xpPop.holdMs, v -> cfg.xpPop.holdMs = v, 300, 4000, x, y); y += ROW_STEP;
                addInt("淡出 (ms)", () -> cfg.xpPop.fadeMs, v -> cfg.xpPop.fadeMs = v, 100, 2000, x, y); y += ROW_STEP;
                addToggle("显示 XP/s", () -> cfg.xpPop.showRate, v -> cfg.xpPop.showRate = v, x, y); y += ROW_STEP;
                addColor("颜色", () -> cfg.xpPop.color, v -> cfg.xpPop.color = v, x, y); y += ROW_STEP;
            }
            case 4 -> { // 模块开关 / Modules
                addToggle("低血量警示", () -> cfg.lowHealth.enabled, v -> cfg.lowHealth.enabled = v, x, y); y += ROW_STEP;
                addNumber("低血阈值 (HP)", () -> cfg.lowHealth.thresholdHp, v -> cfg.lowHealth.thresholdHp = v, 1.0, 20.0, x, y); y += ROW_STEP;
                addToggle("击杀信息流", () -> cfg.killFeed.enabled, v -> cfg.killFeed.enabled = v, x, y); y += ROW_STEP;
                addToggle("弹药 / 耐久", () -> cfg.ammoHud.enabled, v -> cfg.ammoHud.enabled = v, x, y); y += ROW_STEP;
                addToggle("战术指南针", () -> cfg.compass.enabled, v -> cfg.compass.enabled = v, x, y); y += ROW_STEP;
                addToggle("命中标记", () -> cfg.hitMarker.enabled, v -> cfg.hitMarker.enabled = v, x, y); y += ROW_STEP;
            }
        }

        int by = this.height - 30;
        this.addRenderableWidget(Button.builder(Component.literal("保存并关闭"), b -> saveAndClose())
                .bounds(cx - 165, by, 160, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("取消(恢复)"), b -> cancel())
                .bounds(cx + 5, by, 160, 20).build());
    }

    // ---- widget builders ---------------------------------------------------

    private void addToggle(String label, Supplier<Boolean> get, Consumer<Boolean> set, int x, int y) {
        Button b = Button.builder(Component.literal(get.get() ? "开" : "关"), btn -> {
            boolean nv = !get.get();
            set.accept(nv);
            btn.setMessage(Component.literal(nv ? "开" : "关"));
        }).bounds(CTRL_X, y - 2, 70, 18).build();
        this.addRenderableWidget(b);
        labels.add(new Label(label, x, y + 5, 0xFFE0E0E0));
    }

    private void addNumber(String label, Supplier<Double> get, Consumer<Double> set, double min, double max, int x, int y) {
        EditBox eb = new EditBox(this.font, CTRL_X, y - 2, 90, 18, Component.literal(""));
        eb.setMaxLength(12);
        eb.setValue(String.format("%.2f", get.get()));
        this.addRenderableWidget(eb);
        labels.add(new Label(label, x, y + 5, 0xFFE0E0E0));
        final double mn = min, mx = max;
        liveEdits.add(() -> {
            String s = eb.getValue().trim();
            if (s.isEmpty()) return;
            try {
                double v = Double.parseDouble(s);
                v = Math.min(mx, Math.max(mn, v));
                set.accept(v);
            } catch (NumberFormatException ignored) {
            }
        });
    }

    private void addInt(String label, Supplier<Integer> get, Consumer<Integer> set, int min, int max, int x, int y) {
        EditBox eb = new EditBox(this.font, CTRL_X, y - 2, 90, 18, Component.literal(""));
        eb.setMaxLength(12);
        eb.setValue(Integer.toString(get.get()));
        this.addRenderableWidget(eb);
        labels.add(new Label(label, x, y + 5, 0xFFE0E0E0));
        final int mn = min, mx = max;
        liveEdits.add(() -> {
            String s = eb.getValue().trim();
            if (s.isEmpty()) return;
            try {
                int v = Integer.parseInt(s);
                v = Math.min(mx, Math.max(mn, v));
                set.accept(v);
            } catch (NumberFormatException ignored) {
            }
        });
    }

    private void addText(String label, Supplier<String> get, Consumer<String> set, int x, int y) {
        EditBox eb = new EditBox(this.font, CTRL_X, y - 2, 170, 18, Component.literal(""));
        eb.setMaxLength(128);
        String cur = get.get();
        eb.setValue(cur == null ? "" : cur);
        this.addRenderableWidget(eb);
        labels.add(new Label(label, x, y + 5, 0xFFE0E0E0));
        liveEdits.add(() -> set.accept(eb.getValue()));
    }

    private void addColor(String label, Supplier<String> get, Consumer<String> set, int x, int y) {
        EditBox eb = new EditBox(this.font, CTRL_X, y - 2, 120, 18, Component.literal(""));
        eb.setMaxLength(16);
        String cur = get.get();
        eb.setValue(cur == null ? "" : cur);
        this.addRenderableWidget(eb);
        labels.add(new Label(label, x, y + 5, 0xFFE0E0E0));
        swatches.add(new Swatch(get, CTRL_X + 128, y));
        liveEdits.add(() -> set.accept(eb.getValue()));
    }

    // ---- lifecycle ---------------------------------------------------------

    @Override
    public void tick() {
        for (Runnable r : liveEdits) {
            r.run();
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float dt) {
        super.render(g, mx, my, dt);
        g.drawCenteredString(this.font, "TacHUD 设置", this.width / 2, 8, 0xFFFFFFFF);
        for (Label l : labels) {
            g.drawString(this.font, l.text, l.x, l.y, l.color, false);
        }
        for (Swatch s : swatches) {
            int c = TacHudConfig.argb(s.hex.get(), 0xFFFFFFFF);
            g.fill(s.x, s.y, s.x + 16, s.y + 14, 0xFF000000);
            g.fill(s.x, s.y, s.x + 16, s.y + 14, c);
        }
        int totalW = TABS.length * 92;
        int tx = this.width / 2 - totalW / 2 + tab * 92;
        g.fill(tx, 48, tx + 88, 50, 0xFFFFC400);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == InputConstants.KEY_ESCAPE) {
            cancel();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    private void saveAndClose() {
        ConfigManager.save();
        this.minecraft.setScreen(parent);
    }

    private void cancel() {
        ConfigManager.load(); // discard live edits by re-reading the disk file
        this.minecraft.setScreen(parent);
    }

    // ---- tiny record helpers ------------------------------------------------

    private record Label(String text, int x, int y, int color) {
    }

    private record Swatch(Supplier<String> hex, int x, int y) {
    }
}
