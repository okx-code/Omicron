package sh.okx.omicron.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.commands.AddFeedCommand;
import sh.okx.omicron.command.commands.PingCommand;
import sh.okx.omicron.trivia.TriviaCommand;

import java.util.Set;

public class CommandManager extends ListenerAdapter {
    private Omicron omicron;
    private Set<Command> commands;
    private String prefix;

    public CommandManager(String prefix, Omicron omicron) {
        this.commands = Set.of(
                new PingCommand(omicron),
                new AddFeedCommand(omicron),
                new TriviaCommand(omicron));
        this.prefix = prefix;
        this.omicron = omicron;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        String[] parts = e.getMessage().getContent().split(" ", 2);
        if(!parts[0].startsWith(prefix)) {
            return;
        }

        for(Command command : commands) {
            if(!parts[0].equalsIgnoreCase(prefix + command.getName())) {
                continue;
            }

            command.run(omicron, e.getGuild(), e.getTextChannel(), e.getMember(), e.getMessage(),
                    parts.length > 1 ? parts[1] : "");
            return;
        }
    }
}
