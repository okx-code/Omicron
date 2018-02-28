package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

public class PrefixCommand extends Command {
  public PrefixCommand(Omicron omicron) {
    super(omicron, "prefix", Category.MANAGE,
        "Change the prefix for this guild.\n" +
            "The default prefix is `o/`");
  }

  @Override
  public void run(Message message, String args) {
    MessageChannel channel = message.getChannel();
    if (message.getChannelType() != ChannelType.TEXT) {
      channel.sendMessage("This command must be run in a guild!").queue();
      return;
    }

    if (!message.getMember().hasPermission(Permission.MANAGE_SERVER)) {
      channel.sendMessage("You must have permission to manage the server to use this command!").queue();
      return;
    }

    if (args.isEmpty()) {
      channel.sendMessage("You must specify a prefix.").queue();
      return;
    }

    omicron.getCommandManager().setPrefix(message.getGuild().getIdLong(), args);
    channel.sendMessage("Set prefix to `" + args.replaceAll("`", "\\`") + "`").queue();
  }
}
