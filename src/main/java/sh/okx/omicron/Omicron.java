package sh.okx.omicron;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.commons.io.IOUtils;
import sh.okx.omicron.command.CommandManager;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class Omicron {
    public static void main(String[] args) throws IOException, LoginException, InterruptedException, RateLimitedException {
        JDA jda = new JDABuilder(AccountType.BOT)
                .setToken(IOUtils.toString(Omicron.class.getResourceAsStream("/token.txt"), "UTF-8"))
                .buildBlocking();

        jda.addEventListener(new CommandManager("o/"));
    }
}
