package sh.okx.omicron.feed.rss;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import sh.okx.omicron.feed.FeedListener;

public abstract class AbstractRssListener implements FeedListener {
    public abstract void on(SyndFeed feed,  SyndEntry entry);
}
