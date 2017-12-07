package sh.okx.omicron.feed.rss;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import net.dv8tion.jda.core.entities.MessageChannel;
import sh.okx.omicron.feed.FeedListener;

public abstract class AbstractRssListener extends FeedListener {
    public AbstractRssListener(String prefix, MessageChannel channel) {
        super(prefix, channel);
    }

    public abstract void on(SyndFeed feed, SyndEntry entry);
}
