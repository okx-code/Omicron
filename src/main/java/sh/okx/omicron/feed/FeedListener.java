package sh.okx.omicron.feed;

import net.dv8tion.jda.core.entities.MessageChannel;

public abstract class FeedListener {
    protected String prefix;
    protected MessageChannel channel;

    public FeedListener(String prefix, MessageChannel channel) {
        this.prefix = prefix;
        this.channel = channel;
    }

    public void handlePrefix() {
        if(prefix != null && !prefix.isEmpty()) {
            channel.sendMessage(prefix).complete();
        }
    }
}
