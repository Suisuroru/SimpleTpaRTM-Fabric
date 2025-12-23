package fun.bm.simpletpartm.commands.command;

import com.mojang.brigadier.context.CommandContext;
import fun.bm.simpletpartm.commands.AbstractCommand;
import fun.bm.simpletpartm.managers.TeleportDataManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TpaDenyCommand extends AbstractCommand {
    public TpaDenyCommand() {
        super("tpadeny");
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
                                                ServerPlayerEntity into = context.getSource().getPlayer();
                                                if (into == null) {
                                                    context.getSource().sendMessage(Text.literal("请使用玩家执行此命令"));
                                                    return 1;
                                                }
                                                ServerPlayerEntity from = TeleportDataManager.getLastTpaRequest(into);
                                                if (from == null) {
                                                    context.getSource().sendMessage(Text.literal("未找到请求或请求的玩家不在线"));
                                                    return 1;
                                                }
                                                return executeDeny(context, from, into);
                                            })
                                    .then(
                                            CommandManager.argument("player", EntityArgumentType.player())
                                                    .executes(
                                                            context -> {
                                                                ServerPlayerEntity into = context.getSource().getPlayer();
                                                                if (into == null) {
                                                                    context.getSource().sendMessage(Text.literal("请使用玩家执行此命令"));
                                                                    return 1;
                                                                }
                                                                ServerPlayerEntity from = EntityArgumentType.getPlayer(context, "player");
                                                                return executeDeny(context, from, into);
                                                            })
                                    )
                    );
        });
    }

    public static int executeDeny(CommandContext<ServerCommandSource> context, ServerPlayerEntity from, ServerPlayerEntity into) {
        if (from.getUuid() == into.getUuid()) {
            context.getSource().sendMessage(Text.literal("§c无法拒绝自身请求"));
            return 1;
        }

        TeleportDataManager.removeTpaData(from, into);
        context.getSource().sendMessage(Text.literal("§c已拒绝玩家 §b" + from.getName().getString() + " §c的传送请求"));
        from.sendMessage(Text.literal("§c玩家 §b" + into.getName().getString() + " §c拒绝了你的传送请求"));
        return 1;
    }
}
