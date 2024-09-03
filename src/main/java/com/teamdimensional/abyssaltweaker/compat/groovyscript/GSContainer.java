package com.teamdimensional.abyssaltweaker.compat.groovyscript;

import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer;

public class GSContainer extends GroovyPropertyContainer {

    public final Ritual ritual = new Ritual();
    /*
    public final Transmutator transmutator = new Transmutator();
    public final Crystallizer crystallizer = new Crystallizer();
    public final Materializer materializer = new Materializer();

     */

    public GSContainer() {
        addProperty(ritual);
        /*
        addProperty(transmutator);
        addProperty(crystallizer);
        addProperty(materializer);

         */
    }

}
