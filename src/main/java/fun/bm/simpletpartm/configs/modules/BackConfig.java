package fun.bm.simpletpartm.configs.modules;

import fun.bm.simpletpartm.configs.IConfigModule;
import fun.bm.simpletpartm.configs.flags.ConfigClassInfo;
import fun.bm.simpletpartm.configs.flags.ConfigInfo;
import fun.bm.simpletpartm.enums.EnumConfigCategory;

@ConfigClassInfo(category = EnumConfigCategory.BACK)
public class BackConfig implements IConfigModule {
    @ConfigInfo(name = "back-expire-time", comments = "Time in seconds before a back position expires")
    public static int backExpireTime = 120;

    @ConfigInfo(name = "enable-stand-still", comments = "Enable stand still time for tpa requests")
    public static boolean enableStandStill = false;

    @ConfigInfo(name = "stand-still-time", comments = "Time in seconds before a player can be teleported when another accept teleport")
    public static int standStillTime = 3;

    public static int getStandStillTime() {
        return enableStandStill ? standStillTime <= 0 ? -1 : standStillTime : -1;
    }
}
