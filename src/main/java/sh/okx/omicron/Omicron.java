package sh.okx.omicron;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.commons.io.IOUtils;
import sh.okx.omicron.command.CommandManager;
import sh.okx.omicron.feed.FeedManager;
import sh.okx.omicron.util.Data;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class Omicron {
    public static void main(String[] args) throws IOException, LoginException, InterruptedException, RateLimitedException {
        JDA jda = new JDABuilder(AccountType.BOT)
                .setToken(IOUtils.toString(Omicron.class.getResourceAsStream("/token.txt"), "UTF-8"))
                .buildBlocking();

        Omicron omicron = new Omicron();

        omicron.setupData();

        jda.addEventListener(new CommandManager("o/", omicron));
    }

    private Data data;
    private FeedManager feedManager;

    public Omicron() throws IOException {
        setupData();

        feedManager = new FeedManager(this);
    }

    public Data getData() {
        return data;
    }

    public FeedManager getFeedManager() {
        return feedManager;
    }

    private void setupData() throws IOException {
        data = new Data("data.json");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                data.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }
}
