package fun.bm.simpletpartm.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class TickTpaScheduledEvent {
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {

        });
    }
}
