package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
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
    public void run(Message message, String content) {
        MessageChannel channel = message.getChannel();
        if(message.getChannelType() != ChannelType.TEXT) {
            channel.sendMessage("This command must be run in a guild.").queue();
            return;
        }

        Guild guild = message.getGuild();
        Member member = message.getMember();

        if(content.isEmpty()) {
            channel.sendMessage("Usage: **o/disable <command>**").queue();
            return;
        }
        if(guild == null) {
            channel.sendMessage("This must be run in a guild!").queue();
            return;
        }
        if(!member.hasPermission(Permission.MANAGE_SERVER)) {
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
        if(name.equals("disable") || name.equals("enable") || name.equals("help")) {
            channel.sendMessage("That command cannot be disabled!").queue();
            return;
        }

        final Command lambdaDisableCommand = disableCommand;
        long guildId = guild.getIdLong();

        commandManager.isDisabled(guildId, disableCommand)
                .thenAccept(disabled -> {
                    if(disabled) {
                        channel.sendMessage("That command is already disabled! " +
                                "Use **o/enable** to re-enable it.").queue();
                    } else {
                        commandManager.setDisabled(lambdaDisableCommand, guildId, true);
                        channel.sendMessage("Disabled command: " + lambdaDisableCommand.getName()).queue();
                    }
                });
    }
}
