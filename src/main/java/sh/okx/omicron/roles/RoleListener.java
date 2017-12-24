package sh.okx.omicron.roles;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sh.okx.omicron.Omicron;

public class    RoleListener extends ListenerAdapter {
    private Omicron omicron;

    public RoleListener(Omicron omicron) {
        this.omicron = omicron;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent e) {
        Member member = e.getMember();
        if(member == null) {
            return;
        }

        Guild guild = e.getGuild();

        omicron.getRoleManager().hasDefaultRole(guild.getIdLong()).thenAccept(b -> {
            if(!b) {
                return;
            }

            omicron.getRoleManager().getDefaultRole(guild.getIdLong()).thenAccept(defaultRole -> {
                if(defaultRole < 0) {
                    return;
                }

                Role role = omicron.getJDA().getRoleById(defaultRole);
                guild.getController().addSingleRoleToMember(member, role).queue();
            });
        });
    }
}
