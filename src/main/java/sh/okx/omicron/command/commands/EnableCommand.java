package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
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
    public void run(Message message, String content) {
        MessageChannel channel = message.getChannel();
        if(message.getChannelType() != ChannelType.TEXT) {
            channel.sendMessage("This command must be run in a guild.").queue();
            return;
        }

        Guild guild = message.getGuild();
        Member member = message.getMember();

        if(content.isEmpty()) {
            channel.sendMessage("Usage: **o/enable <command>**").queue();
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
        commandManager.isDisabled(guildId, disableCommand).thenAccept(b -> {
            if(!b) {
                channel.sendMessage("That command is not disabled!").queue();
                return;
            }

            commandManager.setDisabled(lambdaDisableCommand, guildId, false);
            channel.sendMessage("Enabled command: " + lambdaDisableCommand.getName()).queue();
        });
    }
}
