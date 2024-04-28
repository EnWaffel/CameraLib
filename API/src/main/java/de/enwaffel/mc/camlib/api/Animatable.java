package de.enwaffel.mc.camlib.api;

public interface Animatable {
    void play();
    void stop();
    void pause();
    void resume();
    void update(float delta);
}
