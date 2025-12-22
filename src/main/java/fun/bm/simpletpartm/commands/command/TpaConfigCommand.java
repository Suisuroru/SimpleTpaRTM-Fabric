package fun.bm.simpletpartm.commands.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import fun.bm.simpletpartm.commands.AbstractCommand;
import fun.bm.simpletpartm.configs.ConfigManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class TpaConfigCommand extends AbstractCommand {
    public TpaConfigCommand() {
        super("tpaConfig");
    }

    @Override
    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher
                    .register(
                            CommandManager
                                    .literal(this.name)
                                    .requires(CommandManager.requirePermissionLevel(CommandManager.field_31839))
                                    .then(
                                            CommandManager
                                                    .literal("reload")
                                                    .executes(
                                                            context -> {
                                                                ConfigManager.configfile.reload();
                                                                return 1;
                                                            })

                                    )
                                    .then(
                                            CommandManager
                                                    .literal("set")
                                                    .then(
                                                            CommandManager
                                                                    .argument("key", StringArgumentType.string())
                                                                    .suggests(
                                                                            (context, builder) -> {
                                                                                ConfigManager.configfile.completeConfigPath(StringArgumentType.getString(context, "key")).forEach(builder::suggest);
                                                                                return builder.buildFuture();
                                                                            })
                                                                    .executes(
                                                                            context -> {
                                                                                String key = StringArgumentType.getString(context, "key");
                                                                                String value = ConfigManager.configfile.getConfig(key);
                                                                                context.getSource().sendMessage(Text.literal("设置项" + key + "当前值: " + value));
                                                                                return 1;
                                                                            }
                                                                    )
                                                                    .then(
                                                                            CommandManager
                                                                                    .argument("value", StringArgumentType.string())
                                                                                    .suggests(
                                                                                            (context, builder) -> {
                                                                                                String[] suggestions = ConfigManager.configfile.getConfigSuggestions(StringArgumentType.getString(context, "key"));
                                                                                                for (String suggestion : suggestions) {
                                                                                                    builder.suggest(suggestion);
                                                                                                }
                                                                                                return builder.buildFuture();
                                                                                            }
                                                                                    )
                                                                                    .executes(
                                                                                            context -> {
                                                                                                String key = StringArgumentType.getString(context, "key");
                                                                                                String value = StringArgumentType.getString(context, "value");
                                                                                                ConfigManager.configfile.setConfig(key, value);
                                                                                                ConfigManager.configfile.saveConfigs();
                                                                                                ConfigManager.configfile.reload();
                                                                                                context.getSource().sendMessage(Text.literal("设置项" + key + "已设置为: " + value));
                                                                                                return 1;
                                                                                            }
                                                                                    )
                                                                    )
                                                    )
                                    )
                    );
        });
    }
}
