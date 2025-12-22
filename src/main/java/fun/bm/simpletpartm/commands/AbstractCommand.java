package fun.bm.simpletpartm.commands;

public abstract class AbstractCommand {
    protected final String name;

    public AbstractCommand(String name) {
        this.name = name;
    }

    public abstract void register();
}
