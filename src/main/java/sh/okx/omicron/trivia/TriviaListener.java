package sh.okx.omicron.trivia;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sh.okx.omicron.Omicron;

public class TriviaListener extends ListenerAdapter {
    private Omicron omicron;

    public TriviaListener(Omicron omicron) {
        this.omicron = omicron;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent e) {
        if(e.getUser().getIdLong() == e.getJDA().getSelfUser().getIdLong()) {
            return;
        }

        String answer = omicron.getTriviaManager().getAnswer(e.getMessageId());
        if(answer == null) {
            return;
        }

        MessageChannel channel  = e.getChannel();
        char letter = (char) (answer.charAt(1) - 0xDDE6 + 0x0041);
        if(answer.equals(e.getReactionEmote().getName())) {
            channel.sendMessage("**" + e.getUser().getName() + "** got the correct answer (**" +
                    letter +"**)!").queue();
        } else {
            channel.sendMessage("**" + e.getUser().getName() + "** answered incorrectly." +
                    "The correct answer was **" + letter + "**.").queue();
        }

        omicron.getTriviaManager().removeAnswer(e.getMessageId());
    }
}
