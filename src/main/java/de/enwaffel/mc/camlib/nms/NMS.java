package de.enwaffel.mc.camlib.nms;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface NMS {

    void sendPlayerPositionPacket(Player player, float x, float y, float z, float yaw, float pitch);
    void sendEntityPositionPacket(Entity entity);
    void setEntityPosition(EntityLocFields fields, float x, float y, float z, float yaw, float pitch);
    EntityLocFields getEntityLocationFields(Entity entity);
    EntityClassPair getNMSEntityRaw(Entity entity);
    PlayerConnection getPlayerConnection(Player player);

    void disable();
    void clearCacheForPlayer(Player player);
    void clearCacheForEntity(Entity entity);
    // For Paper servers
    void disableMovementPackets(Player player);

    void enableMovementPackets(Player player);

    record PlayerConnection(Object instance, Method sendMethod) {
    }

    record EntityClassPair(Object entity, Class<?> clazz) {
    }

    record EntityLocFields(Field x, Field y, Field z, Field yaw, Field pitch, Object vec, Object entity) {
    }

}
