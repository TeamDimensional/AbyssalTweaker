package com.teamdimensional.abyssaltweaker.compat.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.*;
import com.cleanroommc.groovyscript.helper.Alias;
import com.cleanroommc.groovyscript.helper.SimpleObjectStream;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.shinoow.abyssalcraft.api.APIUtils;
import com.shinoow.abyssalcraft.api.recipe.Materialization;
import com.shinoow.abyssalcraft.api.recipe.MaterializerRecipes;
import com.teamdimensional.abyssaltweaker.Tags;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;

@RegistryDescription(linkGenerator = Tags.MOD_ID, admonition = @Admonition(value = "groovyscript.wiki.abyssaltweaker.materializer.note", type = Admonition.Type.WARNING))
public class RegistryMaterialization extends VirtualizedRegistry<Materialization> {
    public RegistryMaterialization() {
        super(Alias.generateOf("Materialization"));
    }

    @Override
    public void onReload() {
        MaterializerRecipes.instance().getMaterializationList().removeAll(removeScripted());
        MaterializerRecipes.instance().getMaterializationList().addAll(restoreFromBackup());
    }

    public void add(Materialization t) {
        MaterializerRecipes.instance().getMaterializationList().add(t);
        addScripted(t);
    }

    public boolean remove(Materialization t) {
        if (MaterializerRecipes.instance().getMaterializationList().remove(t)) {
            addBackup(t);
            return true;
        }
        return false;
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public SimpleObjectStream<Materialization> streamRecipes() {
        return new SimpleObjectStream<>(MaterializerRecipes.instance().getMaterializationList()).setRemover(this::remove);
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, example = @Example("item('minecraft:bone')"))
    public boolean removeByOutput(IIngredient output) {
        return MaterializerRecipes.instance().getMaterializationList().removeIf(r -> {
            if (GSHelpers.compareItemstacks(output, r.output)) {
                addBackup(r);
                return true;
            }
            return false;
        });
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, example = @Example("item('abyssalcraft:phosphorus_crystal')"))
    public boolean removeByInput(IIngredient input) {
        return MaterializerRecipes.instance().getMaterializationList().removeIf(r -> {
            if (Arrays.stream(r.input).anyMatch(s -> GSHelpers.compareItemstacks(input, s))) {
                addBackup(r);
                return true;
            }
            return false;
        });
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, priority = 2000, example = @Example(commented = true))
    public void removeAll() {
        MaterializerRecipes.instance().getMaterializationList().forEach(this::addBackup);
        MaterializerRecipes.instance().getMaterializationList().clear();
    }

    @RecipeBuilderDescription(example = @Example(".input(item('abyssalcraft:coralium_crystal')).output(item('minecraft:diamond'))"))
    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    @Property(property = "input", comp = @Comp(gte = 1, lte = 5))
    @Property(property = "output", comp = @Comp(eq = 1))
    public static class RecipeBuilder extends AbstractRecipeBuilder<Materialization> {
        @Override
        public String getErrorMsg() {
            return "Error adding AbyssalTweaker Materialization recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            validateItems(msg, 1, 5, 1, 1);
            for (IIngredient ing : input) {
                if (ing.getMatchingStacks().length == 0) {
                    msg.add("An empty ingredient was provided");
                } else {
                    msg.add(!APIUtils.isCrystal(ing.getMatchingStacks()[0]), "All Materializer inputs must be crystals");
                }
            }
        }

        @Override
        @Nullable
        @RecipeBuilderRegistrationMethod
        public Materialization register() {
            if (!validate()) return null;
            ItemStack[] inputs = new ItemStack[input.size()];
            for (int i = 0; i < input.size(); i++) {
                inputs[i] = input.get(i).getMatchingStacks()[0];
            }
            Materialization t = new Materialization(inputs, output.get(0));
            GSPlugin.instance.materializer.add(t);
            return t;
        }
    }
}
