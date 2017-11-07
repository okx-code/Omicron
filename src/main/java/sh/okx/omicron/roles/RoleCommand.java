package sh.okx.omicron.roles;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;

import java.util.List;

public class RoleCommand extends Command {
    public RoleCommand(Omicron omicron) {
        super(omicron, "role");
    }

    @Override
    public void run(Omicron omicron, Guild guild, TextChannel channel, Member member, Message message, String content) {
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

                Role role = null;

                // first, try to get a mention
                List<Role> mentionedRoles = message.getMentionedRoles();
                if (mentionedRoles.size() > 0) {
                    role = mentionedRoles.get(0);
                } else if (parts.length > 1) {
                    List<Role> rolesByName = guild.getRolesByName(parts[1], true);
                    if (rolesByName.size() > 0) {
                        role = rolesByName.get(0);
                    } else {
                        try {
                            role = guild.getRoleById(parts[1]);
                        } catch(Exception ex) {
                            channel.sendMessage("Invalid role").queue();
                            return;
                        }
                    }
                }

                if (role == null) {
                    omicron.getRoleManager().removeDefaultRole(guild.getId());
                    channel.sendMessage("Removed default role for this guild").queue();
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

                omicron.getRoleManager().setDefaultRole(guild.getId(), role.getId());
                channel.sendMessage("Set the default role to this guild to: " + role.getName()).queue();
                return;
            } else if (parts[0].equalsIgnoreCase("get")) {
                String defaultRoleId = omicron.getRoleManager().getDefaultRole(guild.getId());
                if(defaultRoleId != null) {
                    channel.sendMessage("The default role for this guild is: " +
                    guild.getRoleById(defaultRoleId).getName()).queue();
                } else {
                    channel.sendMessage("There is no default role set for this guild.").queue();
                }
                return;
            }
        }

        String fullName = omicron.getCommandManager().getPrefix() + name;
        channel.sendMessage("Set the role to give by default to everyone when they join this guild " +
                "(providing no role will remove the automatically given role): **" + fullName + " default <role>**.\n" +
                "Get the role automatically given to people joining this guild: **" + fullName + " get**.")
                .queue();
    }
}
