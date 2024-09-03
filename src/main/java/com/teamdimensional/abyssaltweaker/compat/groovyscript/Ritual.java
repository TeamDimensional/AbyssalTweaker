package com.teamdimensional.abyssaltweaker.compat.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.*;
import com.cleanroommc.groovyscript.helper.SimpleObjectStream;
import com.cleanroommc.groovyscript.helper.ingredient.OreDictIngredient;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.shinoow.abyssalcraft.api.ritual.EnumRitualParticle;
import com.shinoow.abyssalcraft.api.ritual.NecronomiconInfusionRitual;
import com.shinoow.abyssalcraft.api.ritual.NecronomiconRitual;
import com.shinoow.abyssalcraft.api.ritual.RitualRegistry;
import com.teamdimensional.abyssaltweaker.Tags;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RegistryDescription(linkGenerator = Tags.MOD_ID, admonition = @Admonition(value = "groovyscript.wiki.abyssaltweaker.ritual.note", type = Admonition.Type.WARNING))
public class Ritual extends VirtualizedRegistry<NecronomiconRitual> {
    @Override
    @GroovyBlacklist
    public void onReload() {
        RitualRegistry.instance().getRituals().removeAll(removeScripted());
        RitualRegistry.instance().getRituals().addAll(restoreFromBackup());
    }

    public void add(NecronomiconRitual ritual) {
        RitualRegistry.instance().registerRitual(ritual);
        addScripted(ritual);
    }

