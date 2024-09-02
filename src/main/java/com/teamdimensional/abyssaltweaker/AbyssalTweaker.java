package com.teamdimensional.abyssaltweaker;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, dependencies = "required-after:abyssalcraft@[2.0.0-alpha-1,)")
public class AbyssalTweaker {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    /**
     * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
     *     Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
     * </a>
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("Hello From {}!", Tags.MOD_NAME);
        LOGGER.info("Patching Gateway Key dimensions...");
        ConfigHelper.patchConnectedDimensions();
    }

}
