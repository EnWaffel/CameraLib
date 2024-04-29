package de.enwaffel.mc.camlib;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenAccessor;
import aurelienribon.tweenengine.TweenManager;
import de.enwaffel.mc.camlib.api.*;
import de.enwaffel.mc.camlib.impl.v1_20_R3.TimelineImpl;
import de.enwaffel.mc.camlib.nms.NMS;
import de.enwaffel.mc.camlib.nms.NMSVersion;
import io.netty.channel.ChannelHandler;
import org.bukkit.Location;
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
    public static CamLibConfig config;
    public static HashMap<Player, Timer> LOCKED_PLAYERS = new HashMap<>();

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
        config = new CamLibConfig();

        initialized = true;
        startTimer();
    }

    private static void update(float delta) {
        manager.update(delta);
        ANIMATABLES.forEach((animatable) -> animatable.update(delta));

        for (Animatable animatable : toRemove) {
            ANIMATABLES.remove(animatable);
        }
    }

    static void startTimer() {
        timer.schedule(new TimerTask() {

            long last = System.currentTimeMillis();

            @Override
            public void run() {
                long now = System.currentTimeMillis();
                float delta = now - last;
                last = now;
                update(Math.max(1.0f, delta));
            }
        }, 0, config.updateRate);
    }

    public static void disable() {
        if (!initialized) throw new IllegalStateException("Not initialized! Use CameraLibrary.getInstance() to initialize!");
        initialized = false;

        nms.disable();

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

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public void lockPlayer(Player player) {
        if (LOCKED_PLAYERS.containsKey(player)) return;
        if (config.blockPackets) nms.disableMovementPackets(player);
        Location location = player.getLocation().clone();

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                nms.sendPlayerPositionPacket(player, (float) location.getX(), (float) location.getY(), (float) location.getZ(), location.getYaw(), location.getPitch());
            }
        }, 0, config.updateRate);

        LOCKED_PLAYERS.put(player, t);
    }

    @Override
    public void unlockPlayer(Player player) {
        if (!LOCKED_PLAYERS.containsKey(player)) return;
        if (config.blockPackets) nms.enableMovementPackets(player);
        LOCKED_PLAYERS.remove(player).cancel();
    }

    @Override
    public boolean isPlayerLocked(Player player) {
        return LOCKED_PLAYERS.containsKey(player);
    }

}