    public boolean remove(NecronomiconRitual ritual) {
        if (RitualRegistry.instance().getRituals().remove(ritual)) {
            addBackup(ritual);
            return true;
        }
        return false;
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public SimpleObjectStream<NecronomiconRitual> streamRecipes() {
        return new SimpleObjectStream<>(RitualRegistry.instance().getRituals()).setRemover(this::remove);
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, example = @Example("item('abyssalcraft:oc')"))
    public boolean removeByOutput(IIngredient output) {
        return RitualRegistry.instance().getRituals().removeIf(r -> {
            if (r instanceof NecronomiconInfusionRitual && output.test(((NecronomiconInfusionRitual) r).getItem())) {
                addBackup(r);
                return true;
            }
            return false;
        });
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, example = @Example("item('abyssalcraft:lifecrystal')"))
    public boolean removeByCenter(IIngredient input) {
        return RitualRegistry.instance().getRituals().removeIf(r -> {
            IIngredient sac = GSHelpers.ritualInputToIngredient(r.getSacrifice());
            if (Arrays.stream(input.getMatchingStacks()).anyMatch(sac)) {
                addBackup(r);
                return true;
            }
            return false;
        });
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, example = @Example("'dreadInfusedGatewayKey'"))
    public boolean removeByName(String name) {
        return RitualRegistry.instance().getRituals().removeIf(r -> {
            if (r.getID().equals(name)) {
                addBackup(r);
                return true;
            }
            return false;
        });
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, priority = 2000, example = @Example(commented = true))
    public void removeAll() {
        RitualRegistry.instance().getRituals().forEach(this::addBackup);
        RitualRegistry.instance().getRituals().clear();
    }

    @RecipeBuilderDescription(example = @Example(".name('starInfusion').input(item('minecraft:clay'), item('minecraft:diamond'), ore('ingotGold'), ore('ingotIron')).output(item('minecraft:nether_star')).pe(500).requiresSacrifice().bookTier(3).dimension(50)"))
    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    @Property(property = "input", valid = {@Comp(value = "1", type = Comp.Type.GTE), @Comp(value = "9", type = Comp.Type.LTE)})
    @Property(property = "output", valid = @Comp("1"))
    public static class RecipeBuilder extends AbstractRecipeBuilder<NecronomiconInfusionRitual> {

        @Property
        private boolean sacrifice = false;

        @Property(valid = @Comp(value = "1", type = Comp.Type.GTE))
        private int pe = 0;

        @Property(valid = @Comp(value = "empty", type = Comp.Type.NOT))
        private String name = "";

        @Property(valid = {@Comp(value = "0", type = Comp.Type.GTE), @Comp(value = "4", type = Comp.Type.LTE)})
        private int bookTier = 0;

        @Property(defaultValue = "any dimension")
        private int dimension = OreDictionary.WILDCARD_VALUE;

        @Property(defaultValue = "3", valid = {@Comp(value = "0", type = Comp.Type.GTE), @Comp(type = Comp.Type.LT, value = "8")})
        private int particle = 3;

        @RecipeBuilderMethodDescription
        public RecipeBuilder sacrifice(boolean sacrifice) {
            this.sacrifice = sacrifice;
            return this;
        }

        @RecipeBuilderMethodDescription
        public RecipeBuilder name(String name) {
            this.name = name;
            return this;
        }

        @RecipeBuilderMethodDescription(field = "bookTier")
        public RecipeBuilder bookTier(int bookTier) {
            this.bookTier = bookTier;
            return this;
        }

        @RecipeBuilderMethodDescription
        public RecipeBuilder dimension(int dimension) {
            this.dimension = dimension;
            return this;
        }

        @RecipeBuilderMethodDescription
        public RecipeBuilder pe(int pe) {
            this.pe = pe;
            return this;
        }

        @RecipeBuilderMethodDescription(field = "pe")
        public RecipeBuilder energy(int energy) {
            return pe(energy);
        }

        @RecipeBuilderMethodDescription
        public RecipeBuilder particle(int particle) {
            this.particle = particle;
            return this;
        }

        @RecipeBuilderMethodDescription(field = "bookTier")
        public RecipeBuilder bookTier(String bookTier) {
            switch (bookTier.toLowerCase(Locale.ROOT)) {
                case "normal":
                case "overworld":
                    this.bookTier = 0;
                    break;
                case "wasteland":
                case "abyssalwasteland":
                case "abyssal_wasteland":
                    this.bookTier = 1;
                    break;
                case "dreadlands":
                    this.bookTier = 2;
                    break;
                case "omothol":
                    this.bookTier = 3;
                    break;
                case "abyssalnomicon":
                    this.bookTier = 4;
                    break;
                default:
                    GroovyLog.msg("Error parsing Necronomicon Tier argument").add("Invalid book name: {}", bookTier).warn().post();
            }
            return this;
        }

        @RecipeBuilderMethodDescription(field = "sacrifice")
        public RecipeBuilder requiresSacrifice() {
            return sacrifice(true);
        }

        @Override
        public String getErrorMsg() {
            return "Error adding AbyssalTweaker Necronomicon Infusion";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            validateItems(msg, 1, 9, 1, 1);
            msg.add(pe <= 0, "PE cost must be 1 or more");
            msg.add(name.isEmpty(), "Ritual name must not be empty");
            msg.add(RitualRegistry.instance().getRitual(name) != null, "Ritual with the name {} already exists", name);
            msg.add(bookTier < 0 || bookTier > 4, "Book tier must be between 0 and 4");
            int particleCount = EnumRitualParticle.values().length;
            msg.add(particle < 0 || particle >= particleCount, "Book tier must be between 0 and {}", particleCount);
            // NOTE: this breaks with the config tweaking because config tweaking runs in INIT and this recipe is first added in PRE INIT
            // msg.add(dimension == OreDictionary.WILDCARD_VALUE || DimensionDataRegistry.instance().getDataForDim(dimension) != null, "Rituals cannot be performed in dimension {}", dimension);
        }

        private Object[] makeInputs() {
            List<Object> inputs = new ArrayList<>();
            for (IIngredient ing : input.subList(1, input.size())) {
                inputs.add(makeObject(ing));
            }
            return inputs.toArray();
        }

        private Object makeObject(IIngredient ing) {
            if (ing instanceof OreDictIngredient) {
                return ((OreDictIngredient) ing).getOreDict();
            } else {
                return ing.getMatchingStacks();
            }
        }

        @Override
        @Nullable
        @RecipeBuilderRegistrationMethod
        public NecronomiconInfusionRitual register() {
            if (!validate()) return null;
            NecronomiconInfusionRitual ritual = new NecronomiconInfusionRitual(name, bookTier, dimension, pe, sacrifice, output.get(0), makeObject(input.get(0)), makeInputs());
            ritual.setRitualParticle(EnumRitualParticle.fromId(particle));
            GSPlugin.instance.ritual.add(ritual);
            return ritual;
        }
    }

}
