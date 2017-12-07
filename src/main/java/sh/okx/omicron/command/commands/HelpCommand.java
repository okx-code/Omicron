
package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

public class HelpCommand extends Command {
    public HelpCommand(Omicron omicron) {
        super(omicron, "help", Category.MISC, "Display information about each of the bot's commands.");
    }

    @Override
    public void run(Guild guild, MessageChannel channel, Member member, Message message, String content) {
        String prefix = omicron.getCommandManager().getPrefix();
        content = content.replaceFirst(prefix, "");

        EmbedBuilder eb = new EmbedBuilder();

        Command[] commands = omicron.getCommandManager().getCommands();

        DESCRIPTION:
        if(!content.isEmpty()) {
            for (Command command : commands) {
                if (command.getName().equalsIgnoreCase(content)) {
                    eb.setTitle(prefix + command.getName());
                    eb.setDescription(command.getDescription());
                    break DESCRIPTION;
                }
            }

            eb.setTitle("Invalid command");
            eb.setDescription("Cannot find command '" + content + "'.");
        } else {
            eb.setTitle("Omicron");

            for(Category category : Category.values()) {
                StringBuilder description = new StringBuilder();
                for(Command command : commands) {
                    if(command.getCategory() != category) {
                        continue;
                    }

                    description.append(prefix).append(command.getName()).append("\n");
                }

                eb.addField(category.toString(), description.toString().trim(), false);
            }
        }

        eb.setFooter("Use " + prefix + name + " <command> to get help with a specific command, eg " + prefix + name + " feed. \n" +
                "<> is required, [] is optional", null);
        channel.sendMessage(eb.build()).queue();
    }
}
