package sh.okx.omicron.feed;

import net.dv8tion.jda.core.entities.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.feed.rss.RssHandler;
import sh.okx.omicron.util.Data;

import java.net.MalformedURLException;
import java.net.URL;

public class FeedManager {
    private Omicron omicron;

    public FeedManager(Omicron omicron) {
        this.omicron = omicron;
    }

    public void addFeed(TextChannel channel, String content) {
        Data data = omicron.getData();

        JSONObject channelData = data.getChannelData(channel);
        JSONArray feeds = channelData.has("feeds") ? channelData.getJSONArray("feeds") : new JSONArray();
        feeds.put(content);
        channelData.put("feeds", feeds);
        data.setChannelData(channel, channelData);

        try {
            RssHandler handler = new RssHandler(new URL(content));
            handler.addListener(new FeedListener(channel));
            handler.start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
