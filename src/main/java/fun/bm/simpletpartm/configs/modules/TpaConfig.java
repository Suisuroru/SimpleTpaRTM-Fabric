package fun.bm.simpletpartm.configs.modules;

import fun.bm.simpletpartm.configs.IConfigModule;
import fun.bm.simpletpartm.configs.flags.ConfigClassInfo;
import fun.bm.simpletpartm.configs.flags.ConfigInfo;
import fun.bm.simpletpartm.enums.EnumConfigCategory;
import fun.bm.simpletpartm.managers.TeleportDataManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;

import java.util.UUID;

@ConfigClassInfo(category = EnumConfigCategory.TPA)
public class TpaConfig implements IConfigModule {
    @ConfigInfo(name = "request-expire-time", comments = "Time in seconds before a TPA request expires")
    public static int requestExpireTime = 120;

    @ConfigInfo(name = "enable-stand-still", comments = "Enable stand still time for tpa requests")
    public static boolean enableStandStill = false;

    @ConfigInfo(name = "stand-still-time", comments = "Time in seconds before a player can be teleported when another accept teleport")
    public static int standStillTime = 3;

    public static int getStandStillTime() {
        return enableStandStill ? standStillTime <= 0 ? -1 : standStillTime : -1;
    }

    public static boolean checkTpa(ServerPlayerEntity from, ServerPlayerEntity into) {
        long now = System.currentTimeMillis();
        Pair<UUID, Long> value = TeleportDataManager.tpaData.get(from.getUuid());
        if (value == null || value.getLeft() != into.getUuid()) return false;
        return now - value.getRight() <= requestExpireTime * 1000L;
    }
}
