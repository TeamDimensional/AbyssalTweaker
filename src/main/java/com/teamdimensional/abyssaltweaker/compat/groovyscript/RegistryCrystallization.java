package com.teamdimensional.abyssaltweaker.compat.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.*;
import com.cleanroommc.groovyscript.helper.Alias;
import com.cleanroommc.groovyscript.helper.SimpleObjectStream;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.shinoow.abyssalcraft.api.recipe.Crystallization;
import com.shinoow.abyssalcraft.api.recipe.CrystallizerRecipes;
import com.teamdimensional.abyssaltweaker.Tags;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

@RegistryDescription(linkGenerator = Tags.MOD_ID)
public class RegistryCrystallization extends VirtualizedRegistry<Crystallization> {
    public RegistryCrystallization() {
        super(Alias.generateOf("Crystallization"));
    }

    @Override
    public void onReload() {
        CrystallizerRecipes.instance().getCrystallizationList().removeAll(removeScripted());
        CrystallizerRecipes.instance().getCrystallizationList().addAll(restoreFromBackup());
    }

    public void add(Crystallization t) {
        CrystallizerRecipes.instance().getCrystallizationList().add(t);
        addScripted(t);
    }

    public boolean remove(Crystallization t) {
        if (CrystallizerRecipes.instance().getCrystallizationList().remove(t)) {
            addBackup(t);
            return true;
        }
        return false;
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public SimpleObjectStream<Crystallization> streamRecipes() {
        return new SimpleObjectStream<>(CrystallizerRecipes.instance().getCrystallizationList()).setRemover(this::remove);
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, example = @Example("item('abyssalcraft:copper_crystal')"))
    public boolean removeByOutput(IIngredient output) {
        return CrystallizerRecipes.instance().getCrystallizationList().removeIf(r -> {
            if (GSHelpers.compareItemstacks(output, r.OUTPUT1) || GSHelpers.compareItemstacks(output, r.OUTPUT2)) {
                addBackup(r);
                return true;
            }
            return false;
        });
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, example = @Example("item('minecraft:blaze_powder')"))
    public boolean removeByInput(IIngredient input) {
        return CrystallizerRecipes.instance().getCrystallizationList().removeIf(r -> {
            if (GSHelpers.compareItemstacks(input, r.INPUT)) {
                addBackup(r);
                return true;
            }
            return false;
        });
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, priority = 2000, example = @Example(commented = true))
    public void removeAll() {
        CrystallizerRecipes.instance().getCrystallizationList().forEach(this::addBackup);
        CrystallizerRecipes.instance().getCrystallizationList().clear();
    }

    @RecipeBuilderDescription(example = @Example(".input(item('minecraft:clay')).output(item('minecraft:diamond')).xp(0.5)"))
    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    @Property(property = "input", comp = @Comp(eq = 1))
    @Property(property = "output", comp = @Comp(gte = 1, lte = 2))
    public static class RecipeBuilder extends AbstractRecipeBuilder<Crystallization> {
        @Property(comp = @Comp(gte = 0))
        private float xp;

        @RecipeBuilderMethodDescription
        public RecipeBuilder xp(float xp) {
            this.xp = xp;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding AbyssalTweaker Crystallization recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            validateItems(msg, 1, 1, 1, 2);
            msg.add(xp < 0, "XP must be 0 or greater");
        }

        @Override
        @Nullable
        @RecipeBuilderRegistrationMethod
        public Crystallization register() {
            if (!validate()) return null;
            Crystallization t = null;
            for (ItemStack in : input.get(0).getMatchingStacks()) {
                t = new Crystallization(in, output.get(0), output.getOrEmpty(1), xp);
                GSPlugin.instance.crystallizer.add(t);
            }
            return t;
        }
    }
}
