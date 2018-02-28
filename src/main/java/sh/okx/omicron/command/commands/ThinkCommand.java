package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.entities.Message;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

public class ThinkCommand extends Command {
  public ThinkCommand(Omicron omicron) {
    super(omicron, "think", Category.MISC, "Have a think.");
  }

  @Override
  public void run(Message message, String content) {
    message.getChannel().sendMessage(
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
