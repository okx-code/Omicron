package sh.okx.omicron.roles;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;
import sh.okx.omicron.util.Util;

import java.util.HashSet;
import java.util.Set;

public class RoleCommand extends Command {
    public RoleCommand(Omicron omicron) {
        super(omicron, "role", Category.MISC,
            "This command can be used for two things: Users being able to get/remove a role at will, " +
            "and giving users a role when they join." +
            "To add yourself to a role, use **o/role get <role>**\n" +
            "To list roles you can get, use **o/role list**\n" +
            "To add or remove roles anyone can get, use **o/role toggle <role>**\n" +
            "To set the role to be given to users when they join, use **o/role default <role id/name/mention>**.\n" +
            "To check what the default role currently is, use **o/role default get**.\n");
    }

    @Override
    public void run(Message message, String args) {
        MessageChannel channel = message.getChannel();
        if(message.getChannelType() != ChannelType.TEXT) {
            channel.sendMessage("This command must be run in a guild.").queue();
            return;
        }

        Member member = message.getMember();
        if(!member.hasPermission(Permission.MANAGE_SERVER)) {
            channel.sendMessage("You require permission to manage the server in order to use this command.").queue();
            return;
        }

        Guild guild = message.getGuild();
        String[] parts = args.split(" ", 2);
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

                if(parts[1].equalsIgnoreCase("get")) {
                    omicron.getRoleManager().hasDefaultRole(guild.getIdLong()).thenAccept(b -> {
                        if(!b) {
                            channel.sendMessage("There is no default role set for this guild.").queue();
                            return;
                        }

                        omicron.getRoleManager().getDefaultRole(guild.getIdLong()).thenAccept(defaultRoleId -> {
                            if (defaultRoleId < 1) {
                                channel.sendMessage("An unexpected error occured when getting the default role. " +
                                        "This has been reported to the developers.").queue();
                                return;
                            }

                            Role role = guild.getRoleById(defaultRoleId);
                            if (role == null) {
                                channel.sendMessage("The currently default role for this guild is invalid. " +
                                        "It has been deleted.").queue();
                                omicron.getRoleManager().removeDefaultRole(guild.getIdLong());
                            } else {
                                channel.sendMessage("The default role for this guild is: " +
                                        guild.getRoleById(defaultRoleId).getName()).queue();
                            }
                        });
                    });
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
            } else if(parts[0].equalsIgnoreCase("list")) {
                omicron.getRoleManager().getFreeRoles(guild.getIdLong()).thenAccept(freeRoles -> {
                    Set<String> roles = new HashSet<>();
                    for(long roleId : freeRoles) {
                        roles.add(guild.getRoleById(roleId).getName());
                    }
                    EmbedBuilder eb = new EmbedBuilder();

                    eb.setTitle("Obtainable Roles");
                    eb.setDescription(roles.isEmpty() ? "None." : String.join("\t", roles));

                    channel.sendMessage(eb.build()).queue();
                });
                return;
            } else if(parts[0].equalsIgnoreCase("toggle") && parts.length > 1) {
                Role role = Util.getRole(message, parts[1]);
                if(role == null) {
                    channel.sendMessage("Cannot find role.").queue();
                    return;
                }

                if(!member.hasPermission(Permission.MANAGE_ROLES)) {
                    channel.sendMessage("You need permission to manage roles to use this command!").queue();
                    return;
                }

                omicron.getRoleManager().getFreeRoles(guild.getIdLong()).thenAccept(freeRoles -> {
                    if(freeRoles.contains(role.getIdLong())) {
                        channel.sendMessage("Users can no longer obtain the role **" + role.getName() + "**.").queue();
                        omicron.getRoleManager().removeFreeRole(guild.getIdLong(), role.getIdLong());
                    } else {
                        if(!guild.getSelfMember().canInteract(role)) {
                            channel.sendMessage("My role is below the role **" + role.getName() + "**, " +
                                    "so I cannot give users that role. " +
                                    "Please drag my role above it.").queue();
                            return;
                        }

                        channel.sendMessage("Added role. Anyone can type **o/role get " + role.getName() + "** to get it!").queue();
                        omicron.getRoleManager().addFreeRole(guild.getIdLong(), role.getIdLong());
                    }
                });
                return;
            } else if(parts[0].equalsIgnoreCase("get") && parts.length > 1) {
                Role role = Util.getRole(message, parts[1]);
                if(role == null) {
                    channel.sendMessage("Cannot find role.").queue();
                    return;
                }

                omicron.getRoleManager().getFreeRoles(guild.getIdLong()).thenAccept(freeRoles -> {
                    if(!freeRoles.contains(role.getIdLong())) {
                        channel.sendMessage("That role is not obtainable.").queue();
                        return;
                    }

                    if(member.getRoles().contains(role)) {
                        guild.getController().removeRolesFromMember(member, role).reason("Free role from o/role").queue();
                        channel.sendMessage("You have removed the role: **" + role.getName() + "**").queue();
                    } else {
                        guild.getController().addRolesToMember(member, role).reason("Free role from o/role").queue();
                        channel.sendMessage("You have received the role: **" + role.getName() + "**").queue();
                    }
                });
                return;
            }
        }

        channel.sendMessage(this.getDescription()).queue();
    }
}
