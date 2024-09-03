package com.teamdimensional.abyssaltweaker.compat.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.IScriptReloadable;
import com.cleanroommc.groovyscript.api.documentation.annotations.Example;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.registry.NamedRegistry;
import com.shinoow.abyssalcraft.api.AbyssalCraftAPI;
import com.shinoow.abyssalcraft.api.event.FuelBurnTimeEvent;
import com.teamdimensional.abyssaltweaker.Tags;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
@RegistryDescription(category = RegistryDescription.Category.ENTRIES, linkGenerator = Tags.MOD_ID)
public class Fuel extends NamedRegistry implements IScriptReloadable {

    private static final Map<IIngredient, Integer> transmutatorFuels = new HashMap<>();
    private static final Map<IIngredient, Integer> crystallizerFuels = new HashMap<>();

    @Override
    public void onReload() {
        transmutatorFuels.clear();
        crystallizerFuels.clear();
    }

    @Override
    public void afterScriptLoad() {}

    @MethodDescription(example = @Example("item('minecraft:cobblestone'), 50"), type = MethodDescription.Type.ADDITION)
    public void addTransmutator(IIngredient stack, int fuel) {
        if (fuel < 0) {
            GroovyLog.msg("Invalid Transmutator fuel supplied!").add("Fuel duration must be 0 or greater, got {}", fuel).error().post();
            return;
        }
        transmutatorFuels.put(stack, fuel);
    }

    @MethodDescription(example = @Example("item('minecraft:blaze_rod')"))
    public void removeTransmutator(IIngredient stack) {
        addTransmutator(stack, 0);
    }

    @MethodDescription(example = @Example("item('minecraft:stone'), 50"), type = MethodDescription.Type.ADDITION)
    public void addCrystallizer(IIngredient stack, int fuel) {
        if (fuel < 0) {
            GroovyLog.msg("Invalid Crystallizer fuel supplied!").add("Fuel duration must be 0 or greater, got {}", fuel).error().post();
            return;
        }
        crystallizerFuels.put(stack, fuel);
    }

    @MethodDescription(example = @Example("item('abyssalcraft:dreadshard')"))
    public void removeCrystallizer(IIngredient stack) {
        addCrystallizer(stack, 0);
    }

    @SubscribeEvent
    public static void modifyFuelValue(FuelBurnTimeEvent e) {
        if (e.getFuelType() == AbyssalCraftAPI.FuelType.TRANSMUTATOR) {
            for (Map.Entry<IIngredient, Integer> i : transmutatorFuels.entrySet()) {
                if (i.getKey().test(e.getItemStack())) e.setBurnTime(i.getValue());
            }
        } else if (e.getFuelType() == AbyssalCraftAPI.FuelType.CRYSTALLIZER) {
            for (Map.Entry<IIngredient, Integer> i : crystallizerFuels.entrySet()) {
                if (i.getKey().test(e.getItemStack())) e.setBurnTime(i.getValue());
            }
        }
    }

}
