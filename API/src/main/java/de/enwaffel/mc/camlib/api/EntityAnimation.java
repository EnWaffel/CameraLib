package de.enwaffel.mc.camlib.api;

import de.enwaffel.mc.camlib.api.tween.Easing;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface EntityAnimation extends Animation {

    class Builder {

        private Location start;
        private Location end;
        private final List<Location> points = new ArrayList<>();
        private float ms;
        private Easing easing;
        private Entity entity;
        private boolean linear = false;

        public Builder() {
        }

        public EntityAnimation.Builder entity(Entity entity) {
            this.entity = entity;
            return this;
        }

        public EntityAnimation.Builder start(Location start) {
            this.start = start;
            return this;
        }

        public EntityAnimation.Builder end(Location end) {
            this.end = end;
            return this;
        }

        public EntityAnimation.Builder point(Location point) {
            points.add(point);
            return this;
        }

        public EntityAnimation.Builder points(Location... points) {
            this.points.addAll(List.of(points));
            return this;
        }

        public EntityAnimation.Builder points(Collection<Location> points) {
            this.points.addAll(points);
            return this;
        }

        public EntityAnimation.Builder time(float ms) {
            this.ms = ms;
            return this;
        }

        public EntityAnimation.Builder easing(Easing easing) {
            this.easing = easing;
            return this;
        }

        public EntityAnimation.Builder linear(boolean linear) {
            this.linear = linear;
            return this;
        }

        @Nullable
        public EntityAnimation build() {
            if (CameraLibrary.getInstance().isInAnimation(entity)) {
                return null;
            }

            try {
                Class<?> implClasses = Class.forName("de.enwaffel.mc.camlib.ImplClasses");
                Field field = implClasses.getDeclaredField("ENTITY_ANIMATION_CLASS");
                Class<? extends EntityAnimation> clazz = (Class<? extends EntityAnimation>) field.get(null);
                return clazz
                        .getDeclaredConstructor(Entity.class, Location.class, Location.class, List.class, Float.class, Easing.class, Boolean.class)
                        .newInstance(entity, start, end, points, ms, easing, linear);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Implementation class not found!", e);
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException("Important field not found! This version / Jar file may be broken!", e);
            } catch (IllegalAccessException | NoSuchMethodException e) {
                throw new IllegalStateException("This should not be thrown! This version has probably been implemented wrong!", e);
            } catch (InvocationTargetException | InstantiationException e) {
                throw new RuntimeException("Failed to create animation!", e);
            }
        }

    }

}
