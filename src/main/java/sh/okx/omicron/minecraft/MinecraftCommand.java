package sh.okx.omicron.minecraft;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

public class MinecraftCommand extends Command {
  public MinecraftCommand(Omicron omicron) {
    super(omicron, "minecraft", Category.MISC,
        "Get your Minecraft username.");
  }

  @Override
  public void run(Message message, String content) {
    MessageChannel channel = message.getChannel();

    long id = message.getAuthor().getIdLong();
    omicron.getMinecraftManager().getUsername(id).thenAccept(username -> {
      if (username == null) {
        channel.sendMessage("You do not have a Minecraft account linked! Run **o/token** to see how to link one.").queue();
        return;
      }

      channel.sendMessage("Your minecraft username is: " + username).queue();
    });
  }
}
