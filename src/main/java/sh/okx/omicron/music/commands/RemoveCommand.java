package sh.okx.omicron.music.commands;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;
import sh.okx.omicron.music.TrackData;

import java.util.List;

public class RemoveCommand extends Command {
    public RemoveCommand(Omicron omicron) {
        super(omicron, "remove");
    }

    @Override
    public void run(Omicron omicron, Guild guild, TextChannel channel, Member member, Message message, String content) {
        List<TrackData> queue = omicron.getMusicManager().getGuildAudioPlayer(guild).scheduler.getQueue();

        int index;
        try {
            index = Integer.parseInt(content) - 1;
            if(index < 0 || index >= queue.size()) {
                channel.sendMessage("Number too large or small").queue();
                return;
            }
        } catch(NumberFormatException ex) {
            channel.sendMessage("Invalid number").queue();
            return;
        }

        TrackData data = queue.get(index);
        if(!data.getRequestedBy().getId().equals(member.getUser().getId()) &&
                !member.hasPermission(Permission.MANAGE_CHANNEL)) {
            channel.sendMessage("You must have permission to manage channels or be the person who added the song, " +
                    "in order to remove it").queue();
            return;
        }
        channel.sendMessage("Removed song: " + queue.remove(index).getTrack().getInfo().title).queue();
    }
}
