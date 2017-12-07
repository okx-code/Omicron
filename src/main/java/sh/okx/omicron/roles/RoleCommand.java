package sh.okx.omicron.roles;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;
import sh.okx.omicron.util.Util;

public class RoleCommand extends Command {
    public RoleCommand(Omicron omicron) {
        super(omicron, "role", Category.MISC,
                "Give a default role to people when they join. Users must have the manage server permission to use this command.\n" +
                        "To set the role, use **o/role default <role id/name/mention>**.\n" +
                        "To check what the default role currently is, use **o/role get**.");
    }

    @Override
    public void run(Guild guild, MessageChannel channel, Member member, Message message, String content) {
        if(!member.hasPermission(Permission.MANAGE_SERVER)) {
            channel.sendMessage("You require permission to manage the server in order to use this command.").queue();
            return;
        }

        String[] parts = content.split(" ", 2);
        if(parts.length > 0) {
            if (parts[0].equalsIgnoreCase("default")) {
                if(!guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                    channel.sendMessage("I require permission to manage roles.").queue();
                    return;
                }

                if(parts.length < 2) {
                    omicron.getRoleManager().removeDefaultRole(guild.getIdLong());
                    channel.sendMessage("Removed default role for this guild").queue();
                    return;
                }

                Role role = Util.getRole(message, parts[1]);
                if (role == null) {
                    channel.sendMessage("Invalid role").queue();
                    return;
                }

                boolean interact = false;
                for(Role selfRole : guild.getSelfMember().getRoles()) {
                    if(selfRole.canInteract(role)) {
                        interact = true;
                        break;
                    }
                }

                if(!interact) {
                    channel.sendMessage("At least one of my roles must be above the role \"" + role.getName() + "\"!").queue();
                    return;
                }

                omicron.getRoleManager().setDefaultRole(guild.getIdLong(), role.getIdLong());
                channel.sendMessage("Set the default role to this guild to: " + role.getName()).queue();
                return;
            } else if (parts[0].equalsIgnoreCase("get")) {
                if(!omicron.getRoleManager().hasDefaultRole(guild.getIdLong())) {
                    channel.sendMessage("There is no default role set for this guild.").queue();
                    return;
                }

                long defaultRoleId = omicron.getRoleManager().getDefaultRole(guild.getIdLong());
                if(defaultRoleId < 0) {
                    channel.sendMessage("An unexpected error occured when getting the default role. " +
                            "This has been reported to the developers.").queue();
                    return;
                }

                Role role = guild.getRoleById(defaultRoleId);
                if(role == null) {
                    channel.sendMessage("The currently default role for this guild is invalid. " +
                            "It has been deleted.").queue();
                    omicron.getRoleManager().removeDefaultRole(guild.getIdLong());
                } else {
                    channel.sendMessage("The default role for this guild is: " +
                            guild.getRoleById(defaultRoleId).getName()).queue();
                }
                return;
            }
        }

        String fullName = omicron.getCommandManager().getPrefix() + name;
        channel.sendMessage(
                "Set the role to give by default to everyone when they join this guild **" + fullName + " default <role>**.\n" +
                "Get the role automatically given to people joining this guild: **" + fullName + " get**.")
                .queue();
    }
}
