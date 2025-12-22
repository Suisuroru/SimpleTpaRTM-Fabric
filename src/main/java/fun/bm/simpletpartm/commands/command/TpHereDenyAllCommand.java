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

public class TpHereDenyCommand extends AbstractCommand {
    public TpHereDenyCommand() {
        super("tpHereDeny");
    }

    @Override
    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher
                    .register(
                            CommandManager
                                    .literal(this.name)
                                    .executes((context) -> {
                                        ServerPlayerEntity from = context.getSource().getPlayer();
                                        if (from == null) {
                                            context.getSource().sendMessage(Text.literal("§c请使用玩家执行此命令"));
                                            return 1;
                                        }
                                        ServerPlayerEntity into = TeleportDataManager.getLastTpHereRequest( from);
                                        if (into == null){
                                            context.getSource().sendMessage(Text.literal("§c没有发现tpHere请求"));
                                            return 1;
                                        }
                                        return executeTpHereDeny(context, from, into);
                                    })
                                    .then(
                                            CommandManager
                                                    .argument("name", EntityArgumentType.player())
                                                    .executes((context) -> {
                                                        ServerPlayerEntity from = context.getSource().getPlayer();
                                                        if (from == null) {
                                                            context.getSource().sendMessage(Text.literal("§c请使用玩家执行此命令"));
                                                            return 1;
                                                        }
                                                        ServerPlayerEntity into = EntityArgumentType.getPlayer(context, "name");
                                                        return executeTpHereDeny(context, from, into);
                                                    })
                                    )
                    );
        });
    }

    public int executeTpHereDeny(CommandContext<ServerCommandSource> context, ServerPlayerEntity from, ServerPlayerEntity into){
        if(from.getUuid() == into.getUuid()) {
            context.getSource().sendMessage(Text.literal("§c无法拒绝自身请求"));
            return 1;
        }
        TeleportDataManager.removeTpHereData(from, into);
        context.getSource().sendMessage(Text.literal("§c已拒绝玩家 §b" + into.getName().getString() + " §c的传送请求"));
        into.sendMessage(Text.literal("§c玩家 §b" + from.getName().getString() +" §c拒绝了你的传送请求"));
        return 1;
    }
}
