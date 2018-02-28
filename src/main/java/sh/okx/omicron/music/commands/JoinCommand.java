package sh.okx.omicron.music.commands;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.GuildVoiceState;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

public class JoinCommand extends Command {
  public JoinCommand(Omicron omicron) {
    super(omicron, "join", Category.MUSIC,
        "Make the bot join your current voice channel. It is rare that you will need to use this, " +
            "as the bot will automatically join your channel when you queue a song.");
  }

  @Override
  public void run(Message message, String content) {
    MessageChannel channel = message.getChannel();
    if (message.getChannelType() != ChannelType.TEXT) {
      channel.sendMessage("This must be run in a guild!").queue();
      return;
    }

    Guild guild = message.getGuild();
    Member member = message.getMember();

    GuildVoiceState voiceState = member.getVoiceState();
    if (voiceState.inVoiceChannel()) {
      guild.getAudioManager().openAudioConnection(voiceState.getChannel());
      channel.sendMessage("Joined your channel.").queue();
    } else {
      channel.sendMessage("You are not in a voice channel.").queue();
    }
  }
}
