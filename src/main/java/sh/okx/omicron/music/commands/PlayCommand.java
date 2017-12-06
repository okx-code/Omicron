package sh.okx.omicron.music.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;

public class PlayCommand extends Command {
    public PlayCommand(Omicron omicron) {
        super(omicron, "play");
    }

    @Override
    public void run(Guild guild, TextChannel channel, Member member, Message message, String content) {
        omicron.getMusicManager().loadAndPlay(member, channel, content);
    }
}
