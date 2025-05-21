package com.example.price_comaprator_backend;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UnitConverter {

    public enum BaseUnitType {
        WEIGHT, VOLUME, COUNT, UNKNOWN
    }


    private static final Map<String, String> baseUnitMap = new HashMap<>();
    private static final Map<String, Double> conversionFactors = new HashMap<>();
    private static final Map<String, BaseUnitType> unitTypeMap = new HashMap<>();


    public static final Set<String> ALWAYS_COUNT_CATEGORIES = Set.of(
            "electronice", "electrocasnice", "it", "telefoane", "gaming", "carti", "jucarii"

    );

    public static boolean isAlwaysCountCategory(String category) {
        if (category == null) return false;
        return ALWAYS_COUNT_CATEGORIES.contains(category.toLowerCase().trim());
    }



    static {

        baseUnitMap.put("g", "kg");      conversionFactors.put("g", 0.001);      unitTypeMap.put("g", BaseUnitType.WEIGHT);
        baseUnitMap.put("kg", "kg");     conversionFactors.put("kg", 1.0);       unitTypeMap.put("kg", BaseUnitType.WEIGHT);
        baseUnitMap.put("mg", "kg");     conversionFactors.put("mg", 0.000001);  unitTypeMap.put("mg", BaseUnitType.WEIGHT);


        baseUnitMap.put("ml", "l");      conversionFactors.put("ml", 0.001);     unitTypeMap.put("ml", BaseUnitType.VOLUME);
        baseUnitMap.put("l", "l");       conversionFactors.put("l", 1.0);        unitTypeMap.put("l", BaseUnitType.VOLUME);
        baseUnitMap.put("cl", "l");      conversionFactors.put("cl", 0.01);      unitTypeMap.put("cl", BaseUnitType.VOLUME);


        String[] countUnits = {"unit", "units", "buc", "bucata", "pachet", "pachete", "pcs", "pc", "item", "items", "pack", "packs", "each", "rola", "role", "doza", "doze", "set"};
        for (String unit : countUnits) {
            String lowerUnit = unit.toLowerCase().trim();
            baseUnitMap.put(lowerUnit, "item");
            conversionFactors.put(lowerUnit, 1.0);
            unitTypeMap.put(lowerUnit, BaseUnitType.COUNT);
        }
        baseUnitMap.put("dozen", "item"); conversionFactors.put("dozen", 12.0); unitTypeMap.put("dozen", BaseUnitType.COUNT);
    }

    public static String getBaseUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) return "unknown";
        return baseUnitMap.getOrDefault(unit.toLowerCase().trim(), unit.toLowerCase().trim());
    }

    public static Double getConversionFactor(String unit) {
        if (unit == null || unit.trim().isEmpty()) return null;
        return conversionFactors.get(unit.toLowerCase().trim());
    }

    public static BaseUnitType getBaseUnitType(String unit) {
        if (unit == null || unit.trim().isEmpty()) return BaseUnitType.UNKNOWN;
        return unitTypeMap.getOrDefault(unit.toLowerCase().trim(), BaseUnitType.UNKNOWN);
    }
}