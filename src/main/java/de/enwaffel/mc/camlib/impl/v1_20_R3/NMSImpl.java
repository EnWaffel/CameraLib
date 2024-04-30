package de.enwaffel.mc.camlib.impl.v1_20_R3;

import de.enwaffel.mc.camlib.nms.NMS;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class NMSImpl implements NMS {

    private final Class<?> positionPacketClass;
    private final Class<?> packetClass;
    private final Class<?> vecClass;
    private final Constructor<?> vecConstructor;
    private final Class<?> entityTeleportPacketClass;
    private final Class<?> entityClass;
    private final HashMap<Player, PlayerConnection> CONNECTION_CACHE = new HashMap<>();
    private final List<Player> DISABLED_PLAYERS = new ArrayList<>();
    private final HashMap<Entity, EntityLocFields> LOC_FIELDS_CACHE = new HashMap<>();

    public NMSImpl() {
        String nmsPackage = "net.minecraft";

        try {
            positionPacketClass = Class.forName(nmsPackage + ".network.protocol.game.PacketPlayOutPosition");
            packetClass = Class.forName(nmsPackage + ".network.protocol.Packet");
            vecClass = Class.forName(nmsPackage + ".world.phys.Vec3D");
            vecConstructor = vecClass.getDeclaredConstructor(Double.TYPE, Double.TYPE, Double.TYPE);
            entityTeleportPacketClass = Class.forName(nmsPackage + ".network.protocol.game.PacketPlayOutEntityTeleport");
            entityClass = Class.forName(nmsPackage + ".world.entity.Entity");
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendPlayerPositionPacket(Player player, float x, float y, float z, float yaw, float pitch) {
        try {
            Object packet = positionPacketClass
                    .getDeclaredConstructor(Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE, Set.class, Integer.TYPE)
                    .newInstance((double)x, (double)y, (double)z, yaw, pitch, Set.of(), 0);

            PlayerConnection connection = getPlayerConnection(player);
            connection.sendMethod().invoke(connection.instance(), packet);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendEntityPositionPacket(Entity entity) {
        Object packet;
        EntityClassPair pair = getNMSEntityRaw(entity);
        try {
            packet = entityTeleportPacketClass
                    .getDeclaredConstructor(entityClass)
                    .newInstance(pair.entity());

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = getPlayerConnection(player);
            try {
                connection.sendMethod().invoke(connection.instance(), packet);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public EntityLocFields getEntityLocationFields(Entity entity) {
        if (LOC_FIELDS_CACHE.containsKey(entity)) {
            return LOC_FIELDS_CACHE.get(entity);
        }

        try {
            EntityClassPair pair = getNMSEntityRaw(entity);

            Field vecField = pair.clazz().getDeclaredField("u");
            vecField.setAccessible(true);
            Object vec = vecField.get(pair.entity());
            vecField.setAccessible(false);

            Field xField = vec.getClass().getDeclaredField("c");
            Field yField = vec.getClass().getDeclaredField("d");
            Field zField = vec.getClass().getDeclaredField("e");
            Field yawField = pair.clazz().getDeclaredField("aG");
            Field pitchField = pair.clazz().getDeclaredField("aH");

            xField.setAccessible(true);
            yField.setAccessible(true);
            zField.setAccessible(true);
            yawField.setAccessible(true);
            pitchField.setAccessible(true);

            EntityLocFields fields = new EntityLocFields(xField, yField, zField, yawField, pitchField, vec, pair.entity());
            LOC_FIELDS_CACHE.put(entity, fields);
            return fields;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setEntityPosition(EntityLocFields fields, float x, float y, float z, float yaw, float pitch) {
        try {
            fields.x().set(fields.vec(), (double)x);
            fields.y().set(fields.vec(), (double)y);
            fields.z().set(fields.vec(), (double)z);
            fields.yaw().set(fields.entity(), yaw);
            fields.pitch().set(fields.entity(), pitch);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EntityClassPair getNMSEntityRaw(Entity entity) {
        Class<?> craftEntityClass = entity.getClass();
        do {
            craftEntityClass = craftEntityClass.getSuperclass();
        } while (!craftEntityClass.getSimpleName().equals("CraftEntity"));

        try {
            Field entityField = craftEntityClass.getDeclaredField("entity");
            entityField.setAccessible(true);
            Object nmsEntity = entityField.get(entity);
            Class<?> nmsEntityClass = nmsEntity.getClass();
            entityField.setAccessible(false);

            do {
                nmsEntityClass = nmsEntityClass.getSuperclass();
            } while (!nmsEntityClass.getSimpleName().equals("Entity"));

            return new EntityClassPair(nmsEntity, nmsEntityClass);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PlayerConnection getPlayerConnection(Player player) {
        if (CONNECTION_CACHE.containsKey(player)) return CONNECTION_CACHE.get(player);

        try {
            Method handleMethod = player.getClass().getSuperclass().getDeclaredMethod("getHandle");
            Object handle = handleMethod.invoke(player);

            Field playerConnField = handle.getClass().getDeclaredField("c");
            Object playerConn = playerConnField.get(handle);

            Method sendMethod = playerConn.getClass().getSuperclass().getDeclaredMethod("b", packetClass);

            PlayerConnection conn = new PlayerConnection(playerConn, sendMethod);
            CONNECTION_CACHE.put(player, conn);

            return conn;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disable() {
        DISABLED_PLAYERS.forEach(this::enableMovementPackets);
    }

    @Override
    public void clearCacheForPlayer(Player player) {
        CONNECTION_CACHE.remove(player);
    }

    @Override
    public void clearCacheForEntity(Entity entity) {
        LOC_FIELDS_CACHE.remove(entity);
    }

    @Override
    public void disableMovementPackets(Player player) {
        try {
            PlayerConnection playerConnection = getPlayerConnection(player);
            Field networkManagerField = playerConnection.instance().getClass().getSuperclass().getDeclaredField("c");
            networkManagerField.setAccessible(true);
            Object networkManager = networkManagerField.get(playerConnection.instance());
            networkManagerField.setAccessible(false);

            Field channelField = networkManager.getClass().getDeclaredField("n");
            Channel channel = (Channel) channelField.get(networkManager);

            channel.pipeline().addBefore("packet_handler", "injected", new ChannelDuplexHandler() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    String className = msg.getClass().getSimpleName();
                    if (className.equals("PacketPlayInPosition") || className.equals("PacketPlayInPositionLook") || className.equals("PacketPlayInTeleportAccept")) {
                        return;
                    }
                    super.channelRead(ctx, msg);
                }
            });

            DISABLED_PLAYERS.add(player);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void enableMovementPackets(Player player) {
        try {
            PlayerConnection playerConnection = getPlayerConnection(player);
            Field networkManagerField = playerConnection.instance().getClass().getSuperclass().getDeclaredField("c");
            networkManagerField.setAccessible(true);
            Object networkManager = networkManagerField.get(playerConnection.instance());
            networkManagerField.setAccessible(false);

            Field channelField = networkManager.getClass().getDeclaredField("n");
            Channel channel = (Channel) channelField.get(networkManager);

            if (channel.pipeline().get("injected") != null) {
                channel.pipeline().remove("injected");
            }

            DISABLED_PLAYERS.remove(player);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
