package fun.bm.simpletpartm.managers;

import fun.bm.simpletpartm.configs.modules.CoreConfig;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class TeleportDataManager {
    public static final Map<UUID, Pair<UUID, Long>> tpaData = new ConcurrentHashMap<>();
    public static final Map<UUID, Set<Pair<UUID, Long>>> tpHereData = new ConcurrentHashMap<>();
    public static final Map<UUID, Boolean> tpHereToAll = new ConcurrentHashMap<>();
    public static final Map<UUID, Pair<Pair<RegistryKey<World>, Vec3d>, Long>> backData = new ConcurrentHashMap<>();
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

    public static void sendTpHere(ServerPlayerEntity from, ServerPlayerEntity into) {
        long now = System.currentTimeMillis();
        Set<Pair<UUID, Long>> value = tpHereData.get(into.getUuid());
        if (value != null) {
            for (Pair<UUID, Long> pair : value) {
                if (pair.getLeft() == from.getUuid()) {
                    pair.setRight(now);
                    return;
                }
            }
            value.add(new Pair<>(from.getUuid(), now));
            return;
        }
        value = new HashSet<>();
        value.add(new Pair<>(from.getUuid(), now));
        tpHereData.put(into.getUuid(), value);
    }

    public static void reportTeleportedData(ServerPlayerEntity from, boolean natural) {
        long time = System.currentTimeMillis();
        if (!natural) lastTeleportData.put(from.getUuid(), time);
        Pair<Pair<RegistryKey<World>, Vec3d>, Long> value = new Pair<>(new Pair<>(from.getEntityWorld().getRegistryKey(), from.getEntityPos()), time);
        backData.put(from.getUuid(), value);
    }

    public static void removeTpaData(ServerPlayerEntity from, ServerPlayerEntity into) {
        Pair<UUID, Long> value = tpaData.get(from.getUuid());
        if (value != null && value.getLeft() == into.getUuid()) tpaData.remove(from.getUuid());
    }

    public static boolean removeTpHereData(ServerPlayerEntity from, ServerPlayerEntity into) {
        Set<Pair<UUID, Long>> value = tpHereData.get(into.getUuid());
        if (value != null) {
            for (Pair<UUID, Long> pair : value) {
                if (pair.getLeft() == from.getUuid()) {
                    value.remove(pair);
                    return true;
                }
            }
        }
        return false;
    }

    public static ServerPlayerEntity getLastTpHereRequest(ServerPlayerEntity from) {
        AtomicLong lastTime = new AtomicLong(0);
        AtomicReference<ServerPlayerEntity> into = new AtomicReference<>(null);
        for (Map.Entry<UUID, Set<Pair<UUID, Long>>> entry : tpHereData.entrySet()) {
            if (entry.getKey() == from.getUuid()) continue;
            ServerPlayerEntity player = from.getEntityWorld().getServer().getPlayerManager().getPlayer(entry.getKey());
            if (player != null && !player.isDisconnected()) {
                for (Pair<UUID, Long> pair : entry.getValue()) {
                    if (pair.getLeft() == from.getUuid()) {
                        if (lastTime.get() < pair.getRight()) {
                            lastTime.set(pair.getRight());
                            into.set(player);
                        }
                        break;
                    }
                }
            }
        }
        return into.get();
    }

    public static ServerPlayerEntity getLastTpaRequest(ServerPlayerEntity into) {
        AtomicLong lastTime = new AtomicLong(0);
        AtomicReference<ServerPlayerEntity> from = new AtomicReference<>(null);
        tpaData.forEach((key, value) -> {
            if (value.getLeft() == into.getUuid()) {
                if (lastTime.get() < value.getRight()) {
                    ServerPlayerEntity player = into.getEntityWorld().getServer().getPlayerManager().getPlayer(key);
                    if (player != null && !player.isDisconnected()) {
                        lastTime.set(value.getRight());
                        from.set(player);
                    }
                }
            }
        });
        return from.get();
    }

    public static Set<ServerPlayerEntity> getTpHereRequests(ServerPlayerEntity from) {
        Set<ServerPlayerEntity> intos = new HashSet<>();
        for (Map.Entry<UUID, Set<Pair<UUID, Long>>> entry : tpHereData.entrySet()) {
            if (entry.getKey() == from.getUuid()) continue;
            ServerPlayerEntity player = from.getEntityWorld().getServer().getPlayerManager().getPlayer(entry.getKey());
            if (player != null && !player.isDisconnected()) {
                for (Pair<UUID, Long> pair : entry.getValue()) {
                    if (pair.getLeft() == from.getUuid()) {
                        intos.add(player);
                        break;
                    }
                }
            }
        }
        return intos;
    }

    public static Set<ServerPlayerEntity> getTpaRequests(ServerPlayerEntity into) {
        Set<ServerPlayerEntity> froms = new HashSet<>();
        tpaData.forEach((key, value) -> {
            if (value.getLeft() == into.getUuid()) {
                ServerPlayerEntity player = into.getEntityWorld().getServer().getPlayerManager().getPlayer(key);
                if (player != null && !player.isDisconnected()) {
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

    public static void clearPosStore(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        posStore.remove(uuid);
    }

    public static void cleanUpCache(MinecraftServer server) {
        if (server.getTicks() % CoreConfig.cleanupIntervalTicks == 0) {
            cleanCacheForMap(server, tpaData);
            cleanCacheForMap(server, tpHereData);
            cleanCacheForMap(server, tpHereToAll);
            cleanCacheForMap(server, backData);
            cleanCacheForMap(server, lastTeleportData);
            cleanCacheForMap(server, posStore);
        }
    }

    public static void cleanCacheForMap(MinecraftServer server, Map<UUID, ?> map) {
        for (UUID uuid : map.keySet()) {
            if (server.getPlayerManager().getPlayer(uuid) == null) {
                map.remove(uuid);
            }
        }
    }

    public static void clearAllData(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        tpaData.remove(uuid);
        tpHereData.remove(uuid);
        tpHereToAll.remove(uuid);
        backData.remove(uuid);
        lastTeleportData.remove(uuid);
        posStore.remove(uuid);
    }
}
