package sh.okx.omicron.feed.rss;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

public abstract class RssListener {
    public abstract void on(SyndFeed feed,  SyndEntry entry);
}
