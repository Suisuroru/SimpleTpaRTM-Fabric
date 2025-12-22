package fun.bm.simpletpartm.events.event;

import fun.bm.simpletpartm.managers.TeleportDataManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerDeathPostPositionEvent {
    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity player)
                TeleportDataManager.reportTeleportedData(player, true);
        });
    }
}
