package sh.okx.omicron.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sh.okx.omicron.command.commands.PingCommand;

import java.util.Set;

public class CommandManager extends ListenerAdapter {
    private Set<Command> commands;
    private String prefix;

    public CommandManager(String prefix) {
        this.commands = Set.of(new PingCommand());
        this.prefix = prefix;
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

            command.run(e.getGuild(), e.getTextChannel(), e.getMember(), e.getMessage());
            return;
        }
    }
}
