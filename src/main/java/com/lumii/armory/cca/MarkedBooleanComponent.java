package com.lumii.armory.cca;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;

public class MarkedBooleanComponent implements BooleanComponent, AutoSyncedComponent {
    private boolean value = false;
    @Override
    public boolean getValue() {
        return this.value;
    }

    @Override
    public void setValue(boolean setValue) {
        this.value = setValue;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.value = tag.getBoolean("value");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("value", this.value);
    }
}
