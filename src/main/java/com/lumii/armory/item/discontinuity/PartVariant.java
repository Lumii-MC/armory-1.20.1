package com.lumii.armory.item.discontinuity;

public enum PartVariant {
    HANDLE_STANDARD("Standard", PartGroup.HANDLE),
    HANDLE_GRIP("Grippy", PartGroup.HANDLE),
    HANDLE_LONG("Elongated", PartGroup.HANDLE),

    MID_BLADE("Blade", PartGroup.MIDDLE),
    MID_HOOK("Hook", PartGroup.MIDDLE),
    MID_AXE("Axe", PartGroup.MIDDLE),

    END_POINTY("Pointy", PartGroup.END),
    END_HARPOON("Harpoon", PartGroup.END),
    END_SPEARHEAD("Spearhead", PartGroup.END);

    private final String name;
    private final PartGroup group;
    PartVariant(String name, PartGroup group) {
        this.name = name;
        this.group = group;
    }

    public String getName() {
        return name + " " + group.getName();
    }
}
