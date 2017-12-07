package sh.okx.omicron.music.commands;

import net.dv8tion.jda.core.entities.*;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

public class VolumeCommand extends Command {
    public VolumeCommand(Omicron omicron) {
        super(omicron, "volume", Category.MUSIC, "A command to tell you how to use discord.");
    }

    @Override
    public void run(Guild guild, MessageChannel channel, Member member, Message message, String content) {
        channel.sendMessage("Right click Omicron in its voice channel and change its user volume for you personally.").queue();
    }
}
