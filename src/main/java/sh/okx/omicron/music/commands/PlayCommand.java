package sh.okx.omicron.music.commands;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
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
  public void run(Message message, String content) {
    MessageChannel channel = message.getChannel();
    if (message.getChannelType() != ChannelType.TEXT) {
      channel.sendMessage("This must be run in a guild!").queue();
      return;
    }

    Member member = message.getMember();

    omicron.getMusicManager().loadAndPlay(member, (TextChannel) channel, content);
  }
}
