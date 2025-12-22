package fun.bm.simpletpartm.events;

import fun.bm.simpletpartm.events.event.PlayerDeathPostPositionEvent;
import fun.bm.simpletpartm.events.event.PlayerLogoutClearDataEvent;
import fun.bm.simpletpartm.events.event.TickTpaScheduledEvent;

public class Events {
    public static void register() {
        TickTpaScheduledEvent.register();
        PlayerDeathPostPositionEvent.register();
        PlayerLogoutClearDataEvent.register();
    }
}
