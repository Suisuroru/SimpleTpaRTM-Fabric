package fun.bm.simpletpartm.configs.modules;

import fun.bm.simpletpartm.configs.IConfigModule;
import fun.bm.simpletpartm.configs.flags.ConfigClassInfo;
import fun.bm.simpletpartm.configs.flags.ConfigInfo;
import fun.bm.simpletpartm.enums.EnumConfigCategory;
import fun.bm.simpletpartm.managers.TeleportDataManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;

import java.util.Set;
import java.util.UUID;

@ConfigClassInfo(category = EnumConfigCategory.TPHERE)
public class TpHereConfig implements IConfigModule {
    @ConfigInfo(name = "request-expire-time", comments = "Time in seconds before a TPHERE request expires")
    public static int requestExpireTime = 120;

    @ConfigInfo(name = "enable-stand-still", comments = "Enable stand still time for tphere requests")
    public static boolean enableStandStill = false;

    @ConfigInfo(name = "stand-still-time", comments = "Time in seconds before a player can be teleported when another accept teleport")
    public static int standStillTime = 3;

    public static int getStandStillTime() {
        return enableStandStill ? standStillTime <= 0 ? -1 : standStillTime : -1;
    }

    public static boolean checkTpHere(ServerPlayerEntity from, ServerPlayerEntity into) {
        long now = System.currentTimeMillis();
        Set<Pair<UUID, Long>> value = TeleportDataManager.tpHereData.get(into.getUuid());
        if (value == null) return false;
        for (Pair<UUID, Long> pair : value) {
            if (pair.getLeft() == from.getUuid()) {
                return now - pair.getRight() <= requestExpireTime * 1000L;
            }
        }
        return false;
    }
}
