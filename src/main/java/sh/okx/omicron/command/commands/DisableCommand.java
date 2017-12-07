package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;
import sh.okx.omicron.command.CommandManager;

public class DisableCommand extends Command {
    public DisableCommand(Omicron omicron) {
        super(omicron, "disable", Category.MISC,
                "This command allows you to disable a certain command in a guild." +
                        "Usage: **o/disable <command>**. You cannot disable this command or the enable command.");
    }

    @Override
    public void run(Guild guild, MessageChannel channel, Member member, Message message, String content) {
        if(content.isEmpty()) {
            channel.sendMessage("Usage: **o/disable <command>**").queue();
            return;
        }
        if(guild == null) {
            channel.sendMessage("This must be run in a guild!").queue();
            return;
        }
        if(member.hasPermission(Permission.MANAGE_SERVER)) {
            channel.sendMessage("You need manage server permission to run this command!").queue();
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

        String name = disableCommand.getName().toLowerCase();
        if(name.equals("disable") || name.equals("enable")) {
            channel.sendMessage("That command cannot be disabled!").queue();
            return;
        }

        long guildId = guild.getIdLong();
        if(commandManager.isDisabled(guildId, disableCommand)) {
            channel.sendMessage("That command is already disabled! Use **o/enable** to re-enable it.").queue();
            return;
        }

        commandManager.setDisabled(disableCommand, guildId, true);
        channel.sendMessage("Disabled command: " + disableCommand.getName()).queue();
    }
}
