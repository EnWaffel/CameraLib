package de.enwaffel.mc.camlib.nms;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public interface NMS {
    void sendPlayerPositionPacket(Player player, float x, float y, float z, float yaw, float pitch);
    PlayerConnection getPlayerConnection(Player player);

    // For Paper servers
    void disableMovementPackets(Player player);
    void enableMovementPackets(Player player);

    record PlayerConnection(Object instance, Method sendMethod) {
    }
}
