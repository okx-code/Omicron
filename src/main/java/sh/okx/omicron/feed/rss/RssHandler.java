package sh.okx.omicron.feed.rss;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import sh.okx.omicron.feed.FeedHandler;
import sh.okx.omicron.feed.FeedListener;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class RssHandler implements FeedHandler {
    private Date lastCheck = new Date();
    private boolean cancelled = false;
    private Set<AbstractRssListener> listeners = new HashSet<>();
    private TimerTask task;

    public void addListener(FeedListener listener) {
        listeners.add((AbstractRssListener) listener);
    }

    public RssHandler(URL feedUrl) {
        this.task = new TimerTask() {
            @Override
            public void run() {
                try {
                    SyndFeed feed = new SyndFeedInput().build(new XmlReader(feedUrl));

                    List<SyndEntry> entries = feed.getEntries();
                    Collections.reverse(entries);

                    for(SyndEntry entry : entries) {
                        if(entry.getPublishedDate().compareTo(lastCheck) <= 0) {
                            continue;
                        }

                        listeners.forEach(listener -> {
                            listener.handlePrefix();
                            listener.on(feed, entry);
                        });
                    }

                    lastCheck = entries.get(entries.size()-1).getPublishedDate();
                } catch (FeedException | IOException e) {
                    e.printStackTrace();
                    this.cancel();
                    cancelled = true;
                }
            }
        };
    }

    @Override
    public void cancel() {
        cancelled = task.cancel();
    }

    @Override
    public void start() {
        new Timer().scheduleAtFixedRate(task, 0, 5*1000);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public static boolean isValid(String feedUrlString) {
        try {
            new SyndFeedInput().build(new XmlReader(new URL(feedUrlString)));
            return true;
        } catch(Exception ex) {
            return false;
        }
    }
}
