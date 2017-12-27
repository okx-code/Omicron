package sh.okx.omicron;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.okx.omicron.command.CommandManager;
import sh.okx.omicron.custom.CustomManager;
import sh.okx.omicron.evaluate.EvaluateManager;
import sh.okx.omicron.feed.FeedManager;
import sh.okx.omicron.logging.LoggingManager;
import sh.okx.omicron.minecraft.MinecraftManager;
import sh.okx.omicron.music.MusicManager;
import sh.okx.omicron.roles.RoleManager;
import sh.okx.omicron.trivia.TriviaManager;
import sh.okx.sql.ConnectionBuilder;
import sh.okx.sql.api.Connection;
import sh.okx.sql.api.PooledConnection;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class Omicron {
    public static void main(String[] args) throws IOException, LoginException,
            InterruptedException, RateLimitedException {
        JDA jda = new JDABuilder(AccountType.BOT)
                .setToken(IOUtils.toString(new File("token.txt").toURI(), "UTF-8").trim())
                .setGame(Game.of(Game.GameType.DEFAULT, "o/help"))
                .buildBlocking();

        File shutdownChannel = new File("shutdown_channel.txt");
        if(shutdownChannel.exists()) {
            String channelId = IOUtils.toString(shutdownChannel.toURI(), "UTF-8");
            TextChannel channel = jda.getTextChannelById(channelId);
            if(channel != null) {
                channel.sendMessage("Successfully restarted!").queue();
            }
            shutdownChannel.delete();
        }

        new Omicron(jda);
    }

    private JDA jda;
    private FeedManager feedManager;
    private TriviaManager triviaManager;
    private MusicManager musicManager;
    private CommandManager commandManager;
    private RoleManager roleManager;
    private CustomManager customManager;
    private EvaluateManager evaluateManager;
    private MinecraftManager minecraftManager;
    private LoggingManager loggingManager;

    private PooledConnection connectionPool;

    public Omicron(JDA jda) throws IOException {
        this.jda = jda;
        connect();
        setupManagers();
    }

    private void connect() throws IOException {
        connectionPool = new ConnectionBuilder()
                .setCredentials("root",
                        IOUtils.toString(new File("db_password.txt").toURI(), "UTF-8").trim())
                .setDatabase("omicron")
                .buildPool();
    }

    private void setupManagers() {
        this.feedManager = new FeedManager(this);
        this.triviaManager = new TriviaManager(this);
        this.musicManager = new MusicManager(this);
        this.commandManager = new CommandManager("o/", this);
        this.roleManager = new RoleManager(this);
        this.customManager = new CustomManager(this);
        this.evaluateManager = new EvaluateManager();
        this.minecraftManager = new MinecraftManager(this);
        this.loggingManager = new LoggingManager(this);
    }

    /**
     * Get a connection from the pool.
     * This must be closed!
     * @return A connection form the pool.
     */
    public Connection getConnection() {
        return connectionPool.getConnection();
    }

    /**
     * Run a {@link Consumer} and close the connection to return it to the pool.
     * @param consumer The synchronous operation(s) on the {@link Connection}.
     */
    public void runConnection(Consumer<Connection> consumer) {
        Connection connection = getConnection();
        consumer.accept(connection);
        connection.close();
    }

    /**
     * Run a {@link Function} asynchronously and then close the connection to return it to the pool.
     * @param function The asynchronous operation(s) on the {@link Connection}.
     */
    public <T> CompletableFuture<T> runConnectionAsync(Function<Connection, CompletableFuture<T>> function) {
        Connection connection = getConnection();
        return function.apply(connection).whenComplete((a, b) -> connection.close());
    }

    public Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    public boolean isDeveloper(long id) {
        return id == 115090410849828865L || id == 181103798616326144L;
    }

    /**
     * This is the recommended way to get a JDA object
     * @return A copy of the JDA object
     */
    public JDA getJDA() {
        return jda;
    }

    public CustomManager getCustomManager() {
        return customManager;
    }

    public FeedManager getFeedManager() {
        return feedManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public TriviaManager getTriviaManager() {
        return triviaManager;
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public EvaluateManager getEvaluateManager() {
        return evaluateManager;
    }

    public MinecraftManager getMinecraftManager() {
        return minecraftManager;
    }

    public LoggingManager getLoggingManager() {
        return loggingManager;
    }
}