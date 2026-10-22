package com.lumii.armory.item.discontinuity;

public enum PartGroup {
    HANDLE("Handle"),
    MIDDLE("Middle"),
    END("End");

    private final String name;
    PartGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
