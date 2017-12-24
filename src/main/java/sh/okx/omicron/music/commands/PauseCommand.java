package sh.okx.omicron.music.commands;

import net.dv8tion.jda.core.entities.*;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;
import sh.okx.omicron.music.GuildMusicManager;

public class PauseCommand extends Command {
    public PauseCommand(Omicron omicron) {
        super(omicron, "pause", Category.MUSIC,
                "Toggle whether music is paused.");
    }

    @Override
    public void run(Message message, String content) {
        MessageChannel channel = message.getChannel();
        if(message.getChannelType() != ChannelType.TEXT) {
            channel.sendMessage("This must be run in a guild!").queue();
            return;
        }

        GuildMusicManager player = omicron.getMusicManager().getGuildAudioPlayer(message.getGuild());

        if(player.scheduler.pause()) {
            channel.sendMessage("Paused music.").queue();
        } else {
            channel.sendMessage("Unpaused music.").queue();
        }
    }
}
