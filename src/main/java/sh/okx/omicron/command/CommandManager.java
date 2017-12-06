package sh.okx.omicron.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.commands.*;
import sh.okx.omicron.custom.CustomCommand;
import sh.okx.omicron.feed.FeedCommand;
import sh.okx.omicron.music.commands.*;
import sh.okx.omicron.roles.RoleCommand;
import sh.okx.omicron.trivia.TriviaCommand;

public class CommandManager extends ListenerAdapter {
    private Omicron omicron;
    private Command[] commands;
    private String prefix;

    public CommandManager(String prefix, Omicron omicron) {
        this.commands = new Command[] {
                new PingCommand(omicron),
                new FeedCommand(omicron),
                new TriviaCommand(omicron),
                new RoleCommand(omicron),
                new ThinkCommand(omicron),
                new PlayCommand(omicron),
                new SkipCommand(omicron),
                new QueueCommand(omicron),
                new LoopCommand(omicron),
                new VolumeCommand(omicron),
                new RemoveCommand(omicron),
                new JoinCommand(omicron),
                new PauseCommand(omicron),
                new CustomCommand(omicron),
                new HelpCommand(omicron),
                new RestartCommand(omicron),
                new ResolveIdCommand(omicron),
        };
        this.prefix = prefix;
        this.omicron = omicron;

        omicron.getJDA().addEventListener(this);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        String[] parts = e.getMessage().getRawContent().split(" ", 2);
        if(!parts[0].startsWith(prefix)) {
            return;
        }

        for(Command command : commands) {
            if(!parts[0].equalsIgnoreCase(prefix + command.getName())) {
                continue;
            }

            command.run(e.getGuild(), e.getTextChannel(), e.getMember(), e.getMessage(),
                    parts.length > 1 ? parts[1] : "");
            return;
        }
    }
}
