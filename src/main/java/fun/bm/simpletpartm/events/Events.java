package fun.bm.simpletpartm.events;

import fun.bm.simpletpartm.events.event.PlayerDeathPostPositionEvent;
import fun.bm.simpletpartm.events.event.PlayerLogoutClearDataEvent;
import fun.bm.simpletpartm.events.event.TickScheduledEvent;

public class Events {
    public static void register() {
        TickScheduledEvent.register();
        PlayerDeathPostPositionEvent.register();
        PlayerLogoutClearDataEvent.register();
    }
}
