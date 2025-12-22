package fun.bm.simpletpartm;

import fun.bm.simpletpartm.commands.Commands;
import fun.bm.simpletpartm.configs.ConfigManager;
import fun.bm.simpletpartm.events.Events;
import net.fabricmc.api.ModInitializer;

import java.io.IOException;

public class Simpletpartm implements ModInitializer {

    @Override
    public void onInitialize() {
        try {
            ConfigManager.initConfigs();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Commands.initCommandSet();
        Commands.register();
        Events.register();
    }
}
