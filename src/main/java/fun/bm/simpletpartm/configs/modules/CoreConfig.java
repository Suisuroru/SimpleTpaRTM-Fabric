package fun.bm.simpletpartm.config.modules;

import fun.bm.simpletpartm.config.flags.ConfigClassInfo;
import fun.bm.simpletpartm.config.flags.ConfigInfo;
import fun.bm.simpletpartm.enums.EnumConfigCategory;
import fun.bm.simpletpartm.manager.TeleportDataManager;
import net.minecraft.server.network.ServerPlayerEntity;

@ConfigClassInfo(category = EnumConfigCategory.CORE)
public class CoreConfig {
    @ConfigInfo(name = "enable-cooldown", comments = "Enable cooldown for tpa requests & back command")
    public static boolean enableCooldown = false;

    @ConfigInfo(name = "cooldown-time", comments = "Cooldown time in seconds")
    public static int cooldownTime = 5;

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
