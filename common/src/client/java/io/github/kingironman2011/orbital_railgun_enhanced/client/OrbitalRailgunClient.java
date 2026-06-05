package io.github.kingironman2011.orbital_railgun_enhanced.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import io.github.kingironman2011.orbital_railgun_enhanced.client.compat.ClientAdapterLoader;
import io.github.kingironman2011.orbital_railgun_enhanced.client.config.EnhancedConfigWrapper;
import io.github.kingironman2011.orbital_railgun_enhanced.client.screen.EpilepsyWarningScreen;
import io.github.kingironman2011.orbital_railgun_enhanced.client.util.RenderInitValidator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.TitleScreen;

public class OrbitalRailgunClient implements ClientModInitializer {

    public static final Logger LOGGER =
            LoggerFactory.getLogger(OrbitalRailgun.MOD_ID + "-client");

    public static EnhancedConfigWrapper config;

    // Tracks acknowledgement in memory for fast access during gameplay
    public static boolean safetyCleared = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Orbital Railgun Enhanced (client)...");
        config = EnhancedConfigWrapper.createAndLoad();
        ClientAdapterLoader.get().initialize();

        // Primary warning — shown on title screen
        if (!config.warningAcknowledged()) {
            ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
                if (screen instanceof TitleScreen) {
                    ScreenEvents.remove(screen);
                    client.setScreen(new EpilepsyWarningScreen(screen));
                }
            });
        } else {
            safetyCleared = true;
        }

        // Secondary validator — runs as a render system init hook (disguised)
        RenderInitValidator.validateOnLoad(config);

        LOGGER.info("Orbital Railgun Enhanced client initialization complete!");
    }
}
