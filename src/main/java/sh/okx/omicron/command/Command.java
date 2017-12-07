package sh.okx.omicron.command;

import net.dv8tion.jda.core.entities.*;
import sh.okx.omicron.Omicron;

public abstract class Command {
    protected Omicron omicron;
    protected String name;
    protected Category category;
    protected String description;

    public Command(Omicron omicron, String name, Category category, String description) {
        this.omicron = omicron;
        this.name = name;
        this.category = category;
        this.description = description;
    }

    public final Omicron getOmicron() {
        return omicron;
    }

    public final String getName() {
        return name;
    }

    public final Category getCategory() {
        return category;
    }

    public final String getDescription() {
        return description;
    }

    public abstract void run(Guild guild, MessageChannel channel, Member member, Message message, String content);
}
