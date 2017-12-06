package sh.okx.omicron.command;

import net.dv8tion.jda.core.entities.*;
import sh.okx.omicron.Omicron;

public abstract class Command {
    protected Omicron omicron;
    protected String name;

    public Command(Omicron omicron, String name) {
        this.omicron = omicron;
        this.name = name;
    }

    public final Omicron getOmicron() {
        return omicron;
    }

    public final String getName() {
        return name;
    }

    public abstract void run(Guild guild, TextChannel channel, Member member, Message message, String content);
}
