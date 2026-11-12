package net.quedoom.multiblock_miner.config;

import net.minecraft.util.StringRepresentable;

public enum OreBreakPosition implements StringRepresentable {
    MULTI_IF_SHIFT("if_shift"),
    MULTI_UNLESS_SHIFT("unless_shift"),
    ALWAYS_MULTI("always"),
    NEVER_MULTI("never");

    private final String translatable;

    OreBreakPosition(String translatable) {
        this.translatable = translatable;
    }

    @Override
    public String getSerializedName() {
        return this.translatable;
    }
}
