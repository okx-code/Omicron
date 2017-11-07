package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;

public class PingCommand extends Command {
    public PingCommand(Omicron omicron) {
        super(omicron, "ping");
    }

    @Override
    public void run(Omicron omicron, Guild guild, TextChannel channel, Member member, Message message, String content) {
        channel.sendMessage("WebSocket Ping: " +omicron.getJDA().getPing() + "ms | " +
                "Message Ping: " + (System.currentTimeMillis()-message.getCreationTime().toInstant().toEpochMilli()) +
                "ms.").queue();
    }
}
