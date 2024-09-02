package com.teamdimensional.abyssaltweaker;

import com.shinoow.abyssalcraft.api.dimension.DimensionData;
import com.shinoow.abyssalcraft.api.dimension.DimensionDataRegistry;
import com.shinoow.abyssalcraft.api.ritual.RitualRegistry;

import java.util.*;

public class ConfigHelper {
    private static class TwoNumbers {
        int a;
        int b;
    }

    private static List<TwoNumbers> convertStrings(String[] strings) {
        List<TwoNumbers> list = new ArrayList<>();
        for (String s : strings) {
            String[] split = s.split(",");
            TwoNumbers twoNumbers = new TwoNumbers();
            twoNumbers.a = Integer.parseInt(split[0]);
            twoNumbers.b = Integer.parseInt(split[1]);
            list.add(twoNumbers);
        }
        return list;
    }

    private static Map<Integer, Integer> listToMap(List<TwoNumbers> pairs, String name) {
        Map<Integer, Integer> map = new HashMap<>();
        for (TwoNumbers t : pairs) {
            if (map.containsKey(t.a)) {
                throw new IllegalArgumentException(String.format("Duplicate key found in %s: %d", name, t.a));
            }
            map.put(t.a, t.b);
        }
        return map;

    }

    private static List<TwoNumbers> makeDimensionPairs() {
        return convertStrings(AbyssalTweakerConfig.dimension_pairs);
    }

    private static Map<Integer, Integer> makeDimensionKeyTiers() {
        return listToMap(convertStrings(AbyssalTweakerConfig.dimension_key_tiers), "dimension_key_tiers");
    }

    private static Map<Integer, Integer> makeDimensionBookTiers() {
        return listToMap(convertStrings(AbyssalTweakerConfig.dimension_book_tiers), "dimension_book_tiers");
    }

    private static DimensionData.Builder toBuilder(DimensionData data) {
        DimensionData.Builder b = new DimensionData.Builder(data.getId())
            .setColor(data.getR(), data.getG(), data.getB())
            .setGatewayKey(data.getGatewayKey())
            .setMob(data.getMobClass())
            .setOverlay(data.getOverlay());
        for (int dim : data.getConnectedDimensions()) b.addConnectedDimension(dim);
        return b;
    }

    public static void patchConnectedDimensions() {
        Map<Integer, Integer> map = makeDimensionKeyTiers();
        Map<Integer, DimensionData.Builder> dimKeys = new HashMap<>();
        for (DimensionData dim : DimensionDataRegistry.instance().getDimensions()) {
            dimKeys.put(dim.getId(), toBuilder(dim));
        }
        DimensionDataRegistry.instance().getDimensions().removeIf(d -> map.containsKey(d.getId()));
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            int dimension = entry.getKey(), tier = entry.getValue();
            DimensionData.Builder dim = dimKeys.getOrDefault(dimension, new DimensionData.Builder(dimension));
            DimensionDataRegistry.instance().registerDimensionData(dim.setGatewayKey(tier).build());
        }

        for (Map.Entry<Integer, Integer> entry : makeDimensionBookTiers().entrySet()) {
            int dimension = entry.getKey(), tier = entry.getValue();
            RitualRegistry.instance().addDimensionToBookType(dimension, tier);
        }

        List<TwoNumbers> pairs = makeDimensionPairs();
        if (!pairs.isEmpty()) {
            DimensionDataRegistry.instance().getDimensions().forEach(d -> d.getConnectedDimensions().clear());
            for (TwoNumbers t : pairs) {
                DimensionData dim = DimensionDataRegistry.instance().getDataForDim(t.a);
                if (dim == null) {
                    dim = new DimensionData.Builder(t.a).setGatewayKey(0).build();
                    DimensionDataRegistry.instance().registerDimensionData(dim);
                }
                dim.getConnectedDimensions().add(t.b);

                dim = DimensionDataRegistry.instance().getDataForDim(t.b);
                if (dim == null) {
                    dim = new DimensionData.Builder(t.b).setGatewayKey(0).build();
                    DimensionDataRegistry.instance().registerDimensionData(dim);
                }
                dim.getConnectedDimensions().add(t.a);
            }
        }
    }
}
