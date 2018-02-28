package sh.okx.omicron.music.commands;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;
import sh.okx.omicron.music.TrackScheduler;

public class LoopCommand extends Command {
  public LoopCommand(Omicron omicron) {
    super(omicron, "loop", Category.MUSIC,
        "Toggle looping. When enabled, this will repeat the next queued song forever.\n" +
            "Usage: **o/loop**.");
  }

  @Override
  public void run(Message message, String content) {
    MessageChannel channel = message.getChannel();
    if (message.getChannelType() != ChannelType.TEXT) {
      channel.sendMessage("This must be run in a guild!").queue();
      return;
    }

    TrackScheduler scheduler = omicron.getMusicManager().getGuildAudioPlayer(message.getGuild()).scheduler;
    if (scheduler.isLooping()) {
      channel.sendMessage("Cancelled looping.").queue();
    } else {
      channel.sendMessage("The next song in the queue will now be looped.").queue();
    }

    scheduler.setLooping(!scheduler.isLooping());
  }
}
