package fun.bm.simpletpartm.configs.modules;

import fun.bm.simpletpartm.configs.IConfigModule;
import fun.bm.simpletpartm.configs.flags.ConfigClassInfo;
import fun.bm.simpletpartm.configs.flags.ConfigInfo;
import fun.bm.simpletpartm.enums.EnumConfigCategory;
import fun.bm.simpletpartm.managers.TeleportDataManager;
import net.minecraft.server.network.ServerPlayerEntity;

@ConfigClassInfo(category = EnumConfigCategory.CORE)
public class CoreConfig implements IConfigModule {
    @ConfigInfo(name = "enable-cooldown", comments = "Enable cooldown for tpa requests & back command")
    public static boolean enableCooldown = false;

    @ConfigInfo(name = "cooldown-time", comments = "Cooldown time in seconds (except tpHere)")
    public static int cooldownTime = 5;

    @ConfigInfo(name = "cleanup-interval-ticks", comments = "How often to clean up teleport data (in ticks, number of ticks between cleanups)")
    public static int cleanupIntervalTicks = 20 * 60 * 15; // 15 minutes

    public static int getCooldownTime() {
        return enableCooldown ? cooldownTime : 0;
    }

    public static int checkCooldown(ServerPlayerEntity player) {
        int cooldownTime = getCooldownTime();
        if (cooldownTime <= 0) return -1;
        Long lastTeleportTime = TeleportDataManager.lastTeleportData.get(player.getUuid());
        if (lastTeleportTime == null) return -1;
        Long now = System.currentTimeMillis();
        return Math.toIntExact(now - lastTeleportTime > cooldownTime * 1000L ? -1 : cooldownTime - (now - lastTeleportTime) / 1000);
    }
}
