package sh.okx.omicron.music.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;
import sh.okx.omicron.music.GuildMusicManager;

public class PauseCommand extends Command {
    public PauseCommand(Omicron omicron) {
        super(omicron, "pause");
    }

    @Override
    public void run(Omicron omicron, Guild guild, TextChannel channel, Member member, Message message, String content) {
        GuildMusicManager player = omicron.getMusicManager().getGuildAudioPlayer(guild);

        if(player.scheduler.pause()) {
            channel.sendMessage("Paused music.").queue();
        } else {
            channel.sendMessage("Unpaused music.").queue();
        }
    }
}
