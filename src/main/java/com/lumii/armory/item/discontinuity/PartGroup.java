package com.lumii.armory.item.discontinuity;

public enum PartGroup {
    HANDLE("Handle"),
    MIDDLE("Middle"),
    END("End");

    private final String name;
    PartGroup(String name) {
        this.name = name;
    }
    public PartGroup next() {
        PartGroup[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    public String getName() {
        return name;
    }
}
