package sh.okx.omicron.util;

import net.dv8tion.jda.core.entities.*;

import java.util.List;

public class Util {
    private Util() {}

    public static String limit(String string, int length) {
        string = string.trim();
        String substring = string.substring(0, Math.min(string.length(), length - 3));
        if(substring.length() < string.length()) {
            return substring.trim() + "...";
        }
        return string;
    }

    public static String stripHtml(String string) {
        return string
                .replaceAll("</?b>", "**")
                .replaceAll("</?i>", "*")
                .replaceAll("</?[a-z]+>", "");
    }

    public static Role getRole(Guild guild, String message) {
        List<Role> rolesByName = guild.getRolesByName(message, true);
        if (rolesByName.size() > 0) {
            return rolesByName.get(0);
        } else {
            try {
                return guild.getRoleById(message);
            } catch(Exception ex) {
                return null;
            }
        }
    }

    public static Role getRole(Message message) {
        Role role = null;

        // first, try to get a mention
        List<Role> mentionedRoles = message.getMentionedRoles();
        if (mentionedRoles.size() > 0) {
            role = mentionedRoles.get(0);
        } else if (!message.getRawContent().isEmpty()) {
            return getRole(message.getGuild(), message.getRawContent());
        }

        return role;
    }

    public static Member getMember(Guild guild, String message) {
        List<Member> membersByName = guild.getMembersByName(message, true);
        if (membersByName.size() > 0) {
            return membersByName.get(0);
        } else {
            try {
                Member memberById = guild.getMemberById(message);
                if(memberById == null) {
                    List<Member> members = guild.getMembersByNickname(message, true);
                    if(members.size() > 0) {
                        return members.get(0);
                    }
                } else {
                    return memberById;
                }
            } catch(Exception ex) {
                return null;
            }
        }

        return null;
    }

    public static Member getMember(Message message) {
        // first, try to get a mention
        List<User> mentionedUsers = message.getMentionedUsers();
        if (mentionedUsers.size() > 0) {
            return message.getGuild().getMember(mentionedUsers.get(0));
        } else if (!message.getRawContent().isEmpty()) {
            return getMember(message.getGuild(), message.getRawContent());
        }
        return null;
    }
}
