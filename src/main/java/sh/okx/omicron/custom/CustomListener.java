package sh.okx.omicron.custom;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sh.okx.omicron.Omicron;

public class CustomListener extends ListenerAdapter {
    private Omicron omicron;


    public CustomListener(Omicron omicron) {
        this.omicron = omicron;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        Member member = e.getMember();
        if(member == null || member.getUser() == null || member.getUser().isFake() || member.getUser().isBot()) {
            return;
        }

        omicron.getCustomManager()
                .getCommand(e.getGuild().getIdLong(), member, e.getMessage().getContentRaw())
                .thenAccept(createdCustomCommand -> {
            if(createdCustomCommand == null) {
                return;
            }
            e.getChannel().sendMessage(createdCustomCommand.getResponse()).queue();
        });
    }
}
