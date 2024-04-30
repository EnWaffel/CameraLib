package de.enwaffel.mc.camlib.impl.v1_20_R3;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenPaths;
import de.enwaffel.mc.camlib.CamLib;
import de.enwaffel.mc.camlib.api.EntityAnimation;
import de.enwaffel.mc.camlib.api.tween.Easing;
import de.enwaffel.mc.camlib.impl.Utils;
import de.enwaffel.mc.camlib.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class EntityAnimationImpl implements EntityAnimation {

    protected float x;
    protected float y;
    protected float z;
    protected float yaw;
    protected float pitch;

    private final Entity entity;
    private final Tween tween;
    private final NMS.EntityLocFields locFields;

    public EntityAnimationImpl(Entity entity, Location start, Location end, List<Location> points, Float ms, Easing ease, Boolean linear) {
        this.entity = entity;

        if (entity instanceof Player) {
            throw new IllegalArgumentException("Entity cannot be a player!");
        }

        locFields = CamLib.nms.getEntityLocationFields(entity);

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
        CamLib.ANIMATING_ENTITIES.add(entity);
    }

    @Override
    public void stop() {
        tween.kill();
        CamLib.ANIMATABLES.remove(this);
        CamLib.nms.clearCacheForEntity(entity);
        CamLib.ANIMATING_ENTITIES.remove(entity);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugins()[0], () -> entity.teleport(new Location(entity.getWorld(), x, y, z, yaw, pitch)));
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
        CamLib.nms.setEntityPosition(locFields, x, y, z, yaw, pitch);
        CamLib.nms.sendEntityPositionPacket(entity);
        if (tween.isFinished()) {
            CamLib.ANIMATING_ENTITIES.remove(entity);
            CamLib.nms.clearCacheForEntity(entity);
            CamLib.toRemove.add(this);
            Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugins()[0], () -> entity.teleport(new Location(entity.getWorld(), x, y, z, yaw, pitch)));
        }
    }

}
