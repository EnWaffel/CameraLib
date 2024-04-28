package de.enwaffel.mc.camlib.impl.v1_20_R3;

import de.enwaffel.mc.camlib.nms.NMS;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

public class NMSImpl implements NMS {

    private final Class<?> positionPacketClass;
    private final Class<?> packetClass;
    private final HashMap<Player, PlayerConnection> CONNECTION_CACHE = new HashMap<>();

    public NMSImpl() {
        String nmsPackage = "net.minecraft";

        try {
            positionPacketClass = Class.forName(nmsPackage + ".network.protocol.game.PacketPlayOutPosition");
            packetClass = Class.forName(nmsPackage + ".network.protocol.Packet");
        } catch (ClassNotFoundException e) {
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
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
