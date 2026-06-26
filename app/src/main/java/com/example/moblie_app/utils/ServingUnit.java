package com.example.moblie_app.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServingUnit {

    private final String label;
    private final double grams;
    private final String display;

    public ServingUnit(String label, double grams, String display) {
        this.label = label;
        this.grams = grams;
        this.display = display;
    }

    public String getLabel() { return label; }

    public double getGrams() { return grams; }

    public String getDisplay() { return display; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServingUnit that = (ServingUnit) o;
        return label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }

    public static final ServingUnit GRAM   = new ServingUnit("g",     1,   "g");
    public static final ServingUnit CUP    = new ServingUnit("hộp", 100,   "hộp (~100g)");
    public static final ServingUnit CAN    = new ServingUnit("lon",  330,   "lon (~330g)");
    public static final ServingUnit ML     = new ServingUnit("ml",     1,   "ml");
    public static final ServingUnit BOTTLE = new ServingUnit("chai", 330,   "chai (~330ml)");
    public static final ServingUnit BOWL   = new ServingUnit("chén", 150,   "chén (~150g)");
    public static final ServingUnit SPOON  = new ServingUnit("muỗng", 15,   "muỗng (~15g)");
    public static final ServingUnit PACKET = new ServingUnit("bịch", 200,   "bịch (~200g)");
    public static final ServingUnit SLICE  = new ServingUnit("miếng", 50,   "miếng (~50g)");

    private static final List<ServingUnit> ALL_PRESETS = Arrays.asList(
            GRAM, CUP, CAN, ML, BOTTLE, BOWL, SPOON, PACKET, SLICE
    );

    public static List<ServingUnit> parseOffServingSize(String servingSize) {
        List<ServingUnit> result = new ArrayList<>();
        result.add(GRAM);
        if (servingSize == null || servingSize.isEmpty()) {
            for (ServingUnit u : ALL_PRESETS) {
                if (!result.contains(u)) result.add(u);
            }
            return result;
        }
        String lower = servingSize.toLowerCase();
        if (lower.contains("cup") || lower.contains("hộp"))     result.add(CUP);
        if (lower.contains("can") || lower.contains("lon"))      result.add(CAN);
        if (lower.contains("bottle") || lower.contains("chai"))  result.add(BOTTLE);
        if (lower.contains("bowl") || lower.contains("chén"))    result.add(BOWL);
        if (lower.contains("spoon") || lower.contains("muỗng"))  result.add(SPOON);
        if (lower.contains("ml") || lower.contains("lít"))       result.add(ML);
        if (lower.contains("packet") || lower.contains("bịch"))  result.add(PACKET);
        if (lower.contains("slice") || lower.contains("miếng"))  result.add(SLICE);
        for (ServingUnit u : ALL_PRESETS) {
            if (!result.contains(u)) result.add(u);
        }
        return result;
    }

    public static List<ServingUnit> getDefaults() {
        return new ArrayList<>(ALL_PRESETS);
    }

    public static String[] displayArray(List<ServingUnit> units) {
        String[] arr = new String[units.size()];
        for (int i = 0; i < units.size(); i++) {
            arr[i] = units.get(i).display;
        }
        return arr;
    }

    public static String gramsToDisplay(double grams, String unitLabel, double unitQuantity) {
        if (unitLabel == null || unitLabel.isEmpty() || unitLabel.equals("g")) {
            return String.format(java.util.Locale.getDefault(), "%.0f g", grams);
        }
        return String.format(java.util.Locale.getDefault(), "%s %s", formatDecimal(unitQuantity), unitLabel);
    }

    private static String formatDecimal(double value) {
        if (value == (long) value) {
            return String.format(java.util.Locale.US, "%d", (long) value);
        }
        return String.format(java.util.Locale.US, "%.1f", value);
    }
}
