package de.enwaffel.mc.camlib.impl.v1_20_R3;

import de.enwaffel.mc.camlib.api.Timeline;

import java.util.List;

public class TimelineImpl implements Timeline {

    private final List<AnimationImpl> animations;

    TimelineImpl(List<AnimationImpl> animations) {
        this.animations = animations;
    }

    @Override
    public void play() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void update(float delta) {

    }

}
