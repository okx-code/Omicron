package sh.okx.omicron.music.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;
import sh.okx.omicron.music.TrackScheduler;

public class LoopCommand extends Command {
    public LoopCommand(Omicron omicron) {
        super(omicron, "loop");
    }

    @Override
    public void run(Omicron omicron, Guild guild, TextChannel channel, Member member, Message message, String content) {
        TrackScheduler scheduler = omicron.getMusicManager().getGuildAudioPlayer(guild).scheduler;
        if(scheduler.isLooping()) {
            channel.sendMessage("Cancelled looping.").queue();
        } else {
            channel.sendMessage("The next song in the queue will now be looped.").queue();
        }

        scheduler.setLooping(!scheduler.isLooping());
    }
}
