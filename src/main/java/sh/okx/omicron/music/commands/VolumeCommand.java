package sh.okx.omicron.music.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;

public class VolumeCommand extends Command {
    public VolumeCommand(Omicron omicron) {
        super(omicron, "volume");
    }

    @Override
    public void run(Guild guild, TextChannel channel, Member member, Message message, String content) {
        channel.sendMessage("Right click Omicron in its voice channel and change its user volume for you personally.").queue();
    }
}
