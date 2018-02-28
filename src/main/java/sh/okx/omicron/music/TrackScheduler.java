package sh.okx.omicron.music;


import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.entities.Guild;
import sh.okx.omicron.Omicron;

import java.util.ArrayList;
import java.util.List;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
  private final AudioPlayer player;
  private List<TrackData> queue;
  private boolean looping = false;
  private Omicron omicron;
  private Guild guild;

  public List<TrackData> getQueue() {
    return queue;
  }

  public void remove(int at) {
    queue.remove(at);
  }

  /**
   * @param player The audio player this scheduler uses
   */
  public TrackScheduler(Omicron omicron, Guild guild, AudioPlayer player) {
    this.omicron = omicron;
    this.guild = guild;
    this.player = player;
    this.queue = new ArrayList<>();
  }

  /**
   * Add the next track to queue or play right away if nothing is in the queue.
   *
   * @param track The track to play or add to queue.
   */
  public void queue(TrackData track) {
    // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
    // something is playing, it returns false and does nothing. In that case the player was already playing so this
    if (!player.startTrack(track.getTrack(), true)) {
      queue.add(track);
    }
  }

  public void setLooping(boolean looping) {
    this.looping = looping;
  }

  public boolean isLooping() {
    return looping;
  }

  /**
   * Start the next track, stopping the current one if it is playing.
   */
  public void nextTrack() {
    AudioTrack poll;
    if (queue.size() < 1) {
      poll = null;
    } else {
      poll = looping ? queue.get(0).getTrack().makeClone() : queue.remove(0).getTrack();
    }

    player.startTrack(poll, false);
  }

  public void clearAndStop() {
    queue.clear();
    player.stopTrack();
  }

  public void setVolume(int vol) {
    player.setVolume(vol);
  }

  public boolean pause() {
    player.setPaused(!player.isPaused());
    return player.isPaused();
  }

  @Override
  public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
    // Only start the next track if the end reason is suitable for it
    if (endReason.mayStartNext) {
      nextTrack();
    }
  }
}