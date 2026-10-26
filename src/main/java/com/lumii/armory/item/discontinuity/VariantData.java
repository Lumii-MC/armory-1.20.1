package com.lumii.armory.item.discontinuity;

import java.util.Random;

public record VariantData(PartVariant handle, PartVariant mid, PartVariant end) {
    public static VariantData random() {
        Random random = new Random();
        return new VariantData(PartVariant.values()[random.nextInt(0, 5)], PartVariant.values()[random.nextInt(5, 8)], PartVariant.values()[random.nextInt(8, 13)]);
    }

    public PartVariant get(PartGroup group) {
        return switch (group) {
            case HANDLE -> handle;
            case MIDDLE -> mid;
            case END -> end;
        };
    }

    public VariantData with(PartGroup group, PartVariant variant) {
        return switch (group) {
            case HANDLE -> new VariantData(variant, mid, end);
            case MIDDLE -> new VariantData(handle, variant, end);
            case END -> new VariantData(handle, mid, variant);
        };
    }
}
