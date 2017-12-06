package sh.okx.omicron.music.commands;

import net.dv8tion.jda.core.entities.*;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

public class JoinCommand extends Command {
    public JoinCommand(Omicron omicron) {
        super(omicron, "join", Category.MUSIC,
                "Make the bot join your current voice channel. It is rare that you will need to use this, " +
                        "as the bot will automatically join your channel when you queue a song.");
    }

    @Override
    public void run(Guild guild, TextChannel channel, Member member, Message message, String content) {
        GuildVoiceState voiceState = member.getVoiceState();
        if(voiceState.inVoiceChannel()) {
            guild.getAudioManager().openAudioConnection(voiceState.getChannel());
            channel.sendMessage("Joined your channel.").queue();
        } else {
            channel.sendMessage("You are not in a voice channel.").queue();
        }
    }
}
