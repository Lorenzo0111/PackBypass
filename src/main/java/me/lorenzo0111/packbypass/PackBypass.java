package me.lorenzo0111.packbypass;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackBypass implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("packbypass");

    @Override
    public void onInitialize() {
        LOGGER.info("PackBypass has been initialized!");
    }

}
