package fun.bm.simpletpartm.commands.command;

import fun.bm.simpletpartm.commands.AbstractCommand;
import fun.bm.simpletpartm.managers.TeleportDataManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TpHereCancelCommand extends AbstractCommand {
    public TpHereCancelCommand() {
        super("tpHereCancel");
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
                                                    context.getSource().sendMessage(Text.literal("§c请使用玩家执行此命令"));
                                                    return 1;
                                                }

                                                Set<ServerPlayerEntity> froms;
                                                if (TeleportDataManager.tpHereToAll.get(into.getUuid())) {
                                                    froms = new HashSet<>();
                                                    Set<Pair<UUID, Long>> tpHereData = TeleportDataManager.tpHereData.get(into.getUuid());
                                                    for (Pair<UUID, Long> pair : tpHereData) {
                                                        ServerPlayerEntity from = into.getEntityWorld().getServer().getPlayerManager().getPlayer(pair.getLeft());
                                                        if (from != null && !from.isDisconnected()) {
                                                            froms.add(from);
                                                        }
                                                    }
                                                } else {
                                                    froms = Set.of(TeleportDataManager.getLastTpHereRequest(into));
                                                }
                                                boolean flag = false;
                                                for (ServerPlayerEntity from : froms) {
                                                    flag = flag || TeleportDataManager.removeTpHereData(from, into);
                                                    from.sendMessage(Text.literal("§b" + into.getStringifiedName() + "§a已取消tpHere请求"));
                                                }
                                                if (flag) {
                                                    into.sendMessage(Text.literal("§a取消了上一个tpHere请求"));
                                                } else {
                                                    into.sendMessage(Text.literal("§c没有tpHere请求可以取消"));
                                                }
                                                return 1;
                                            })
                                    .then(
                                            CommandManager
                                                    .argument("name", EntityArgumentType.player())
                                                    .executes(
                                                            context -> {
                                                                ServerPlayerEntity into = context.getSource().getPlayer();
                                                                if (into == null) {
                                                                    context.getSource().sendMessage(Text.literal("§c请使用玩家执行此命令"));
                                                                    return 1;
                                                                }
                                                                ServerPlayerEntity from = EntityArgumentType.getPlayer(context, "name");
                                                                if (from.getUuid() == into.getUuid()) {
                                                                    context.getSource().sendMessage(Text.literal("§c无法传送至自己"));
                                                                    return 1;
                                                                }
                                                                if (TeleportDataManager.removeTpHereData(from, into)) {
                                                                    from.sendMessage(Text.literal("§b" + into.getStringifiedName() + "§c已取消tpHere请求"));
                                                                    into.sendMessage(Text.literal("§a取消了对§b " + from.getStringifiedName() + " §a的tpHere请求"));
                                                                } else {
                                                                    context.getSource().sendMessage(Text.literal("§c没有tpHere请求可以取消"));
                                                                }
                                                                return 1;
                                                            })
                                    )
                    );
        });
    }
}
