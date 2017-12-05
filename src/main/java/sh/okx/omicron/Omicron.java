package sh.okx.omicron;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
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

public class Omicron {
    public static void main(String[] args) throws IOException, LoginException,
            InterruptedException, RateLimitedException, URISyntaxException {
        JDA jda = new JDABuilder(AccountType.BOT)
                .setToken(IOUtils.toString(new File("token.txt").toURI(), "UTF-8").trim())
                .setGame(Game.of(Game.GameType.DEFAULT, "o/help"))
                .buildBlocking();

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

        this.jda = jda;
        this.feedManager = new FeedManager(this);
        this.triviaManager = new TriviaManager(this);
        this.musicManager = new MusicManager(this);
        this.commandManager = new CommandManager("o/", this);
        this.roleManager = new RoleManager(this);
        this.customManager = new CustomManager(this);
    }

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
            try {
                roleManager.save();
                customManager.save();
                feedManager.save();

                data.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }
}