package sh.okx.omicron.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.commands.*;
import sh.okx.omicron.custom.CustomCommand;
import sh.okx.omicron.evaluate.commands.NodeJsCommand;
import sh.okx.omicron.evaluate.commands.Python2Command;
import sh.okx.omicron.evaluate.commands.PythonCommand;
import sh.okx.omicron.feed.FeedCommand;
import sh.okx.omicron.minecraft.MinecraftCommand;
import sh.okx.omicron.minecraft.TokenCommand;
import sh.okx.omicron.music.commands.*;
import sh.okx.omicron.roles.RoleCommand;
import sh.okx.omicron.trivia.TriviaCommand;
import sh.okx.sql.api.Connection;
import sh.okx.sql.api.query.QueryResults;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

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
                new TokenCommand(omicron),
                new MinecraftCommand(omicron),
                new InfoCommand(omicron),
        };
        this.prefix = prefix;
        this.omicron = omicron;

        omicron.getJDA().addEventListener(this);

        omicron.runConnectionAsync(connection ->
                connection.table("disabled_commands")
                .create()
                .ifNotExists()
                .column("command VARCHAR(255)")
                .column("guild BIGINT(20)")
                .executeAsync()
                .thenAccept(i -> omicron.getLogger().info("Loaded commands with status {}", i)));
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
        if(e.getAuthor().isBot() || e.getAuthor().isFake()) {
            return;
        }

        String[] parts = e.getMessage().getContentRaw().split(" ", 2);
        if(!parts[0].startsWith(prefix)) {
            return;
        }

        for (Command command : commands) {
            boolean useAlias = false;
            for (String alias : command.getAliases()) {
                if (parts[0].equalsIgnoreCase(prefix + alias)) {
                    useAlias = true;
                    break;
                }
            }
            if (!useAlias && !parts[0].equalsIgnoreCase(prefix + command.getName())) {
                continue;
            }

            if (e.getGuild() != null) {
                isDisabled(e.getGuild().getIdLong(), command).thenAccept(b -> {
                    if (!b) {
                        command.run(e.getMessage(), parts.length > 1 ? parts[1] : "");
                    }
                });
                return;
            }

            command.run(e.getMessage(), parts.length > 1 ? parts[1] : "");
            return;
        }
    }

    public CompletableFuture<Boolean> isDisabled(long guild, Command command) {
        return omicron.runConnectionAsync(connection -> isDisabled(connection, guild, command));
    }

    public CompletableFuture<Boolean> isDisabled(sh.okx.sql.api.Connection connection, long guild, Command command) {
        return connection.table("disabled_commands")
                .select()
                .where()
                .prepareEquals("command", command.getName())
                .and()
                .prepareEquals("guild", guild)
                .then().executeAsync()
                .thenApply(QueryResults::next);
    }

    public void setDisabled(Command command, long guild, boolean disable) {
        String name = command.getName();
        CompletableFuture.runAsync(() -> {
            try(Connection connection = omicron.getConnection();
                PreparedStatement statement = connection.getUnderlying().prepareStatement(disable ?
                        "INSERT INTO disabled_commands (command, guild) VALUES (?, ?);" :
                        "DELETE FROM disabled_commands WHERE command=? AND guild=?")) {
                statement.setString(1, name);
                statement.setLong(2, guild);

                statement.execute();
            } catch(SQLException ex) {
                ex.printStackTrace();
            }
        });
    }
}
