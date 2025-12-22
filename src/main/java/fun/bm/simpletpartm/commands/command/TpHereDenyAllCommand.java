package fun.bm.simpletpartm.commands.command;

import fun.bm.simpletpartm.commands.AbstractCommand;
import fun.bm.simpletpartm.managers.TeleportDataManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Set;

public class TpHereDenyAllCommand extends AbstractCommand {
    public TpHereDenyAllCommand() {
        super("tpHereDenyAll");
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
                                        Set<ServerPlayerEntity> intos = TeleportDataManager.getTpHereRequests(from);
                                        if (intos.isEmpty()) {
                                            context.getSource().sendMessage(Text.literal("§c没有发现tpHere请求"));
                                            return 1;
                                        }
                                        for (ServerPlayerEntity into : intos) {
                                            TpHereDenyCommand.executeTpHereDeny(context, from, into);
                                        }
                                        return 1;
                                    })
                    );
        });
    }
}
