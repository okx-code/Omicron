package sh.okx.omicron.command;

import net.dv8tion.jda.core.entities.*;

public abstract class Command {
    private String name;

    public Command(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void run(Guild guild, TextChannel channel, Member member, Message message);
}
