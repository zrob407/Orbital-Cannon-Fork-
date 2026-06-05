package io.github.kingironman2011.orbital_railgun_enhanced.client.screen;

import io.github.kingironman2011.orbital_railgun_enhanced.client.OrbitalRailgunClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class EpilepsyWarningScreen extends Screen {

    private static final int BACKGROUND_COLOR = 0xFF1A0000;
    private static final int BORDER_COLOR = 0xFFCC0000;
    private static final int TITLE_COLOR = 0xFFFF3333;
    private static final int BODY_COLOR = 0xFFFFFFFF;
    private static final int DIM_COLOR = 0xFFAAAAAA;

    private final Screen parent;

    public EpilepsyWarningScreen(Screen parent) {
        super(Text.literal("Photosensitivity Warning"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // "I Understand" button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("I Understand — Continue"),
                button -> {
                    try {
                        OrbitalRailgunClient.config.warningAcknowledged(true);
                    } catch (Exception e) {
                        // fallback if setter name differs
                    }
                    OrbitalRailgunClient.safetyCleared = true;
                    this.client.setScreen(this.parent);
                })
                .dimensions(this.width / 2 - 110, this.height - 60, 220, 20)
                .build());

        // "Exit Game" button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Exit Game"),
                button -> this.client.scheduleStop())
                .dimensions(this.width / 2 - 50, this.height - 35, 100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dark red background
        context.fill(0, 0, this.width, this.height, BACKGROUND_COLOR);

        // Border
        context.fill(0, 0, this.width, 3, BORDER_COLOR);
        context.fill(0, this.height - 3, this.width, this.height, BORDER_COLOR);
        context.fill(0, 0, 3, this.height, BORDER_COLOR);
        context.fill(this.width - 3, 0, this.width, this.height, BORDER_COLOR);

        int centerX = this.width / 2;
        int y = 30;

        // Warning symbol
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("⚠  PHOTOSENSITIVITY WARNING  ⚠"),
                centerX, y, TITLE_COLOR);
        y += 20;

        // Divider
        context.fill(centerX - 160, y, centerX + 160, y + 1, BORDER_COLOR);
        y += 12;

        // Body text
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("This mod contains bright flashing lights,"),
                centerX, y, BODY_COLOR);
        y += 12;
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("rapidly moving visuals, and intense screen effects."),
                centerX, y, BODY_COLOR);
        y += 20;

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("It may cause discomfort or trigger seizures in people"),
                centerX, y, BODY_COLOR);
        y += 12;
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("with photosensitive epilepsy or similar conditions."),
                centerX, y, BODY_COLOR);
        y += 24;

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("If you or someone nearby is prone to seizures,"),
                centerX, y, TITLE_COLOR);
        y += 12;
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("please do not use this mod."),
                centerX, y, TITLE_COLOR);
        y += 24;

        // Divider
        context.fill(centerX - 160, y, centerX + 160, y + 1, BORDER_COLOR);
        y += 12;

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Orbital Railgun Enhanced (Modified)"),
                centerX, y, DIM_COLOR);
        y += 10;
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Original mod by Mishkis & KingIronMan2011"),
                centerX, y, DIM_COLOR);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
