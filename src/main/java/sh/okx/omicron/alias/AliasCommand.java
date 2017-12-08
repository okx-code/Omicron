package sh.okx.omicron.alias;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

public class AliasCommand extends Command {
    public AliasCommand(Omicron omicron) {
        super(omicron, "alias", Category.EVAL,
                "Run a alias command when the specified message is sent by someone.\n" +
                        "To add an alias: **o/alias <message> | <alias>**\n" +
                        "To remove one: **o/alias <message>**\n" +
                        "In the alias, %i will be replaced with the user's ID " +
                        "and %m will be replaced with the user's name.");
    }

    @Override
    public void run(Guild guild, MessageChannel channel, Member member, Message message, String content) {
        if(guild == null) {
            channel.sendMessage("This must be run in a guild!").queue();
            return;
        }

        if(content.isEmpty()) {
            channel.sendMessage(description).queue();
            return;
        }

        if(!member.hasPermission(Permission.MESSAGE_MANAGE)) {
            channel.sendMessage("You need permission to manage messages in order to use this command!").queue();
            return;
        }

        String[] parts = content.split(" ?\\| ?", 2);

        if(parts.length < 2) {
            omicron.getAliasManager().removeAlias(parts[0]);
            channel.sendMessage("Removed alias '" + parts[0] + "'.").queue();
            return;
        }

        omicron.getAliasManager().setAlias(parts[0], parts[1]);
        channel.sendMessage("Added alias " + parts[0] + " for " + parts[1] + ".").queue();
    }
}
