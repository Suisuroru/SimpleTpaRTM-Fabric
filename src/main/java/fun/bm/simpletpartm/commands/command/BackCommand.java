package fun.bm.simpletpartm.commands.command;

import fun.bm.simpletpartm.commands.AbstractCommand;
import fun.bm.simpletpartm.configs.modules.BackConfig;
import fun.bm.simpletpartm.configs.modules.CoreConfig;
import fun.bm.simpletpartm.managers.TeleportDataManager;
import fun.bm.simpletpartm.managers.TeleportScheduler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BackCommand extends AbstractCommand {
    public BackCommand() {
        super("back");
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
                                                ServerPlayerEntity player = context.getSource().getPlayer();
                                                if (player == null) {
                                                    context.getSource().sendMessage(Text.literal("§c请使用玩家执行此命令"));
                                                    return 1;
                                                }
                                                int cooldown = CoreConfig.checkCooldown(player);
                                                if (cooldown != -1) {
                                                    context.getSource().sendMessage(Text.literal("§c请等待" + cooldown + "秒后再试"));
                                                }

                                                Pair<Pair<RegistryKey<World>, Vec3d>, Long> value = TeleportDataManager.backData.get(player.getUuid());
                                                if (value == null) {
                                                    context.getSource().sendMessage(Text.literal("§c没有可返回的位置"));
                                                    return 1;
                                                }
                                                if (System.currentTimeMillis() - value.getRight() > BackConfig.backExpireTime * 1000L) {
                                                    TeleportDataManager.backData.remove(player.getUuid());
                                                    context.getSource().sendMessage(Text.literal("§c返回位置已过期"));
                                                    return 1;
                                                }

                                                AtomicBoolean scheduled = new AtomicBoolean(false);
                                                final AtomicInteger taskId = new AtomicInteger();
                                                Runnable task = () -> {
                                                    if (scheduled.get()) {
                                                        if (!TeleportDataManager.checkPosStore(player)) {
                                                            TeleportScheduler.cancel(taskId.get());
                                                            player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 20, 10));
                                                            player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("§c由于移动，传送已取消")));
                                                            TeleportDataManager.clearPosStore(player);
                                                            return;
                                                        }
                                                        player.networkHandler.sendPacket(new TitleS2CPacket(Text.empty()));
                                                        player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 20, 10));
                                                    }

                                                    TeleportDataManager.reportTeleportedData(player, false);
                                                    ServerWorld world = player.getEntityWorld().getServer().getWorld(value.getLeft().getLeft());
                                                    Vec3d pos = value.getLeft().getRight();
                                                    player.teleport(world, pos.getX(), pos.getY(), pos.getZ(), Set.of(), player.getYaw(), player.getPitch());
                                                    context.getSource().sendMessage(Text.literal("§a已返回至上一个地点[" + value.getLeft().getLeft().getValue().toString() + "]" + "(" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")"));
                                                    TeleportDataManager.clearPosStore(player);
                                                };
                                                int standStill = BackConfig.getStandStillTime();
                                                if (standStill != -1) {
                                                    long time = System.currentTimeMillis();
                                                    TeleportDataManager.sendPosStore(player);
                                                    Runnable periodicTask = () -> {
                                                        long timeLast = (System.currentTimeMillis() - time) / 1000;

                                                        if (!TeleportDataManager.checkPosStore(player)) {
                                                            TeleportScheduler.cancel(taskId.get());
                                                            player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 20, 10));
                                                            player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("§c由于移动，传送已取消")));
                                                            TeleportDataManager.clearPosStore(player);
                                                            return;
                                                        }

                                                        player.networkHandler.sendPacket(new TitleFadeS2CPacket(0, 20, 0));
                                                        player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("§a请静止" + timeLast + "秒以传送")));
                                                        scheduled.set(true);
                                                    };
                                                    taskId.set(TeleportScheduler.schedule(task, periodicTask, 1000L, standStill * 1000L, time));
                                                    periodicTask.run();
                                                } else {
                                                    task.run();
                                                }
                                                return 1;
                                            })
                    );
        });
    }
}
