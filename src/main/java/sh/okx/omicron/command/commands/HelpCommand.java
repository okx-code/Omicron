
package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;
import sh.okx.sql.api.Connection;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class HelpCommand extends Command {
    public HelpCommand(Omicron omicron) {
        super(omicron, "help", Category.MISC, "Display information about each of the bot's commands.");
    }

    @Override
    public void run(Message message, String content) {
        Guild guild = message.getGuild();
        MessageChannel channel = message.getChannel();

        String prefix = omicron.getCommandManager().getPrefix();
        content = content.replaceFirst(prefix, "");

        EmbedBuilder eb = new EmbedBuilder();

        Command[] commands = omicron.getCommandManager().getCommands();

        if(!content.isEmpty()) {
            eb.setFooter( "<> is required, [] is optional.", null);

            for (Command command : commands) {
                boolean isAlias = false;
                for(String alias : command.getAliases()) {
                    if(alias.equalsIgnoreCase(content)) {
                        isAlias = true;
                        break;
                    }
                }

                if (isAlias || command.getName().equalsIgnoreCase(content)) {
                    eb.setTitle(prefix + command.getName());
                    eb.setDescription(command.getDescription());

                    String[] aliases = command.getAliases();
                    if(aliases.length > 0) {
                        eb.addField("Aliases", String.join("\t", aliases), false);
                    }

                    channel.sendMessage(eb.build()).queue();
                    return;
                }
            }

            eb.setTitle("Invalid command");
            eb.setDescription("Cannot find command '" + content + "'.");

            channel.sendMessage(eb.build()).queue();
            return;
        }

        CompletableFuture.runAsync(() -> {
            eb.setTitle("Omicron");
            eb.setFooter("Use " + prefix + name + " <command> to get help with a specific command, eg " +
                    prefix + name + " feed.", null);

            try(Connection connection = omicron.getConnection()) {
                Set<Command> disabledCommands = new HashSet<>();
                for (Category category : Category.values()) {
                    StringBuilder description = new StringBuilder();
                    for (Command command : commands) {
                        if(command.getName().equals("restart") && !omicron.isDeveloper(message.getAuthor().getIdLong())) {
                            continue;
                        }

                        if (guild != null && omicron.getCommandManager().isDisabled(connection,
                                guild.getIdLong(), command).join()) {
                            disabledCommands.add(command);
                            continue;
                        }

                        if (command.getCategory() != category) {
                            continue;
                        }

                        description.append(prefix).append(command.getName()).append("\t");
                    }

                    eb.addField(category.toString(), description.toString().trim(), false);
                }

                if (guild != null && message.getMember().hasPermission(Permission.MANAGE_SERVER) && !disabledCommands.isEmpty()) {
                    StringBuilder disabled = new StringBuilder();
                    for (Command disabledCommand : disabledCommands) {
                        disabled.append(prefix).append(disabledCommand.getName()).append("\t");
                    }
                    eb.addField("Disabled", disabled.toString().trim(), false);
                }

                channel.sendMessage(eb.build()).queue();
            }
        });
    }
}
