package sh.okx.omicron.evaluate.commands;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

public class PythonCommand extends Command {
  public PythonCommand(Omicron omicron) {
    super(omicron, "python3", Category.EVAL,
        "Run Python 3 code. This does not run in a REPL, and as such you will need `print` statements.\n" +
            "This is completely isolated from the main bot.\n" +
            "Usage: **o/python3 <command(s)>**",
        "python", "py", "py3");
  }

  @Override
  public void run(Message message, String content) {
    MessageChannel channel = message.getChannel();
    if (content.isEmpty()) {
      channel.sendMessage("Usage: **o/python3 <command(s)>**").queue();
      return;
    }

    omicron.getEvaluateManager().commandLanguage(channel, content, "python3");
  }
}
