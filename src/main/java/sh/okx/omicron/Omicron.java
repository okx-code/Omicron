package sh.okx.omicron;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.commons.io.IOUtils;
import sh.okx.omicron.command.CommandManager;
import sh.okx.omicron.custom.CustomManager;
import sh.okx.omicron.feed.FeedManager;
import sh.okx.omicron.music.MusicManager;
import sh.okx.omicron.roles.RoleManager;
import sh.okx.omicron.trivia.TriviaManager;
import sh.okx.omicron.util.Data;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Omicron {
    private final String sqlPassword;

    public static void main(String[] args) throws IOException, LoginException,
            InterruptedException, RateLimitedException, URISyntaxException {
        //System.setProperty("user.timezone", "UTC");

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

        Omicron omicron = new Omicron(jda);
        omicron.setupData();
    }

    private JDA jda;
    private Data data;
    private FeedManager feedManager;
    private TriviaManager triviaManager;
    private MusicManager musicManager;
    private CommandManager commandManager;
    private RoleManager roleManager;
    private CustomManager customManager;

    public Omicron(JDA jda) throws IOException {
        setupData();

        this.sqlPassword = IOUtils.toString(new File("db_password.txt").toURI(), "UTF-8").trim();

        this.jda = jda;
        this.feedManager = new FeedManager(this);
        this.triviaManager = new TriviaManager(this);
        this.musicManager = new MusicManager(this);
        this.commandManager = new CommandManager("o/", this);
        this.roleManager = new RoleManager(this);
        this.customManager = new CustomManager(this);
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/omicron",
                    "root", sqlPassword);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This is the recommended way to get a JDA object
     * @return A copy of the JDA object
     */
    public JDA getJDA() {
        return jda;
    }

    public Data getData() {
        return data;
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

    private void setupData() throws IOException {
        data = new Data("data.json");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down and saving data.");
            customManager.save();
            System.out.println("Success!");
        }));
    }
}