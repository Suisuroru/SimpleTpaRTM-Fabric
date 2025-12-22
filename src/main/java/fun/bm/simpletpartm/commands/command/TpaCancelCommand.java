package fun.bm.simpletpartm.commands.command;

import com.mojang.brigadier.context.CommandContext;
import fun.bm.simpletpartm.commands.AbstractCommand;
import fun.bm.simpletpartm.configs.modules.TpaConfig;
import fun.bm.simpletpartm.managers.TeleportDataManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TpaCancelCommand extends AbstractCommand {
    public TpaCancelCommand() {
        super("tpaCancel");
    }

    @Override
    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher
                    .register(
                            CommandManager
                                    .literal(this.name)
                                    .executes(
                                            context -> {
                                                ServerPlayerEntity from = context.getSource().getPlayer();
                                                if (from == null) {
                                                    context.getSource().sendMessage(Text.literal("§c请使用玩家执行此命令"));
                                                    return 1;
                                                }
                                                ServerPlayerEntity into = from.getEntityWorld().getServer().getPlayerManager().getPlayer(TeleportDataManager.tpaData.get(from.getUuid()).getLeft());
                                                if (into == null) {
                                                    context.getSource().sendMessage(Text.literal("§c未找到传送请求"));
                                                    return 1;
                                                }
                                                return cancelTpa(context, from, into);
                                            })
// You can only have one tpa request, so no need to specify the player
/*                                    .then(
                                            CommandManager
                                                    .argument("name", EntityArgumentType.player())
                                                    .executes(
                                                            context -> {
                                                                ServerPlayerEntity from = context.getSource().getPlayer();
                                                                if (from == null) {
                                                                    context.getSource().sendMessage(Text.literal("§c请使用玩家执行此命令"));
                                                                    return 1;
                                                                }
                                                                ServerPlayerEntity into = EntityArgumentType.getPlayer(context, "name");
                                                                return cancelTpa(context, from, into);
                                                            })
                                    )*/
                    );
        });
    }

    private static int cancelTpa(CommandContext<ServerCommandSource> context, ServerPlayerEntity from, ServerPlayerEntity into) {
        if (!TpaConfig.checkTpa(from, into)) {
            context.getSource().sendMessage(Text.literal("§c请求已过期，无需取消"));
            return 1;
        }
        TeleportDataManager.removeTpaData(from, into);
        context.getSource().sendMessage(Text.literal("§a已取消传送到" + into.getStringifiedName() + "请求"));
        return 1;
    }
}
