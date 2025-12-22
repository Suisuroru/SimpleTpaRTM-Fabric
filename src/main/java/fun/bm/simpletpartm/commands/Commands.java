package fun.bm.simpletpartm.commands;

import fun.bm.simpletpartm.commands.command.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class Commands {
    private static final Set<Supplier<AbstractCommand>> commandSet = new HashSet<>();

    public static void register() {
        commandSet.forEach(command -> command.get().register());
    }

    public static void initCommandSet() {
        addCommand(
                TpaConfigCommand::new,
                TpaCommand::new,
                TpaCancelCommand::new,
                TpaAcceptCommand::new,
                TpaAcceptAllCommand::new,
                TpaDenyCommand::new,
                TpaDenyAllCommand::new,
                TpHereCommand::new,
                TpHereAcceptCommand::new,
                TpHereDenyCommand::new,
                TpHereDenyAllCommand::new,
                TpHereCancelCommand::new,
                BackCommand::new
        );
    }

    @SafeVarargs
    private static void addCommand(Supplier<AbstractCommand>... commands) {
        commandSet.addAll(Arrays.asList(commands));
    }
}
