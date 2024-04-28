package de.enwaffel.mc.camlib.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface CameraLibrary {

    static CameraLibrary getInstance() {
        try {
            Class<? extends CameraLibrary> clazz = (Class<? extends CameraLibrary>) Class.forName("de.enwaffel.mc.camlib.CamLib");
            Method instanceMethod = clazz.getDeclaredMethod("getInstance");
            Object instance = instanceMethod.invoke(null);
            if (instance == null) {
                clazz.getDeclaredMethod("init").invoke(null);
                return (CameraLibrary) instanceMethod.invoke(null);
            }
            return (CameraLibrary) instance;
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static void disable() {
        Class<? extends CameraLibrary> clazz = null;
        try {
            clazz = (Class<? extends CameraLibrary>) Class.forName("de.enwaffel.mc.camlib.CamLib");
            clazz.getDeclaredMethod("disable").invoke(null);
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    Timeline.Builder newTimeline();

    Animation.Builder newAnimation();

}
