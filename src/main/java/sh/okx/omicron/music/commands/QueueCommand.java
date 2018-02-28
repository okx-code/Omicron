package sh.okx.omicron.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;
import sh.okx.omicron.music.GuildMusicManager;
import sh.okx.omicron.music.TrackData;

import java.util.ArrayList;
import java.util.List;

public class QueueCommand extends Command {
  public QueueCommand(Omicron omicron) {
    super(omicron, "queue", Category.MUSIC,
        "Show the song currently playing and those which are in the queue to play later.");
  }

  @Override
  public void run(Message message, String content) {
    MessageChannel channel = message.getChannel();
    if (message.getChannelType() != ChannelType.TEXT) {
      channel.sendMessage("This must be run in a guild!").queue();
      return;
    }

    Guild guild = message.getGuild();

    GuildMusicManager musicManager = omicron.getMusicManager().getGuildAudioPlayer(guild);

    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Queued songs");

    eb.addField("Currently playing", format(musicManager.player.getPlayingTrack()), false);

    List<String> queue = new ArrayList<>();
    if (musicManager.scheduler.getQueue().size() > 0) {
      int i = 0;
      for (TrackData queueItem : musicManager.scheduler.getQueue()) {
        queue.add(++i + ". " + format(queueItem.getTrack()));
        if (i >= 10) {
          break;
        }
      }

      eb.addField("Queue", String.join("\n", queue), false);
    }

    channel.sendMessage(eb.build()).queue();
  }

  private String format(AudioTrack audioTrack) {
    if (audioTrack == null) {
      return "None.";
    }

    AudioTrackInfo info = audioTrack.getInfo();
    return info.title + " (" + omicron.getMusicManager().formatTime(info.length) + ")";
  }
}
