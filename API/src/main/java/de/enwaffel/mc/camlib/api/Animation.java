package de.enwaffel.mc.camlib.api;

import de.enwaffel.mc.camlib.api.tween.Easing;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface Animation extends Animatable {

    class Builder {

        private Location start;
        private Location end;
        private final List<Location> points = new ArrayList<>();
        private float ms;
        private Easing easing;
        private Player player;
        private boolean linear = false;

        public Builder() {
        }

        public Builder player(Player player) {
            this.player = player;
            return this;
        }

        public Builder start(Location start) {
            this.start = start;
            return this;
        }

        public Builder end(Location end) {
            this.end = end;
            return this;
        }

        public Builder point(Location point) {
            points.add(point);
            return this;
        }

        public Builder points(Location... points) {
            this.points.addAll(List.of(points));
            return this;
        }

        public Builder points(Collection<Location> points) {
            this.points.addAll(points);
            return this;
        }

        public Builder time(float ms) {
            this.ms = ms;
            return this;
        }

        public Builder easing(Easing easing) {
            this.easing = easing;
            return this;
        }

        public Builder linear(boolean linear) {
            this.linear = linear;
            return this;
        }

        public Animation build() {
            try {
                Class<?> implClasses = Class.forName("de.enwaffel.mc.camlib.ImplClasses");
                Field field = implClasses.getDeclaredField("ANIMATION_CLASS");
                Class<? extends Animation> clazz = (Class<? extends Animation>) field.get(null);
                return clazz
                        .getDeclaredConstructor(Player.class, Location.class, Location.class, List.class, Float.class, Easing.class, Boolean.class)
                        .newInstance(player, start, end, points, ms, easing, linear);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Implementation class not found!", e);
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException("Important field not found! This version / CamLib file may be broken!", e);
            } catch (IllegalAccessException | NoSuchMethodException e) {
                throw new IllegalStateException("This should not be thrown! This version has probably been implemented wrong!", e);
            } catch (InvocationTargetException | InstantiationException e) {
                throw new RuntimeException("Failed to create animation!", e);
            }
        }

    }

}
