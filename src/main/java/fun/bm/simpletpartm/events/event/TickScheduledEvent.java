package fun.bm.simpletpartm.events.event;

import fun.bm.simpletpartm.managers.TeleportDataManager;
import fun.bm.simpletpartm.managers.TeleportScheduler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class TickScheduledEvent {
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            TeleportScheduler.tick();
            TeleportDataManager.cleanUpCache(server);
        });
    }
}
