package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;

public class ThinkCommand extends Command {
    public ThinkCommand(Omicron omicron) {
        super(omicron, "think");
    }

    @Override
    public void run(Omicron omicron, Guild guild, TextChannel channel, Member member, Message message, String content) {
        channel.sendMessage(
           "⠀⠀⠀⠀⠀⢀⣀⣀⣀\n" +
                "⠀⠀⠀⠰⡿⠿⠛⠛⠻⠿⣷\n" +
                "⠀⠀⠀⠀⠀⠀⣀⣄⡀⠀⠀⠀⠀⢀⣀⣀⣤⣄⣀⡀\n" +
                "⠀⠀⠀⠀⠀⢸⣿⣿⣷⠀⠀⠀⠀⠛⠛⣿⣿⣿⡛⠿⠷\n" +
                "⠀⠀⠀⠀⠀⠘⠿⠿⠋⠀⠀⠀⠀⠀⠀⣿⣿⣿⠇\n" +
                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠉⠁\n" +
                "⠀⠀⠀⠀⣿⣷⣄⠀⢶⣶⣷⣶⣶⣤⣀\n" +
                "⠀⠀⠀⠀⣿⣿⣿⠀⠀⠀⠀⠀⠈⠙⠻⠗\n" +
                "⠀⠀⠀⣰⣿⣿⣿⠀⠀⠀⠀⢀⣀⣠⣤⣴⣶⡄\n" +
                "⠀⣠⣾⣿⣿⣿⣥⣶⣶⣿⣿⣿⣿⣿⠿⠿⠛⠃\n" +
                "⢰⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡄\n" +
                "⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡁\n" +
                "⠈⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠁\n" +
                "⠀⠀⠛⢿⣿⣿⣿⣿⣿⣿⡿⠟\n" +
                "⠀⠀⠀⠀⠀⠉⠉⠉").queue();
    }
}
