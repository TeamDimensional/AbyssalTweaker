package com.teamdimensional.abyssaltweaker.compat.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.*;
import com.cleanroommc.groovyscript.helper.Alias;
import com.cleanroommc.groovyscript.helper.SimpleObjectStream;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.shinoow.abyssalcraft.api.recipe.Transmutation;
import com.shinoow.abyssalcraft.api.recipe.TransmutatorRecipes;
import com.teamdimensional.abyssaltweaker.AbyssalTweaker;
import com.teamdimensional.abyssaltweaker.Tags;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

@RegistryDescription(linkGenerator = Tags.MOD_ID)
public class RegistryTransmutation extends VirtualizedRegistry<Transmutation> {
    public RegistryTransmutation() {
        super(Alias.generateOf("Transmutation"));
    }

    @Override
    public void onReload() {
        TransmutatorRecipes.instance().getTransmutationList().removeAll(removeScripted());
        TransmutatorRecipes.instance().getTransmutationList().addAll(restoreFromBackup());
    }

    public void add(Transmutation t) {
        TransmutatorRecipes.instance().getTransmutationList().add(t);
    }

    public boolean remove(Transmutation t) {
        if (TransmutatorRecipes.instance().getTransmutationList().remove(t)) {
            addBackup(t);
            return true;
        }
        return false;
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public SimpleObjectStream<Transmutation> streamRecipes() {
        return new SimpleObjectStream<>(TransmutatorRecipes.instance().getTransmutationList()).setRemover(this::remove);
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, example = @Example("item('minecraft:flint')"))
    public boolean removeByOutput(IIngredient output) {
        return TransmutatorRecipes.instance().getTransmutationList().removeIf(r -> {
            if (GSHelpers.compareItemstacks(output, r.OUTPUT)) {
                addBackup(r);
                return true;
            }
            return false;
        });
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, example = @Example("item('minecraft:diamond')"))
    public boolean removeByInput(IIngredient input) {
        return TransmutatorRecipes.instance().getTransmutationList().removeIf(r -> {
            AbyssalTweaker.LOGGER.info("Input: {}, test against: {}", input, r.INPUT);
            if (GSHelpers.compareItemstacks(input, r.INPUT)) {
                AbyssalTweaker.LOGGER.info("Yea!");
                addBackup(r);
                return true;
            }
            return false;
        });
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, priority = 2000, example = @Example(commented = true))
    public void removeAll() {
        TransmutatorRecipes.instance().getTransmutationList().forEach(this::addBackup);
        TransmutatorRecipes.instance().getTransmutationList().clear();
    }

    @RecipeBuilderDescription(example = @Example(".input(item('minecraft:clay')).output(item('minecraft:diamond')).xp(0.5)"))
    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    @Property(property = "input", valid = @Comp("1"))
    @Property(property = "output", valid = @Comp("1"))
    public static class RecipeBuilder extends AbstractRecipeBuilder<Transmutation> {
        @Property(valid = @Comp(type = Comp.Type.GTE, value = "0"))
        private float xp;

        @RecipeBuilderMethodDescription
        public RecipeBuilder xp(float xp) {
            this.xp = xp;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding AbyssalTweaker Transmutation recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            validateItems(msg, 1, 1, 1, 1);
            msg.add(xp < 0, "XP must be 0 or greater");
        }

        @Override
        @Nullable
        @RecipeBuilderRegistrationMethod
        public Transmutation register() {
            if (!validate()) return null;
            Transmutation t = null;
            for (ItemStack in : input.get(0).getMatchingStacks()) {
                t = new Transmutation(in, output.get(0), xp);
                GSPlugin.instance.transmutator.add(t);
            }
            return t;
        }
    }
}
