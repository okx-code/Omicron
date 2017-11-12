package sh.okx.omicron.music;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sh.okx.omicron.Omicron;

import java.util.List;

public class MusicListener extends ListenerAdapter {
    private Omicron omicron;

    public MusicListener(Omicron omicron) {
        this.omicron = omicron;
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent e) {
        VoiceChannel left = e.getChannelLeft();
        List<Member> members = left.getMembers();
        if(members.size() != 1) {
            return;
        }

        if(members.get(0).getUser().getId().equals(e.getJDA().getSelfUser().getId())) {
            omicron.getMusicManager().leave(left.getGuild());
        }
    }
}
