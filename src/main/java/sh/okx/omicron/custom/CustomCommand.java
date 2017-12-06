package sh.okx.omicron.custom;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;
import sh.okx.omicron.util.Util;

public class CustomCommand extends Command {
    public CustomCommand(Omicron omicron) {
        super(omicron, "custom");
    }

    @Override
    public void run(Guild guild, TextChannel channel, Member member, Message message, String content) {
        if(!member.hasPermission(Permission.MESSAGE_MANAGE)) {
            channel.sendMessage("You need permission to manage messages in order to use this command.").queue();
            return;
        }

        String[] parts = content.split(" ? \\| ?", 3);
        if(parts.length < 2) {
            channel.sendMessage("Invalid usage! Usage **o/help " + name + "** for help").queue();
            return;
        }

        MemberPermission permission;

        if(parts.length == 3) {

            Role role;
            try {
                role = message.getMentionedRoles().get(0);
            } catch(IndexOutOfBoundsException ex) {
                role = Util.getRole(guild, parts[0]);
            }
            Member customMember;
            try {
                customMember = guild.getMember(message.getMentionedUsers().get(0));
            } catch(Exception ex) {
                customMember = Util.getMember(guild, parts[0]);
            }

            if(role == null && customMember == null) {
                channel.sendMessage("Invalid role / member").queue();
                return;
            }

            if(customMember != null) {
                permission = new MemberPermission(customMember.getUser());
            } else {
                permission = new MemberPermission(role);
            }
        } else {
            permission = new MemberPermission();
        }

        String response = parts[parts.length - 1];
        String command = parts[parts.length - 2];

        CreatedCustomCommand customCommand = new CreatedCustomCommand(guild.getId(), permission, command, response);

        if(omicron.getCustomManager().hasCommand(customCommand)) {
            omicron.getCustomManager().removeCommand(customCommand);

            channel.sendMessage("Removed command " + command + " when said by " + permission.getReadableAccess())
                    .queue();
        } else {
            omicron.getCustomManager().addCommand(new CreatedCustomCommand(guild.getId(), permission, command, response));

            channel.sendMessage("Added response '" + response + "' when " + command +
                    " is said by " + permission.getReadableAccess()).queue();
        }
    }
}
