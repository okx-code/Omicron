package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;
import sh.okx.omicron.command.CommandManager;

public class EnableCommand extends Command {
    public EnableCommand(Omicron omicron) {
        super(omicron, "enable", Category.MISC,
                "This command allows you to enable a disabled command in a guild." +
                        "Usage: **o/enable <command>**.");
    }

    @Override
    public void run(Guild guild, MessageChannel channel, Member member, Message message, String content) {
        if(content.isEmpty()) {
            channel.sendMessage("Usage: **o/enable <command>**").queue();
            return;
        }
        if(guild == null) {
            channel.sendMessage("This must be run in a guild!").queue();
            return;
        }

        CommandManager commandManager = omicron.getCommandManager();

        String commandName = content.replace(commandManager.getPrefix(), "");
        Command disableCommand = null;
        for(Command command : commandManager.getCommands()) {
            if (command.getName().equalsIgnoreCase(commandName)) {
                disableCommand = command;
                break;
            }
        }

        if(disableCommand == null) {
            channel.sendMessage("Cannot find command: " + commandName + ".").queue();
            return;
        }

        long guildId = guild.getIdLong();
        if(!commandManager.isDisabled(guildId, disableCommand)) {
            channel.sendMessage("That command is not enabled!").queue();
            return;
        }

        commandManager.setDisabled(disableCommand, guildId, false);
        channel.sendMessage("Enabled command: " + disableCommand.getName()).queue();
    }
}
