package fun.bm.simpletpartm.managers;

import fun.bm.simpletpartm.configs.modules.TpaConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class TeleportDataManager {
    public static final Map<UUID, Pair<UUID, Long>> tpaData = new ConcurrentHashMap<>();
    public static final Map<UUID, Pair<Vec3d, Long>> backData = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> lastTeleportData = new ConcurrentHashMap<>();
    public static final Map<UUID, Pair<Vec3i, Long>> posStore = new ConcurrentHashMap<>();

    public static boolean sendTpa(ServerPlayerEntity from, ServerPlayerEntity into) {
        long now = System.currentTimeMillis();
        Pair<UUID, Long> value = tpaData.get(from.getUuid());
        if (value != null && value.getLeft() != into.getUuid()) {
            return false;
        }
        tpaData.put(from.getUuid(), new Pair<>(into.getUuid(), now));
        return true;
    }

    public static void reportTeleportedData(ServerPlayerEntity from) {
        long time = System.currentTimeMillis();
        lastTeleportData.put(from.getUuid(), time);
        Pair<Vec3d, Long> value = new Pair<>(from.getEntityPos(), time);
        backData.put(from.getUuid(), value);
    }

    public static void removeData(ServerPlayerEntity from, ServerPlayerEntity into) {
        Pair<UUID, Long> value = tpaData.get(from.getUuid());
        if (value != null && value.getLeft() == into.getUuid()) tpaData.remove(from.getUuid());
    }

    public static boolean checkTpa(ServerPlayerEntity from, ServerPlayerEntity into) {
        AtomicReference<Pair<UUID, UUID>> key = new AtomicReference<>();
        long now = System.currentTimeMillis();
        Pair<UUID, Long> value = tpaData.get(from.getUuid());
        if (value == null || value.getLeft() != into.getUuid()) return false;
        return now - value.getRight() <= TpaConfig.requestExpireTime * 1000L;
    }

    public static ServerPlayerEntity getLastTpaRequest(ServerPlayerEntity into) {
        AtomicLong lastTime = new AtomicLong(0);
        AtomicReference<ServerPlayerEntity> from = new AtomicReference<>(null);
        tpaData.forEach((key, value) -> {
            if (value.getLeft() == into.getUuid()) {
                if (lastTime.get() < value.getRight()) {
                    ServerPlayerEntity player = into.getEntityWorld().getServer().getPlayerManager().getPlayer(key);
                    if (!(player == null) && !player.isDisconnected()) {
                        lastTime.set(value.getRight());
                        from.set(player);
                    }
                }
            }
        });
        return from.get();
    }

    public static Set<ServerPlayerEntity> getTpaRequest(ServerPlayerEntity into) {
        Set<ServerPlayerEntity> froms = new HashSet<>();
        tpaData.forEach((key, value) -> {
            if (value.getLeft() == into.getUuid()) {
                ServerPlayerEntity player = into.getEntityWorld().getServer().getPlayerManager().getPlayer(key);
                if (!(player == null) && !player.isDisconnected()) {
                    froms.add(player);
                }
            }
        });
        return froms;
    }

    public static void sendPosStore(ServerPlayerEntity player) {
        long now = System.currentTimeMillis();
        UUID uuid = player.getUuid();
        Pair<Vec3i, Long> data = new Pair<>(player.getBlockPos(), now);
        posStore.put(uuid, data);
    }

    public static boolean checkPosStore(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        Vec3i posStored = posStore.get(uuid).getLeft();
        Vec3i posNow = player.getBlockPos();
        return posNow.equals(posStored);
    }
}
