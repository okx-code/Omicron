package sh.okx.omicron.feed.rss;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class RssHandler {
    private Date lastCheck = new Date();
    private URL feedUrl;
    private Set<RssListener> listeners = new HashSet<>();

    public void addListener(RssListener listener) {
        listeners.add(listener);
    }

    public RssHandler(URL feedUrl) {
        this.feedUrl = feedUrl;
    }

    public void start() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
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

                        listeners.forEach(listener -> listener.on(feed, entry));
                    }

                    lastCheck = entries.get(entries.size()-1).getPublishedDate();
                } catch (FeedException | IOException e) {
                    e.printStackTrace();
                    this.cancel();
                    return;
                }
            }
        }, 0, 5*1000);

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
