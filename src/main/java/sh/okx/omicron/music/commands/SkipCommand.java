package sh.okx.omicron.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;

import java.util.HashMap;
import java.util.Map;

public class SkipCommand extends Command {
    private Map<String, Integer> votes = new HashMap<>();

    public SkipCommand(Omicron omicron) {
        super(omicron, "skip");
    }

    @Override
    public void run(Guild guild, TextChannel channel, Member member, Message message, String content) {
        if(member.hasPermission(Permission.MANAGE_CHANNEL)) {
            AudioTrack playing = omicron.getMusicManager().getPlaying(guild);
            if(playing == null) {
                channel.sendMessage("Cannot skip: there is no music currently playing.").queue();
                return;
            }

            channel.sendMessage("Skipping: " + playing.getInfo().title).queue();
            votes.remove(guild.getId());
            omicron.getMusicManager().skip(guild);
            return;
        }

        votes.put(guild.getId(), votes.getOrDefault(guild.getId(), 0)+1);

        int required = (int) Math.ceil((guild.getAudioManager().getConnectedChannel().getMembers().size()-1) / 2.0f);
        int has = votes.get(guild.getId());

        if(has >= required) {
            channel.sendMessage("Skipping song; " + required + " votes reached.").queue();
            votes.remove(guild.getId());
            omicron.getMusicManager().skip(guild);
            return;
        }

        channel.sendMessage("Vote added! " + has + "/" + required).queue();
    }
}

