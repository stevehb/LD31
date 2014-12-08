package com.sblackwell.ld31.types;

public enum EntityType {
    DROPPER(0),
    PLATFORM(1),
    SLOPE(1),
    TOP_BALL(2),
    BOTTOM_BALL(2),
    STICK_HANDS(3),
    CHARCOAL_EYES(3),
    CARROT_NOSE(3),
    TOP_HAT(3),
    DISPLAY_HANDS(0),
    DISPLAY_EYES(0),
    DISPLAY_NOSE(0),
    DISPLAY_HAT(0);

    public static EntityType[] array = EntityType.values();
    public short groupIdx;

    EntityType(int groupIdx) {
        this.groupIdx = (short)groupIdx;
    }
}
