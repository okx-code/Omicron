package sh.okx.omicron.feed;

import net.dv8tion.jda.core.entities.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.feed.rss.AbstractRssListener;
import sh.okx.omicron.feed.rss.RssHandler;
import sh.okx.omicron.feed.rss.RssListener;
import sh.okx.omicron.feed.youtube.AbstractYoutubeListener;
import sh.okx.omicron.feed.youtube.YoutubeHandler;
import sh.okx.omicron.feed.youtube.YoutubeListener;

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
                loadFeed(feed.getString("prefix"),
                        FeedType.valueOf(feed.getString("type")),
                        omicron.getJDA().getTextChannelById(feed.getString("channel")),
                        feed.getString("url"));
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
            feedJson.put("url", feed.getLocation());
            feedJson.put("channel", feed.getChannel());
            feedJson.put("prefix", feed.getPrefix());
            feedsJson.put(feedJson);
        }

        omicron.getData().set("feeds", feedsJson);
    }

    public void removeFeed(String channel, String content) {
        feeds.removeIf(feed -> {
            boolean remove = feed.getLocation().equalsIgnoreCase(content) &&
                    feed.getChannel().equals(channel);
            if(remove) {
                feed.getHandler().cancel();
            }
            return remove;
        });
    }

    public boolean hasFeed(String channel, String location) {
        for(Feed feed : feeds) {
            if(feed.getLocation().equalsIgnoreCase(location) && feed.getChannel().equals(channel)) {
                return true;
            }
        }

        return false;
    }

    public void loadFeed(String prefix, FeedType type, TextChannel channel, String content) throws MalformedURLException {
        prefix = prefix.replace("<everyone>", "@everyone");

        FeedHandler feedHandler;

        switch(type) {
            case RSS:
                RssHandler rssHandler = new RssHandler(new URL(content));
                AbstractRssListener rssListener = new RssListener(prefix, channel);
                feedHandler = rssHandler;
                rssHandler.addListener(rssListener);
                rssHandler.start();
                break;
            case YOUTUBE:
                YoutubeHandler youtubeHandler = new YoutubeHandler(content);
                AbstractYoutubeListener youtubeListener = new YoutubeListener(prefix, channel);
                feedHandler = youtubeHandler;
                youtubeHandler.addListener(youtubeListener);
                youtubeHandler.start();
                break;
            default:
                return;
        }

        feeds.add(new Feed(prefix, type, content, channel.getId(), feedHandler));
    }
}
