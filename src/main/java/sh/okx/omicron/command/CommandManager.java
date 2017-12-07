package sh.okx.omicron.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.commands.HelpCommand;
import sh.okx.omicron.command.commands.ResolveIdCommand;
import sh.okx.omicron.command.commands.RestartCommand;
import sh.okx.omicron.command.commands.ThinkCommand;
import sh.okx.omicron.custom.CustomCommand;
import sh.okx.omicron.evaluate.commands.NodeJsCommand;
import sh.okx.omicron.evaluate.commands.Python2Command;
import sh.okx.omicron.evaluate.commands.PythonCommand;
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
                new PythonCommand(omicron),
                new Python2Command(omicron),
                new NodeJsCommand(omicron),
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

    public Command[] getCommands() {
        return commands;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if(e.getAuthor().isBot()) {
            return;
        }

        String[] parts = e.getMessage().getRawContent().split(" ", 2);
        if(!parts[0].startsWith(prefix)) {
            return;
        }

        for(Command command : commands) {
            boolean useAlias = false;
            for(String alias : command.getAliases()) {
                if(parts[0].equalsIgnoreCase(prefix + alias)) {
                    useAlias = true;
                    break;
                }
            }
            if(!useAlias && !parts[0].equalsIgnoreCase(prefix + command.getName())) {
                continue;
            }

            command.run(e.getGuild(), e.getChannel(), e.getMember(), e.getMessage(),
                    parts.length > 1 ? parts[1] : "");
            return;
        }
    }
}
