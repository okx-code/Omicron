package sh.okx.omicron.alias;

import net.dv8tion.jda.core.entities.impl.MessageImpl;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sh.okx.omicron.Omicron;

public class AliasListener extends ListenerAdapter {
    private Omicron omicron;

    public AliasListener(Omicron omicron) {
        this.omicron = omicron;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        String command = e.getMessage().getRawContent();
        if(command == null || command.isEmpty()) {
            return;
        }
        String alias = omicron.getAliasManager().getAlias(command);
        if(alias == null) {
            return;
        }

        MessageImpl message = (MessageImpl) e.getMessage();
        message.setContent(alias.replace("%i", message.getAuthor().getId()));
    }

}
