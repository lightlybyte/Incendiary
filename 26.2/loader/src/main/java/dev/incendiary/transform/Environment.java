package dev.incendiary.transform;

import dev.incendiary.api.Side;

public final class Environment {

    private static final Side CURRENT = detect();
    private static final boolean CLIENT_CLASSES_PRESENT = current() != Side.SERVER;

    private Environment() {}

    private static Side detect() {
        try {
            Class.forName("net.minecraft.client.Minecraft", false,
                Environment.class.getClassLoader());
            return Side.CLIENT;
        } catch (ClassNotFoundException e) {
            return Side.SERVER;
        }
    }

    public static Side current() {
        return CURRENT;
    }

    public static boolean clientClassesPresent() {
        return CLIENT_CLASSES_PRESENT;
    }

    /**
     * Returns true if a hook tagged with `hookSide` should be applied
     * in the current environment.
     */
    public static boolean shouldApply(Side hookSide) {
        return switch (hookSide) {
            case BOTH -> true;
            case CLIENT -> CURRENT == Side.CLIENT;
            case SERVER -> CURRENT == Side.SERVER;
        };
    }
}