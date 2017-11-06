package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.command.Command;

public class PingCommand extends Command {
    public PingCommand() {
        super("ping");
    }

    @Override
    public void run(Guild guild, TextChannel channel, Member member, Message message) {
        channel.sendMessage("Pong").queue();
    }
}
