package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;

import java.io.IOException;

public class RestartCommand extends Command {
    public RestartCommand(Omicron omicron) {
        super(omicron, "restart");
    }

    @Override
    public void run(Guild guild, TextChannel channel, Member member, Message message, String content) {
        try {
            Runtime.getRuntime().exec("./start.sh &> output.log &");
            omicron.getJDA().shutdown();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
