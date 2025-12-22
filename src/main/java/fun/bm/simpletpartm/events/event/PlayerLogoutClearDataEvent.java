package fun.bm.simpletpartm.events.event;

import fun.bm.simpletpartm.managers.TeleportDataManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

public class PlayerLogoutClearDataEvent {
    public static void register() {
        ServerPlayerEvents.LEAVE.register(TeleportDataManager::clearAllData);
    }
}
