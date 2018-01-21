package sh.okx.omicron.feed;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.feed.reddit.RedditHandler;
import sh.okx.omicron.feed.reddit.RedditListener;
import sh.okx.omicron.feed.rss.RssHandler;
import sh.okx.omicron.feed.rss.RssListener;
import sh.okx.omicron.feed.youtube.YoutubeHandler;
import sh.okx.omicron.feed.youtube.YoutubeListener;
import sh.okx.sql.api.Connection;
import sh.okx.sql.api.database.ExecuteTable;
import sh.okx.sql.api.query.QueryResults;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FeedManager {
    private Omicron omicron;

    private Map<FeedHandler, FeedListener> loadedHandlers = new HashMap<>();

    public FeedManager(Omicron omicron) {
        this.omicron = omicron;

        omicron.runConnection(connection -> {
            ExecuteTable feeds = connection.table("feeds");
            feeds.create().ifNotExists()
                    .column("prefix VARCHAR(255)")
                    .column("type VARCHAR(20)")
                    .column("channel BIGINT(20)")
                    .column("content VARCHAR(255)")
                    .executeAsync()
                    .thenAccept(i -> feeds.select().executeAsync()
                            .thenAccept(qr -> loadFeeds(qr.getResultSet())));
        });
    }

    private void loadFeeds(ResultSet rs) {
        try {
            while (rs.next()) {
                TextChannel channel = omicron.getJDA().getTextChannelById(rs.getString("channel"));
                if (channel == null) {
                    continue;
                }
                try {
                    loadFeed(rs.getString("prefix"), FeedType.valueOf(rs.getString("type")),
                            channel, rs.getString("content"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }

            omicron.getLogger().info("Loaded {} feeds.", loadedHandlers.size());
        } catch(SQLException ex) {
            omicron.getLogger().error("Could not load feeds", ex);
        }
    }

    public void removeFeed(String channel, String content) {
        CompletableFuture.runAsync(() -> omicron.runConnection(connection -> {
            try {
                PreparedStatement statement = connection.getUnderlying().prepareStatement(
                        "DELETE FROM feeds WHERE channel=? AND content=?;");
                statement.setString(1, channel);
                statement.setString(2, content);

                statement.execute();

                loadedHandlers.entrySet().removeIf(next -> {
                    if(next.getKey().getContent().equalsIgnoreCase(content) &&
                            next.getValue().channel.getId().equals(channel)) {
                        next.getKey().cancel();
                        return next.getKey().isCancelled();
                    }
                    return false;
                });
            } catch(SQLException ex) {
                ex.printStackTrace();
            }
        }));
    }

    public CompletableFuture<Boolean> hasFeed(String channelId, String content) {
        return omicron.runConnectionAsync(connection -> connection.table("feeds").select()
               .where()
               .prepareEquals("channel", channelId)
               .and()
               .prepareEquals("content", content)
               .then().executeAsync()
               .thenApply(QueryResults::next));
    }

    /**
     * Adds a feed to the SQL database and then loads it
     * @see FeedManager#loadFeed(String, FeedType, MessageChannel, String)
     * @throws MalformedURLException
     */
    public void addFeed(String prefix, FeedType type, MessageChannel channel, String content) throws MalformedURLException {
        CompletableFuture.runAsync(() -> {
            try(Connection connection = omicron.getConnection();
                 PreparedStatement statement = connection.getUnderlying().prepareStatement(
                         "REPLACE INTO feeds (prefix, type, channel, content) VALUES (?, ?, ?, ?);")) {

                statement.setString(1, prefix);
                statement.setString(2, type.name());
                statement.setLong(3, channel.getIdLong());
                statement.setString(4, content);

                statement.execute();
            } catch(SQLException e) {
                e.printStackTrace();
            }
        });

        loadFeed(prefix, type, channel, content);
    }

    /**
     * Loads a feed to send feed updates to a channel
     * @param prefix The message to send before the feed information
     * @param type The type of feed
     * @param channel Which channel this feed is registered to
     * @param content Data the feed handler may use
     * @throws MalformedURLException
     */
    public void loadFeed(String prefix, FeedType type, MessageChannel channel, String content) throws MalformedURLException {
        prefix = prefix.replace("<everyone>", "@everyone");

        FeedHandler handler;
        FeedListener listener;
        switch(type) {
            case RSS:
                handler = new RssHandler(new URL(content));
                listener = new RssListener(prefix, channel);
                break;
            case YOUTUBE:
                handler = new YoutubeHandler(content);
                listener = new YoutubeListener(prefix, channel);
                break;
            case REDDIT:
                handler = new RedditHandler(content);
                listener = new RedditListener(prefix, channel);
                break;
            default:
                return;
        }

        handler.addListener(listener);
        listener.handlePrefix();
        handler.start();

        loadedHandlers.put(handler, listener);

        System.out.println("Registered feed " + prefix + " : " + type + " : " + content);
        System.out.println("Channel: " + channel.getId());
    }
}
