package de.enwaffel.mc.camlib;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenAccessor;
import aurelienribon.tweenengine.TweenManager;
import de.enwaffel.mc.camlib.api.Animatable;
import de.enwaffel.mc.camlib.api.Animation;
import de.enwaffel.mc.camlib.api.CameraLibrary;
import de.enwaffel.mc.camlib.api.Timeline;
import de.enwaffel.mc.camlib.impl.v1_20_R3.TimelineImpl;
import de.enwaffel.mc.camlib.nms.NMS;
import de.enwaffel.mc.camlib.nms.NMSVersion;
import io.netty.channel.ChannelHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class CamLib implements CameraLibrary {

    private static boolean initialized = false;
    private static CamLib instance;

    public static Timer timer;
    public static TweenManager manager;
    public static HashMap<Player, ChannelHandler> HANDLERS;
    public static NMS nms;
    public static List<Animatable> ANIMATABLES;
    public static List<Animatable> toRemove;

    public static void init() {
        if (initialized) throw new IllegalStateException("Already initialized!");

        String mcVersion = NMSVersion.VERSION;
        String implPackage = "de.enwaffel.mc.camlib.impl." + mcVersion;

        try {
            ImplClasses.TIMELINE_CLASS = (Class<? extends Timeline>) Class.forName(implPackage + ".TimelineImpl");
            ImplClasses.ANIMATION_CLASS = (Class<? extends Animation>) Class.forName(implPackage + ".AnimationImpl");
            nms = (NMS) Class.forName(implPackage + ".NMSImpl").getDeclaredConstructor().newInstance();

            Class<?> clazz = Class.forName(implPackage + ".AnimationAccessor");
            Tween.registerAccessor(ImplClasses.ANIMATION_CLASS, (TweenAccessor<?>) clazz.getDeclaredConstructor().newInstance());

        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        instance = new CamLib();
        timer = new Timer();
        manager = new TweenManager();
        HANDLERS = new HashMap<>();
        ANIMATABLES = new ArrayList<>();
        toRemove = new ArrayList<>();

        initialized = true;
        timer.schedule(new TimerTask() {

            long last = System.currentTimeMillis();

            @Override
            public void run() {
                long now = System.currentTimeMillis();
                float delta = now - last;
                last = now;
                update(Math.max(1.0f, delta));
            }
        }, 0, 1);
    }

    private static void update(float delta) {
        manager.update(delta);
        ANIMATABLES.forEach((animatable) -> animatable.update(delta));

        for (Animatable animatable : toRemove) {
            ANIMATABLES.remove(animatable);
        }
    }

    public static void disable() {
        if (!initialized) throw new IllegalStateException("Not initialized! Use CameraLibrary.getInstance() to initialize!");
        initialized = false;

        for (Map.Entry<Player, ChannelHandler> set : HANDLERS.entrySet()) {

        }

        manager.killAll();
        timer.cancel();
    }

    public static CamLib getInstance() {
        return instance;
    }

    @Override
    public TimelineImpl.Builder newTimeline() {
        return new Timeline.Builder();
    }

    @Override
    public Animation.Builder newAnimation() {
        return new Animation.Builder();
    }

}
