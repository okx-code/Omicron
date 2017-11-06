package sh.okx.omicron.feed;

import net.dv8tion.jda.core.entities.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.feed.rss.AbstractRssListener;
import sh.okx.omicron.feed.rss.RssHandler;
import sh.okx.omicron.feed.rss.RssListener;
import sh.okx.omicron.util.Data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class FeedManager {
    private Omicron omicron;

    public FeedManager(Omicron omicron) {
        this.omicron = omicron;
        load();
    }

    private Set<Feed> feeds = new HashSet<>();

    public void load() {
        JSONArray feeds = omicron.getData().getJSONArray("feeds");
        for(int i = 0; i < feeds.length(); i++) {
            JSONObject feed = feeds.getJSONObject(i);

            try {
                loadFeed(FeedType.valueOf(feed.getString("type")),
                        omicron.getJDA().getTextChannelById(feed.getString("channel")),
                        new URL(feed.getString("url")));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    public void save() {
        JSONArray feedsJson = new JSONArray();
        for(Feed feed : feeds) {
            JSONObject feedJson = new JSONObject();
            feedJson.put("type", feed.getType().name());
            feedJson.put("url", feed.getUrl().toString());
            feedJson.put("channel", feed.getChannel());
        }

        omicron.getData().set("feeds", feedsJson);
    }

    public void addFeed(TextChannel channel, String content, FeedType type) {
        Data data = omicron.getData();

        JSONArray channelData = data.getJSONArray("feeds");

        JSONObject feed = new JSONObject();
        feed.put("type", type.name());
        feed.put("url", content);
        feed.put("channel", channel.getId());

        channelData.put(feed);
        data.set(channel.getId(), feed);

        try {
            loadFeed(type, channel, new URL(content));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
    }

    public void removeFeed(String content) {
        feeds.removeIf(feed -> feed.getUrl().toString().equalsIgnoreCase(content));
    }

    public boolean hasFeed(String url) {
        for(Feed feed : feeds) {
            if(feed.getUrl().toString().equalsIgnoreCase(url)) {
                return true;
            }
        }

        return false;
    }

    public void loadFeed(FeedType type, TextChannel channel, URL url) {
        FeedListener listener;

        switch(type) {
            case RSS:
                RssHandler handler = new RssHandler(url);
                AbstractRssListener rssListener = new RssListener(channel);
                listener = rssListener;
                handler.addListener(rssListener);
                handler.start();
                break;
            default:
                return;
        }

        feeds.add(new Feed(type, url, channel.getId(), listener));
    }
}
