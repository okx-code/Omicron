package sh.okx.omicron;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.commons.io.IOUtils;
import sh.okx.omicron.command.CommandManager;
import sh.okx.omicron.feed.FeedManager;
import sh.okx.omicron.trivia.TriviaManager;
import sh.okx.omicron.util.Data;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class Omicron {
    public static void main(String[] args) throws IOException, LoginException, InterruptedException, RateLimitedException {
        JDA jda = new JDABuilder(AccountType.BOT)
                .setToken(IOUtils.toString(Omicron.class.getResourceAsStream("/token.txt"), "UTF-8"))
                .buildBlocking();

        Omicron omicron = new Omicron(jda);
        omicron.setupData();

        jda.addEventListener(new CommandManager("o/", omicron));
    }

    private JDA jda;
    private Data data;
    private FeedManager feedManager;
    private TriviaManager triviaManager;

    public Omicron(JDA jda) throws IOException {
        setupData();

        this.jda = jda;
        this.feedManager = new FeedManager(this);
        this.triviaManager = new TriviaManager(this);
    }

    public JDA getJDA() {
        return jda;
    }

    public Data getData() {
        return data;
    }

    public FeedManager getFeedManager() {
        return feedManager;
    }

    public TriviaManager getTriviaManager() {
        return triviaManager;
    }

    private void setupData() throws IOException {
        data = new Data("data.json");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                data.save();
                feedManager.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }
}
