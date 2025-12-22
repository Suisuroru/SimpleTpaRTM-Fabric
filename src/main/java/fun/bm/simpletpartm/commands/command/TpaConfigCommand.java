package fun.bm.simpletpartm.commands.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import fun.bm.simpletpartm.commands.AbstractCommand;
import fun.bm.simpletpartm.configs.ConfigManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.Objects;

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
                                                                                String path;
                                                                                try {
                                                                                    path = StringArgumentType.getString(context, "key");
                                                                                } catch (Exception e) {
                                                                                    path = "";
                                                                                }
                                                                                int dotIndex = path.lastIndexOf(".");
                                                                                builder = builder.createOffset(builder.getInput().lastIndexOf(' ') + dotIndex + 2);
                                                                                for (String s : ConfigManager.configfile.completeConfigPath(path)) {
                                                                                    builder.suggest(s.substring(path.lastIndexOf('.') + 1));
                                                                                }
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
                                                                                                String path;
                                                                                                try {
                                                                                                    path = StringArgumentType.getString(context, "key");
                                                                                                } catch (Exception e) {
                                                                                                    path = "";
                                                                                                }
                                                                                                if (!ConfigManager.configfile.getAllConfigPaths("").contains(path)) {
                                                                                                    return builder
                                                                                                            .suggest("<ERROR CONFIG>", Text.literal("This config path does not exist."))
                                                                                                            .buildFuture();
                                                                                                }
                                                                                                Object value = ConfigManager.configfile.getConfigOrigin(path);
                                                                                                String[] suggestions = ConfigManager.configfile.getConfigSuggestions(path);
                                                                                                builder.suggest(value.toString(), Text.literal("Default value")
                                                                                                        .styled(style -> style.withColor(TextColor.fromFormatting(Formatting.GRAY))));
                                                                                                if (suggestions == null) {
                                                                                                    if (value instanceof Boolean) {
                                                                                                        builder.suggest(String.valueOf(!(Boolean) value));
                                                                                                    }
                                                                                                } else {
                                                                                                    for (String s : suggestions) {
                                                                                                        if (!Objects.equals(s, value.toString())) {
                                                                                                            builder.suggest(s);
                                                                                                        }
                                                                                                    }
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
