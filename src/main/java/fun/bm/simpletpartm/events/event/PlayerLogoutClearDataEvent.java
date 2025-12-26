package fun.bm.simpletpartm.events.event;

import fun.bm.simpletpartm.managers.TeleportDataManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class PlayerLogoutClearDataEvent {
    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            TeleportDataManager.clearAllData(handler.getPlayer());
        });
    }
}
