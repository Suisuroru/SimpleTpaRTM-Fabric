package fun.bm.simpletpartm.config;


import java.io.IOException;

public class ConfigManager {
    public static final ConfigsInstance configfile = ConfigsInstance.of("tpa", "fun.bm.simpletpartm.config.modules");

    public static void initConfigs() throws IOException {
        configfile.preLoadConfig();
        configfile.finalizeLoadConfig();
    }
}