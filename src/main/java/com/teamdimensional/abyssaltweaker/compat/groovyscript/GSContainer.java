package com.teamdimensional.abyssaltweaker.compat.groovyscript;

import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer;

public class GSContainer extends GroovyPropertyContainer {

    public final Ritual ritual = new Ritual();
    public final RegistryTransmutation transmutator = new RegistryTransmutation();
    public final RegistryCrystallization crystallizer = new RegistryCrystallization();
    public final RegistryMaterialization materializer = new RegistryMaterialization();
    public final Fuel fuel = new Fuel();

    public GSContainer() {
        addProperty(ritual);
        addProperty(transmutator);
        addProperty(crystallizer);
        addProperty(materializer);
        addProperty(fuel);
    }

}
