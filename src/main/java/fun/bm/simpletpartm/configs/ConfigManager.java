package fun.bm.simpletpartm.configs;


import java.io.File;
import java.io.IOException;

public class ConfigManager {
    public static ConfigsInstance configfile;

    public static void initConfigs() throws IOException {
        configfile = ConfigsInstance.of(new File("config"), "tpa", "fun.bm.simpletpartm.configs.modules");
        configfile.preLoadConfig();
        configfile.finalizeLoadConfig();
    }
}