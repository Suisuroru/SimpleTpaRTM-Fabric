package fun.bm.simpletpartm.commands.command;

import com.mojang.brigadier.context.CommandContext;
import fun.bm.simpletpartm.commands.AbstractCommand;
import fun.bm.simpletpartm.configs.modules.CoreConfig;
import fun.bm.simpletpartm.configs.modules.TpaConfig;
import fun.bm.simpletpartm.managers.TeleportDataManager;
import fun.bm.simpletpartm.managers.TeleportScheduler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TpaAcceptCommand extends AbstractCommand {

    public TpaAcceptCommand() {
        super("tpaaccept");
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
                                                ServerPlayerEntity from = TeleportDataManager.getLastTpaRequest(into);
                                                if (from == null) {
                                                    context.getSource().sendMessage(Text.literal("§c未找到请求"));
                                                    return 1;
                                                }
                                                return executeAccept(context, from, into);
                                            })
                                    .then(
                                            CommandManager
                                                    .argument("player", EntityArgumentType.player())
                                                    .executes(
                                                            context -> {
                                                                ServerPlayerEntity into = context.getSource().getPlayer();
                                                                if (into == null) {
                                                                    context.getSource().sendMessage(Text.literal("§c请使用玩家执行此命令"));
                                                                    return 1;
                                                                }
                                                                ServerPlayerEntity from = EntityArgumentType.getPlayer(context, "player");
                                                                return executeAccept(context, from, into);
                                                            })
                                    )
                    );
        });
    }

    public static int executeAccept(CommandContext<ServerCommandSource> context, ServerPlayerEntity from, ServerPlayerEntity into) {
        if (from.getUuid() == into.getUuid()) {
            context.getSource().sendMessage(Text.literal("§c无法接受自身请求"));
            return 1;
        }
        int cooldown = CoreConfig.checkCooldown(from);
        if (cooldown != -1) {
            context.getSource().sendMessage(Text.literal("§c请等待" + cooldown + "秒后再试"));
        }
        if (!TpaConfig.checkTpa(from, into)) {
            context.getSource().sendMessage(Text.literal("§c请求已过期"));
            return 1;
        }
        AtomicBoolean scheduled = new AtomicBoolean(false);
        final AtomicInteger taskId = new AtomicInteger();
        Runnable task = () -> {
            if (onlineCheck(from, into)) return;

            if (scheduled.get()) {
                if (!TeleportDataManager.checkPosStore(from)) {
                    TeleportScheduler.cancel(taskId.get());
                    from.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 20, 10));
                    from.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("§c由于移动，传送已取消")));
                    TeleportDataManager.clearPosStore(from);
                    return;
                }
                from.networkHandler.sendPacket(new TitleS2CPacket(Text.empty()));
                from.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 20, 10));
            }
            TeleportDataManager.removeTpaData(from, into);
            TeleportDataManager.reportTeleportedData(from, false);
            from.teleport((ServerWorld) into.getEntityWorld(), into.getX(), into.getY(), into.getZ(), Set.of(), from.getYaw(), from.getPitch());
            from.sendMessage(Text.literal("§a已传送至" + into.getName().getString()));
            TeleportDataManager.clearPosStore(from);
        };
        into.sendMessage(Text.literal("§a已接受传送"));
        from.sendMessage(Text.literal("§a" + into.getName().getString() + "已接受传送"));
        int standStill = TpaConfig.getStandStillTime();
        if (standStill != -1) {
            long time = System.currentTimeMillis();
            TeleportDataManager.sendPosStore(from);
            Runnable periodicTask = () -> {
                if (onlineCheck(from, into)) return;
                long timeLast = (System.currentTimeMillis() - time) / 1000;

                if (!TeleportDataManager.checkPosStore(from)) {
                    TeleportScheduler.cancel(taskId.get());
                    from.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 20, 10));
                    from.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("§c由于移动，传送已取消")));
                    TeleportDataManager.clearPosStore(from);
                    return;
                }

                from.networkHandler.sendPacket(new TitleFadeS2CPacket(0, 20, 0));
                from.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("§a请静止" + timeLast + "秒以传送")));
                scheduled.set(true);
            };
            taskId.set(TeleportScheduler.schedule(task, periodicTask, 1000L, standStill * 1000L, time));
            periodicTask.run();
        } else {
            task.run();
        }
        return 1;
    }

    public static boolean onlineCheck(ServerPlayerEntity from, ServerPlayerEntity into) {
        if (from.isDisconnected() || into.isDisconnected()) {
            if (!into.isDisconnected()) {
                into.sendMessage(Text.literal("§c玩家" + from.getName().getString() + "已离线"));
            }
            if (!from.isDisconnected()) {
                from.sendMessage(Text.literal("§c玩家" + into.getName().getString() + "已离线"));
            }
            return true;
        }
        return false;
    }
}
