package sh.okx.omicron.music.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

public class PlayCommand extends Command {
    public PlayCommand(Omicron omicron) {
        super(omicron, "play", Category.MUSIC,
                "Play a song. This only supports YouTube videos currently.\n" +
                "The bot will automatically join whichever voice channel you are in, " +
                "or the first it has access to if you are not in a voice channel\n" +
                "Usage: **o/play <url / search videos>**.");
    }

    @Override
    public void run(Guild guild, TextChannel channel, Member member, Message message, String content) {
        omicron.getMusicManager().loadAndPlay(member, channel, content);
    }
}
