package com.teamdimensional.abyssaltweaker.compat.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.*;
import com.cleanroommc.groovyscript.helper.SimpleObjectStream;
import com.cleanroommc.groovyscript.helper.ingredient.OreDictIngredient;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.shinoow.abyssalcraft.api.ritual.*;
import com.teamdimensional.abyssaltweaker.Tags;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RegistryDescription(linkGenerator = Tags.MOD_ID, admonition = {
    @Admonition(value = "groovyscript.wiki.abyssaltweaker.ritual.note0", type = Admonition.Type.WARNING),
    @Admonition(value = "groovyscript.wiki.abyssaltweaker.ritual.note1", type = Admonition.Type.WARNING),
    @Admonition(value = "groovyscript.wiki.abyssaltweaker.ritual.note2", type = Admonition.Type.WARNING)
})
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
            if (r instanceof NecronomiconCreationRitual && output.test(((NecronomiconCreationRitual) r).getItem())) {
                addBackup(r);
                return true;
            }
            return false;
        });
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, example = @Example("entity('abyssalcraft:dragonboss')"))
    public boolean removeBySummonedMob(EntityEntry mob) {
        return RitualRegistry.instance().getRituals().removeIf(r -> {
            if (r instanceof NecronomiconSummonRitual && mob.getEntityClass().equals(((NecronomiconSummonRitual) r).getEntity())) {
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

    @RecipeBuilderDescription(example = {
        @Example(".name('simpleRitual').centerItem(item('minecraft:diamond')).input(item('minecraft:diamond')).output(item('minecraft:diamond_block')).pe(100)"),
        @Example(".name('starInfusion').centerItem(item('minecraft:clay')).input(item('minecraft:diamond'), ore('ingotGold'), ore('ingotIron')).output(item('minecraft:nether_star')).pe(500).requiresSacrifice().bookTier(3).dimension(50)"),
        @Example(".name('simpleCreation').input(item('minecraft:iron_ingot'), item('minecraft:iron_ingot'), item('minecraft:iron_ingot'), item('minecraft:iron_ingot')).output(item('minecraft:gold_ingot')).pe(100)"),
        @Example(".name('zombieSummoning').input(item('minecraft:rotten_flesh'), item('minecraft:iron_ingot'), item('minecraft:carrot'), item('minecraft:potato')).summonedMob(entity('minecraft:zombie')).pe(100)"),
    })
    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    @Property(property = "input", comp = @Comp(gte = 1, lte = 9))
    @Property(property = "output", comp = @Comp(eq = 1))
    public static class RecipeBuilder extends AbstractRecipeBuilder<NecronomiconRitual> {

        @Property
        private IIngredient centerItem = null;

        @Property
        private EntityEntry summonedMob = null;

        @Property
        private boolean sacrifice = false;

        @Property(comp = @Comp(gte = 1))
        private int pe = 0;

        @Property(comp = @Comp(not = "empty"))
        private String name = "";

        @Property(comp = @Comp(gte = 0, lte = 4))
        private int bookTier = 0;

        @Property(defaultValue = "any dimension")
        private int dimension = OreDictionary.WILDCARD_VALUE;

        @Property(comp = @Comp(gte = 0, lte = 8))
        private int particle = -1;

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

        @RecipeBuilderMethodDescription
        public RecipeBuilder centerItem(IIngredient centerItem) {
            this.centerItem = centerItem;
            return this;
        }

        @RecipeBuilderMethodDescription
        public RecipeBuilder summonedMob(EntityEntry summonedMob) {
            this.summonedMob = summonedMob;
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
            validateItems(msg, 0, 8, 0, 1);
            msg.add(centerItem == null && input.isEmpty(), "At least one input item must be set");
            msg.add(centerItem != null && summonedMob != null, "Summon rituals cannot have a center item");
            msg.add(summonedMob != null && !output.isEmpty(), "The ritual must either output an item or summon a mob, not both");
            msg.add(summonedMob == null && output.isEmpty(), "The ritual must either output an item or summon a mob");
            msg.add(summonedMob != null && !EntityLivingBase.class.isAssignableFrom(summonedMob.getEntityClass()), "Only subclasses of EntityLivingBase can be summoned");
            msg.add(pe <= 0, "PE cost must be 1 or more");
            msg.add(name.isEmpty(), "Ritual name must not be empty");
            msg.add(RitualRegistry.instance().getRitualById(name) != null, "Ritual with the name {} already exists", name);
            msg.add(bookTier < 0 || bookTier > 4, "Book tier must be between 0 and 4");
            int particleCount = EnumRitualParticle.values().length;
            msg.add(particle < -1 || particle >= particleCount, "Particle ID must be between 0 and {}", particleCount);
            // NOTE: this breaks with the config tweaking because config tweaking runs in INIT and this recipe is first added in PRE INIT
            // msg.add(dimension == OreDictionary.WILDCARD_VALUE || DimensionDataRegistry.instance().getDataForDim(dimension) != null, "Rituals cannot be performed in dimension {}", dimension);
        }

        private Object[] makeInputs() {
            List<Object> inputs = new ArrayList<>();
            for (IIngredient ing : input) {
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
        @SuppressWarnings("unchecked")
        public NecronomiconRitual register() {
            if (!validate()) return null;
            NecronomiconRitual ritual;
            if (summonedMob != null) {
                ritual = new NecronomiconSummonRitual(name, bookTier, dimension, pe, sacrifice, (Class<? extends EntityLivingBase>) summonedMob.getEntityClass(), makeInputs());
            } else if (centerItem != null) {
                ritual = new NecronomiconInfusionRitual(name, bookTier, dimension, pe, sacrifice, output.get(0), makeObject(centerItem), makeInputs());
            } else {
                ritual = new NecronomiconCreationRitual(name, bookTier, dimension, pe, sacrifice, output.get(0), makeInputs());
            }
            if (particle > -1) {
                ritual.setRitualParticle(EnumRitualParticle.fromId(particle));
            }
            GSPlugin.instance.ritual.add(ritual);
            return ritual;
        }
    }

}
