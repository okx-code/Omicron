package sh.okx.omicron.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.User;

public class TrackData {
    private AudioTrack track;
    private User requestedBy;

    public TrackData(AudioTrack track, User requestedBy) {
        this.track = track;
        this.requestedBy = requestedBy;
    }

    public AudioTrack getTrack() {
        return track;
    }

    public User getRequestedBy() {
        return requestedBy;
    }
}
