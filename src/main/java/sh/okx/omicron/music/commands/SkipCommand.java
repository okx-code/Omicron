package sh.okx.omicron.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SkipCommand extends Command {
    private Map<String, Set<Long>> votes = new HashMap<>();

    public SkipCommand(Omicron omicron) {
        super(omicron, "skip", Category.MUSIC,
                "Skips the currently playing song. People with manage channels permission will instantly skip, " +
                        "otherwise at least 50% of people in the bot's voice channel must vote to skip.\n" +
                        "Usage: **o/skip**");
    }

    @Override
    public void run(Message message, String content) {
        MessageChannel channel = message.getChannel();
        if(message.getChannelType() != ChannelType.TEXT) {
            channel.sendMessage("This must be run in a guild!").queue();
            return;
        }

        Member member = message.getMember();
        Guild guild = member.getGuild();

        if(member.hasPermission(Permission.MANAGE_CHANNEL)) {
            AudioTrack playing = omicron.getMusicManager().getPlaying(guild);
            if(playing == null) {
                channel.sendMessage("Cannot skip: there is no music currently playing.").queue();
                return;
            }

            channel.sendMessage("Force skipping: " + playing.getInfo().title).queue();
            votes.remove(guild.getId());
            omicron.getMusicManager().skip(guild);
            return;
        }

        long id = message.getAuthor().getIdLong();
        Set<Long> voters = votes.getOrDefault(guild.getId(), new HashSet<>());
        if(!voters.add(id)) {
            channel.sendMessage("You have already voted to skip!").queue();
            return;
        }

        votes.put(guild.getId(), voters);

        int required = (int) Math.ceil((guild.getAudioManager().getConnectedChannel().getMembers().size()) / 2.0f);
        int has = votes.get(guild.getId()).size();

        if(has >= required) {

            channel.sendMessage("Skipping: " + omicron.getMusicManager().getGuildAudioPlayer(guild)
                    .player.getPlayingTrack().getInfo().title).queue();
            votes.remove(guild.getId());
            omicron.getMusicManager().skip(guild);
            return;
        }

        channel.sendMessage("Vote added! " + has + "/" + required).queue();
    }
}

