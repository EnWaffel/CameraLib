package de.enwaffel.mc.camlib.impl.v1_20_R3;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenPaths;
import de.enwaffel.mc.camlib.CamLib;
import de.enwaffel.mc.camlib.api.Animation;
import de.enwaffel.mc.camlib.api.tween.Easing;
import de.enwaffel.mc.camlib.impl.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AnimationImpl implements Animation {

    protected float x;
    protected float y;
    protected float z;
    protected float yaw;
    protected float pitch;

    private final Player player;
    private final Tween tween;
    private final boolean blockPackets;

    public AnimationImpl(Player player, Location start, Location end, List<Location> points, Float ms, Easing ease, Boolean linear) {
        this.player = player;

        blockPackets = CamLib.config.blockPackets;

        if (CamLib.LOCKED_PLAYERS.containsKey(player)) {
            throw new IllegalStateException("Cannot create animation while " + player.getName() + " is locked!");
        }

        x = (float) start.getX();
        y = (float) start.getY();
        z = (float) start.getZ();
        yaw = start.getYaw();
        pitch = start.getPitch();

        Tween.setWaypointsLimit(points.size());
        Tween.setCombinedAttributesLimit(5);

        Tween tempTween = Tween.to(this, 0, ms).target((float) end.getX(), (float) end.getY(), (float) end.getZ(), end.getYaw(), end.getPitch());

        for (Location point : points) {
            tempTween.waypoint((float) point.getX(), (float) point.getY(), (float) point.getZ(), point.getYaw(), point.getPitch());
        }

        tempTween.ease(Utils.translateEasing(ease));
        if (linear) {
            tempTween.path(TweenPaths.linear);
        } else {
            tempTween.path(TweenPaths.catmullRom);
        }

        tween = tempTween.build();
    }

    @Override
    public void play() {
        tween.start(CamLib.manager);
        CamLib.ANIMATABLES.add(this);
        if (blockPackets) CamLib.nms.disableMovementPackets(player);
    }

    @Override
    public void stop() {
        tween.kill();
        CamLib.ANIMATABLES.remove(this);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugins()[0], () -> {
            if (blockPackets) CamLib.nms.enableMovementPackets(player);
            player.teleport(new Location(player.getWorld(), x, y, z, yaw, pitch));
        });
    }

    @Override
    public void pause() {
        tween.pause();
    }

    @Override
    public void resume() {
        tween.resume();
    }

    @Override
    public void update(float delta) {
        CamLib.nms.sendPlayerPositionPacket(player, x, y, z, yaw, pitch);
        if (tween.isFinished()) {
            CamLib.toRemove.add(this);
            Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugins()[0], () -> {
                if (blockPackets) CamLib.nms.enableMovementPackets(player);
                player.teleport(new Location(player.getWorld(), x, y, z, yaw, pitch));
            });
        }
    }

}
