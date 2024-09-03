package com.teamdimensional.abyssaltweaker.compat.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.api.GroovyPlugin;
import com.cleanroommc.groovyscript.compat.mods.GroovyContainer;
import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer;
import com.cleanroommc.groovyscript.documentation.linkgenerator.BasicLinkGenerator;
import com.cleanroommc.groovyscript.documentation.linkgenerator.LinkGeneratorHooks;
import com.teamdimensional.abyssaltweaker.Tags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GSPlugin implements GroovyPlugin {

    @GroovyBlacklist
    public static GSContainer instance;

    @Override
    public @Nullable GroovyPropertyContainer createGroovyPropertyContainer() {
        if (GSPlugin.instance == null) GSPlugin.instance = new GSContainer();
        return GSPlugin.instance;
    }

    @Override
    public @Nonnull String getModId() {
        return Tags.MOD_ID;
    }

    @Override
    public @Nonnull String getContainerName() {
        return Tags.MOD_NAME;
    }

    @Override
    public void onCompatLoaded(GroovyContainer<?> container) {
        LinkGeneratorHooks.registerLinkGenerator(new AbyssalTweakerLinkGenerator());
    }

    private static class AbyssalTweakerLinkGenerator extends BasicLinkGenerator {
        @Override
        public String id() {
            return Tags.MOD_ID;
        }
        @Override
        protected String domain() {
            return "https://github.com/TeamDimensional/AbyssalTweaker/";
        }
    }

}
