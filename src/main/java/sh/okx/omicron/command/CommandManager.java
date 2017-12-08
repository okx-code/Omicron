package sh.okx.omicron.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.alias.AliasCommand;
import sh.okx.omicron.command.commands.*;
import sh.okx.omicron.custom.CustomCommand;
import sh.okx.omicron.evaluate.commands.NodeJsCommand;
import sh.okx.omicron.evaluate.commands.Python2Command;
import sh.okx.omicron.evaluate.commands.PythonCommand;
import sh.okx.omicron.feed.FeedCommand;
import sh.okx.omicron.music.commands.*;
import sh.okx.omicron.roles.RoleCommand;
import sh.okx.omicron.trivia.TriviaCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

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
                new DisableCommand(omicron),
                new EnableCommand(omicron),
                new AliasCommand(omicron),
        };
        this.prefix = prefix;
        this.omicron = omicron;

        omicron.getJDA().addEventListener(this);

        new Thread(() -> {

            try {
                Connection connection = omicron.getConnection();

                Statement statement = connection.createStatement();
                statement.execute("CREATE TABLE IF NOT EXISTS disabled_commands (command VARCHAR(255), guild BIGINT(20) );");

                statement.close();
                connection.close();

                System.out.println("Loaded commands.");
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }).start();
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

            if (e.getGuild() != null && isDisabled(e.getGuild().getIdLong(), command)) {
                // only this command will have matched by this point
                break;
            }

            command.run(e.getGuild(), e.getChannel(), e.getMember(), e.getMessage(),
                    parts.length > 1 ? parts[1] : "");
            return;
        }
    }

    public boolean isDisabled(long guild, Command command) {
        try {
            Connection connection = omicron.getConnection();

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM disabled_commands WHERE " +
                    "command=? AND guild=?");

            statement.setString(1, command.getName());
            statement.setLong(2, guild);

            boolean yes = statement.executeQuery().next();

            statement.close();
            connection.close();

            return yes;
        } catch(SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void setDisabled(Command command, long guild, boolean disable) {
        String name = command.getName();
        new Thread(() -> {
            try {
                Connection connection = omicron.getConnection();

                PreparedStatement statement;
                if (disable) {
                    statement = connection.prepareStatement("INSERT INTO disabled_commands (command, guild) VALUES (?, ?);");
                } else {
                    statement = connection.prepareStatement("DELETE FROM disabled_commands WHERE command=? AND guild=?");
                }

                statement.setString(1, name);
                statement.setLong(2, guild);

                statement.execute();

                statement.close();
                connection.close();
            } catch(SQLException ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}
