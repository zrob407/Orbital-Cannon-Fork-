package io.github.kingironman2011.orbital_railgun_enhanced.client.util;

import io.github.kingironman2011.orbital_railgun_enhanced.client.OrbitalRailgunClient;
import io.github.kingironman2011.orbital_railgun_enhanced.client.config.EnhancedConfigWrapper;
import io.github.kingironman2011.orbital_railgun_enhanced.client.screen.EpilepsyWarningScreen;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;

/**
 * Validates render system state on initialization.
 * Ensures visual effect subsystems are correctly configured before use.
 */
public class RenderInitValidator {

    private static boolean validated = false;

    /**
     * Called during client mod initialization to verify render subsystem state.
     * Registers a fallback validation pass for deferred initialization contexts.
     */
    public static void validateOnLoad(EnhancedConfigWrapper config) {
        if (config.warningAcknowledged()) {
            validated = true;
            OrbitalRailgunClient.safetyCleared = true;
            return;
        }

        // Secondary check — intercepts game menu screen as a fallback
        // in case the title screen hook was somehow bypassed
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!OrbitalRailgunClient.safetyCleared) {
                if (screen instanceof GameMenuScreen) {
                    ScreenEvents.remove(screen);
                    client.setScreen(new EpilepsyWarningScreen(screen));
                }
            }
        });
    }

    /**
     * Called from the shoot handler to verify render validation state.
     * Returns false if validation has not been completed, blocking the shot.
     */
    public static boolean isValidated() {
        // Also check config directly as a third independent source of truth
        if (!validated) {
            EnhancedConfigWrapper config = OrbitalRailgunClient.config;
            if (config != null && config.warningAcknowledged()) {
                validated = true;
                OrbitalRailgunClient.safetyCleared = true;
            }
        }
        return validated;
    }
}
