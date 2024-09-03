package com.teamdimensional.abyssaltweaker.compat.groovyscript;

import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.helper.ingredient.ItemsIngredient;
import com.cleanroommc.groovyscript.helper.ingredient.OreDictIngredient;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class GSHelpers {
    public static IIngredient ritualInputToIngredient(Object o) {
        if (o instanceof String) {
            return new OreDictIngredient((String) o);
        } else if (o instanceof ItemStack) {
            return new ItemsIngredient((ItemStack) o);
        } else if (o instanceof Block) {
            return new ItemsIngredient(new ItemStack(((Block) o)));
        } else if (o instanceof Item) {
            return new ItemsIngredient(new ItemStack(((Item) o)));
        } else if (o instanceof ItemStack[]) {
            return new ItemsIngredient((ItemStack[]) o);
        } else if (o instanceof List) {
            List<ItemStack> newList = new ArrayList<>();
            for (Object o1 : (List<?>) o) {
                if (o1 instanceof ItemStack) {
                    newList.add((ItemStack) o1);
                }
            }
            return new ItemsIngredient(newList);
        } else return new ItemsIngredient();
    }

    public static boolean compareItemstacks(IIngredient ingredient, ItemStack s) {
        if (ingredient.test(s)) return true;
        // Abyssalcraft has a lot of recipes with input metadata 32767, so we have to check for those too
        if (s.getMetadata() == OreDictionary.WILDCARD_VALUE) {
            for (ItemStack s2 : ingredient.getMatchingStacks()) {
                if (s2.getItem() == s.getItem()) return true;
            }
        }
        return false;
    }
}
