package sh.okx.omicron.feed;

import net.dv8tion.jda.core.entities.TextChannel;

public abstract class FeedListener {
    protected String prefix;
    protected TextChannel channel;

    public FeedListener(String prefix, TextChannel channel) {
        this.prefix = prefix;
        this.channel = channel;
    }

    public void handlePrefix() {
        if(prefix != null && !prefix.isEmpty()) {
            channel.sendMessage(prefix).complete();
        }
    }
}
