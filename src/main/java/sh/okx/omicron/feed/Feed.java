package sh.okx.omicron.feed;

import java.net.URL;

public class Feed {
    private FeedType type;
    private URL url;
    private String channel;
    private FeedListener listener;

    public Feed(FeedType type, URL url, String channel, FeedListener listener) {
        this.type = type;
        this.url = url;
        this.channel = channel;
        this.listener = listener;
    }


    public FeedType getType() {
        return type;
    }

    public URL getUrl() {
        return url;
    }

    public String getChannel() {
        return channel;
    }

    public FeedListener getListener() {
        return listener;
    }
}
