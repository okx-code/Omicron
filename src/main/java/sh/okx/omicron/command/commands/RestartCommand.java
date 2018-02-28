package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class RestartCommand extends Command {
  public RestartCommand(Omicron omicron) {
    super(omicron, "restart", Category.MISC,
        "Pull the latest update from the master branch on git and restart the bot. " +
            "This command can only be used by the bot developers.");
  }

  @Override
  public void run(Message message, String content) {
    if (!omicron.isDeveloper(message.getAuthor().getIdLong())) {
      return;
    }

    MessageChannel channel = message.getChannel();
    try {
      channel.sendMessage("Pulling from git and shutting down...").complete();
      Runtime.getRuntime().exec("./start.sh &");
      omicron.getJDA().shutdown();

      File shutdownChannel = new File("shutdown_channel.txt");
      Files.write(shutdownChannel.toPath(), channel.getId().getBytes(),
          StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      System.exit(0);
    } catch (IOException e) {
      e.printStackTrace();
      channel.sendMessage("An error occurred while attempting to restart.").queue();
    }
  }
}
