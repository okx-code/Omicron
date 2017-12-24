package sh.okx.omicron.custom;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;
import sh.okx.omicron.util.Util;

public class CustomCommand extends Command {
    public CustomCommand(Omicron omicron) {
        super(omicron, "custom", Category.MISC,
                "Register custom commands for a guild. Users must have the manage messages permission to use this command.\n" +
                        "Usage: **o/custom <who> | <command> [| <response>]**\n" +
                        "**<who>** indicates which users are able to trigger this command. This can be a user, a role, or everyone. " +
                        "For users, you can user their name, their nickname, mention them or use their ID. " +
                        "For roles, you can mention it, use its name, or use its ID. " +
                        "For everyone, you can use 'everyone' or 'all'.\n" +
                        "The command is what users type to trigger this. This is case-insensitive.\n" +
                        "The response is what the bot sends when the command is triggered.");
    }

    @Override
    public void run(Message message, String args) {
        MessageChannel channel = message.getChannel();
        if(message.getChannelType() != ChannelType.TEXT) {
            channel.sendMessage("This command must be run in a guild.").queue();
            return;
        }

        Guild guild = message.getGuild();
        Member member = message.getMember();

        if(!member.hasPermission(Permission.MESSAGE_MANAGE)) {
            channel.sendMessage("You need permission to manage messages in order to use this command.").queue();
            return;
        }

        String[] parts = args.split(" ? \\| ?", 3);
        if(parts.length < 2) {
            channel.sendMessage("Invalid usage! Usage **o/help " + name + "** for help").queue();
            return;
        }

        MemberPermission permission;

        if(parts[0].equalsIgnoreCase("everyone") || parts[0].equalsIgnoreCase("all")) {
            permission = new MemberPermission();
        } else {
            Role role = Util.getRole(message, parts[0]);
            Member customMember = Util.getMember(message, parts[0]);

            if(role == null && customMember == null) {
                channel.sendMessage("Invalid role / member").queue();
                return;
            }

            permission = customMember == null ? new MemberPermission(role) : new MemberPermission(customMember.getUser());
        }
        String command = parts[1];

        omicron.getCustomManager().getCommand(guild.getIdLong(), member, command).thenAccept(existing -> {
            if(existing != null) {
                omicron.getCustomManager().removeCommand(guild.getIdLong(), permission, command);
                channel.sendMessage("Removed command " + existing.getCommand() + " when said by " + permission.getReadableAccess())
                        .queue();
                return;
            } else if(parts.length == 2) {
                channel.sendMessage("Either could not find command to remove or the response is missing.").queue();
                return;
            }

            String response = parts[2];
            omicron.getCustomManager().addCommand(new CreatedCustomCommand(guild.getIdLong(), permission, command, response));

            channel.sendMessage("Added response '" + response + "' when " + command +
                    " is said by " + permission.getReadableAccess()).queue();
        });
    }
}
