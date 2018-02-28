package sh.okx.omicron.music.commands;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

public class ForceSkipCommand extends Command {
  public ForceSkipCommand(Omicron omicron) {
    super(omicron, "forceskip", Category.MUSIC,
        "Forcefully skips the currently playing song, requires permission to manage channels.", "fskip");
  }

  @Override
  public void run(Message message, String args) {
    MessageChannel channel = message.getChannel();
    if (message.getChannelType() != ChannelType.TEXT) {
      channel.sendMessage("This must be run in a guild!").queue();
      return;
    }

    Member member = message.getMember();
    if (!member.hasPermission(Permission.MANAGE_CHANNEL)) {
      channel.sendMessage("You must have permission to manage channels!").queue();
      return;
    }

    Guild guild = message.getGuild();

    channel.sendMessage("Skipping: " + omicron.getMusicManager().getGuildAudioPlayer(guild)
        .player.getPlayingTrack().getInfo().title).queue();
    omicron.getMusicManager().skip(guild);
  }
}
