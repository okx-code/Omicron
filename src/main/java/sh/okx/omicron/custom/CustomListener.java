package sh.okx.omicron.custom;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sh.okx.omicron.Omicron;

public class CustomListener extends ListenerAdapter {
    private Omicron omicron;

    public CustomListener(Omicron omicron) {
        this.omicron = omicron;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        if(e.getMember() == null || e.getMember().getUser() == null || e.getMember().getUser().isBot()) {
            return;
        }

        CreatedCustomCommand createdCustomCommand = omicron.getCustomManager()
                .getCommand(e.getMessage().getRawContent(), e.getGuild().getId(), e.getMember());
        if(createdCustomCommand == null) {
            return;
        }
        e.getChannel().sendMessage(createdCustomCommand.getResponse()).queue();
    }
}
