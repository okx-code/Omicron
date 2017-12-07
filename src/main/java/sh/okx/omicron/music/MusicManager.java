package sh.okx.omicron.music;

import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Video;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;
import sh.okx.omicron.Omicron;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class MusicManager {
    private Omicron omicron;

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    public MusicManager(Omicron omicron) {
        this.omicron = omicron;
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);

        omicron.getJDA().addEventListener(new MusicListener(omicron));
    }

    public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = guild.getIdLong();
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(omicron, guild, playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public void skip(Guild guild) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        musicManager.scheduler.nextTrack();
    }

    public AudioTrack getPlaying(Guild guild) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        return musicManager.player.getPlayingTrack();
    }

    public void loadAndPlay(Member requester, final TextChannel channel, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());


        Video video;
        if(trackUrl.matches("https?://(www\\.)?youtube.com/watch\\?v=[A-z0-9\\-_]+")) {
            video = YoutubeAPI.getVideo(trackUrl.split("\\?v=")[1]);
        } else {
            ResourceId resourceId = YoutubeAPI.search(trackUrl);
            if(resourceId == null) {
                channel.sendMessage("No results found by **" + trackUrl + "**.").queue();
                return;
            }
            video = YoutubeAPI.getVideo(resourceId.getVideoId());
        }

        if(video == null) {
            channel.sendMessage("An unknown error occurred trying to get the video. " +
                    "This has been reported to the developers.").queue();
            return;
        }


        playerManager.loadItemOrdered(musicManager,
                "https://youtube.com/watch?v=" + video.getId(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if(!play(requester, channel, musicManager, new TrackData(track, requester.getUser()))) {
                    channel.sendMessage("Could not connect to any voice channels. Do I have permission? " +
                            "Do any exist?").queue();
                } else {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setAuthor(video.getSnippet().getChannelTitle(),
                            "https://youtube.com/channel/" + video.getSnippet().getChannelId());
                    eb.setTitle(video.getSnippet().getTitle(),
                            "https://youtube.com/watch?v=" + video.getId());
                    eb.addField("Estimated Wait Time",
                            formatTime(musicManager.scheduler.getQueue()
                                    .stream()
                                    .filter(audioTrack -> audioTrack.equals(track))
                                    .map(TrackData::getTrack)
                                    .mapToLong(AudioTrack::getDuration)
                                    .sum()+musicManager.player.getPlayingTrack().getInfo().length), true);
                    eb.addField("Length", formatTime(track.getInfo().length), true);
                    eb.setThumbnail(video.getSnippet().getThumbnails().getHigh().getUrl());
                    eb.setColor(Color.WHITE);
                    eb.setTimestamp(Instant.ofEpochMilli(video.getSnippet().getPublishedAt().getValue()));

                    channel.sendMessage(eb.build()).queue();
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                /*AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

                if(!play(channel, musicManager, firstTrack)) {

                }*/

                channel.sendMessage("Playlists currently unsupported").queue();
            }

            @Override
            public void noMatches() {
                channel.sendMessage("An unknown error occured when getting " + trackUrl + ".").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    private boolean play(Member member, TextChannel channel, GuildMusicManager musicManager, TrackData track) {
        if(member.getVoiceState().inVoiceChannel()) {
            channel.getGuild().getAudioManager().openAudioConnection(member.getVoiceState().getChannel());
        } else if(!connectToFirstVoiceChannel(channel.getGuild().getAudioManager())) {
            return false;
        }

        musicManager.scheduler.queue(track);
        return true;
    }

    public void leave(Guild guild) {
        guild.getAudioManager().closeAudioConnection();
    }

    private boolean connectToFirstVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                try {
                    audioManager.openAudioConnection(voiceChannel);
                    return true;
                } catch(Exception ignored) { }
            }

            return false;
        }

        return true;
    }

    public String formatTime(long millis) {
        SimpleDateFormat df;
        if(millis > 1000*60*60) {
            df = new SimpleDateFormat("hh:mm:ss");
        } else {
            df = new SimpleDateFormat("mm:ss");
        }
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = new Date(millis);
        return df.format(date);
    }
}
