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

public class TpHereCommand extends AbstractCommand {
    public TpHereCommand() {
        super("tpHere");
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
                                                TeleportDataManager.tpHereToAll.put(into.getUuid(), true);
                                                into.getEntityWorld().getServer().getPlayerManager().getPlayerList().forEach(from -> {
                                                    if (from.getUuid() != into.getUuid())
                                                        executeTpHere(context, from, into);
                                                });
                                                return 1;
                                            })
                                    .then(
                                            CommandManager
                                                    .argument("player", EntityArgumentType.player())
                                                    .executes(
                                                            context -> {
                                                                ServerPlayerEntity into = context.getSource().getPlayer();
                                                                if (into == null) {
                                                                    context.getSource().sendMessage(Text.literal("请使用玩家执行此命令"));
                                                                    return 1;
                                                                }
                                                                ServerPlayerEntity from = EntityArgumentType.getPlayer(context, "player");
                                                                TeleportDataManager.tpHereToAll.put(into.getUuid(), false);
                                                                return executeTpHere(context, from, into);
                                                            }
                                                    )
                                    )
                    );
        });
    }

    private static int executeTpHere(CommandContext<ServerCommandSource> context, ServerPlayerEntity from, ServerPlayerEntity into) {
        if (from.getUuid() == into.getUuid()) {
            context.getSource().sendMessage(Text.literal("§c无法传送自身"));
            return 1;
        }

        TeleportDataManager.sendTpHere(from, into);

        context.getSource().sendMessage(Text.literal("§a已向§b " + from.getStringifiedName() + " §a发送传送到你的位置的请求"));
        context.getSource().sendMessage(Text.literal("§a请输入 /tpHereCancel 以取消传送"));
        from.sendMessage(Text.literal("§a玩家§b " + into.getStringifiedName() + " §a请求传送至TA的位置"));
        from.sendMessage(Text.literal("§a请输入 /tpHereAccept 以接受传送"));
        return 1;
    }
}
