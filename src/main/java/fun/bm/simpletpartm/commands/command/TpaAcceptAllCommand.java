package fun.bm.simpletpartm.commands.command;

import fun.bm.simpletpartm.commands.AbstractCommand;
import fun.bm.simpletpartm.managers.TeleportDataManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Set;

public class TpaAcceptAllCommand extends AbstractCommand {
    public TpaAcceptAllCommand() {
        super("tpaacceptall");
    }

    @Override
    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher
                    .register(
                            CommandManager
                                    .literal(this.name)
                                    .executes(context -> {
                                        ServerPlayerEntity into = context.getSource().getPlayer();
                                        if (into == null) {
                                            context.getSource().sendMessage(Text.literal("§c请使用玩家执行此命令"));
                                            return 1;
                                        }
                                        Set<ServerPlayerEntity> froms = TeleportDataManager.getTpaRequests(into);
                                        for (ServerPlayerEntity from : froms) {
                                            TpaAcceptCommand.executeAccept(context, from, into);
                                        }
                                        return 1;
                                    })
                    );
        });
    }
}
