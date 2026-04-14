package edu.icet.model.enums;

import java.util.Arrays;
import java.util.List;

public enum ClothingSize {
    XS("XS"),
    S("S"),
    M("M"),
    L("L"),
    XL("XL"),
    XXL("XXL"),
    FREE("One Size");

    private final String label;

    ClothingSize(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static List<ClothingSize> standardSizes() {
        return Arrays.asList(XS, S, M, L, XL, XXL);
    }

    public static ClothingSize fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (ClothingSize size : values()) {
            if (size.name().equalsIgnoreCase(value.trim()) || size.label.equalsIgnoreCase(value.trim())) {
                return size;
            }
        }
        return null;
    }
}
