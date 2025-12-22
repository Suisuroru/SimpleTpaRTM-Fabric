package fun.bm.simpletpartm.commands.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fun.bm.simpletpartm.commands.AbstractCommand;
import fun.bm.simpletpartm.configs.modules.CoreConfig;
import fun.bm.simpletpartm.managers.TeleportDataManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TpaCommand extends AbstractCommand {
    public TpaCommand() {
        super("tpa");
    }

    @Override
    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher
                    .register(
                            CommandManager
                                    .literal(this.name)
                                    .then(
                                            CommandManager
                                                    .argument("name", EntityArgumentType.player())
                                                    .executes(this::executeTpa)
                                    )
                    );
        });
    }

    public int executeTpa(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity from = context.getSource().getPlayer();
        if (from == null) {
            context.getSource().sendMessage(Text.literal("§c请使用玩家执行此命令"));
            return 1;
        }

        ServerPlayerEntity into = EntityArgumentType.getPlayer(context, "name");

        if (from.getUuid() == into.getUuid()) {
            context.getSource().sendMessage(Text.literal("§c无法传送至自己"));
            return 1;
        }

        int cooldown = CoreConfig.checkCooldown(from);
        if (cooldown != -1) {
            context.getSource().sendMessage(Text.literal("§c请等待" + cooldown + "秒后再试"));
        }

        boolean success = TeleportDataManager.sendTpa(from, into);
        if (!success) {
            ServerPlayerEntity target = from.getEntityWorld().getServer().getPlayerManager().getPlayer(TeleportDataManager.tpaData.get(from.getUuid()).getLeft());
            context.getSource().sendMessage(Text.literal("§c你已有一个未处理的传送请求，请等待对方处理，当前的传送目标：" + (target == null ? "null" : target.getStringifiedName())));
            context.getSource().sendMessage(Text.literal("§c请输入 /tpaCancel 以取消传送"));
        } else {
            context.getSource().sendMessage(Text.literal("§a已发送传送请求至" + into.getStringifiedName()));
            into.sendMessage(Text.literal("§a玩家 " + from.getStringifiedName() + " 请求传送至你"));
            into.sendMessage(Text.literal("§a请输入 /tpaAccept 以接受传送"));
            into.sendMessage(Text.literal("§a请输入 /tpaDeny 以拒绝传送"));
        }
        return 1;
    }
}
