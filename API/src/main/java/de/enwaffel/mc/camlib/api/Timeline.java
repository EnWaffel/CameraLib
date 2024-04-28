package de.enwaffel.mc.camlib.api;

import java.util.ArrayList;
import java.util.List;

public interface Timeline extends Animatable {

    class Builder {

        private final List<Animation> animations = new ArrayList<>();

        public Builder() {
        }

        public void addAnimation(Animation animation) {
            animations.add(animation);
        }

        public Timeline build() {
            return null;
        }

    }

}
