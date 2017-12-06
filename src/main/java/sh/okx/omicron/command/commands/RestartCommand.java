package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class RestartCommand extends Command {
    public RestartCommand(Omicron omicron) {
        super(omicron, "restart");
    }

    @Override
    public void run(Guild guild, TextChannel channel, Member member, Message message, String content) {
        try {
            channel.sendMessage("Pulling from git and shutting down...").complete();
            Runtime.getRuntime().exec("./start.sh &> output.log &");
            omicron.getJDA().shutdown();

            File shutdownChannel = new File("shutdown_channel.txt");
            Files.write(shutdownChannel.toPath(), channel.getId().getBytes(),
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
