package fun.bm.simpletpartm.config.modules;

import fun.bm.simpletpartm.config.flags.ConfigClassInfo;
import fun.bm.simpletpartm.config.flags.ConfigInfo;
import fun.bm.simpletpartm.enums.EnumConfigCategory;

@ConfigClassInfo(category = EnumConfigCategory.TPA)
public class TpaConfig {
    @ConfigInfo(name = "request-expire-time", comments = "Time in seconds before a TPA request expires")
    public static int requestExpireTime = 120;

    @ConfigInfo(name = "enable-stand-still", comments = "Enable stand still time for tpa requests")
    public static boolean enableStandStill = false;

    @ConfigInfo(name = "stand-still-time", comments = "Time in seconds before a player can be teleported when another accept teleport")
    public static int standStillTime = 3;

    public static int getStandStillTime() {
        return enableStandStill ? standStillTime <= 0 ? 0 : standStillTime : 0;
    }
}
