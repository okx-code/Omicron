package sh.okx.omicron.feed;

import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.feed.reddit.RedditHandler;
import sh.okx.omicron.feed.reddit.RedditListener;
import sh.okx.omicron.feed.rss.RssHandler;
import sh.okx.omicron.feed.rss.RssListener;
import sh.okx.omicron.feed.youtube.YoutubeHandler;
import sh.okx.omicron.feed.youtube.YoutubeListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class FeedManager {
    private Omicron omicron;

    private Map<FeedHandler, FeedListener> loadedHandlers = new HashMap<>();

    public FeedManager(Omicron omicron) {
        this.omicron = omicron;

        new Thread(() -> {
            try {
                // load the driver
                Class.forName("com.mysql.jdbc.Driver");

                Connection connection = omicron.getConnection();

                Statement table = connection.createStatement();
                table.execute("CREATE TABLE IF NOT EXISTS feeds (prefix VARCHAR(255), " +
                        "type VARCHAR(20), " +
                        "channel BIGINT(20), " +
                        "content VARCHAR(255) );");

                table.close();

                Statement feeds = connection.createStatement();
                ResultSet rs = feeds.executeQuery("SELECT * FROM feeds;");

                while(rs.next()) {
                    TextChannel channel = omicron.getJDA().getTextChannelById(rs.getString("channel"));
                    if(channel == null) {
                        continue;
                    }
                    try {
                        loadFeed(rs.getString("prefix"), FeedType.valueOf(rs.getString("type")),
                                channel, rs.getString("content"));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }

                feeds.close();
                connection.close();

                System.out.println("Loaded feeds.");
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void removeFeed(String channel, String content) {
        new Thread(() -> {
            try {
                Connection connection = omicron.getConnection();

                PreparedStatement statement = connection.prepareStatement("DELETE FROM feeds " +
                        "WHERE channel=? AND content=?;");
                statement.setString(1, channel);
                statement.setString(2, content);

                statement.execute();

                statement.close();
                connection.close();

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
        }).start();
    }

    public boolean hasFeed(String channelId, String content) {
        try {
            Connection connection = omicron.getConnection();

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM feeds WHERE channel=? AND content=?;");
            statement.setString(1, channelId);
            statement.setString(2, content);

            ResultSet rs = statement.executeQuery();
            boolean yes = rs.next();

            statement.close();
            connection.close();

            return yes;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Adds a feed to the SQL database and then loads it
     * @see FeedManager#loadFeed(String, FeedType, TextChannel, String)
     * @param prefix
     * @param type
     * @param channel
     * @param content
     * @throws MalformedURLException
     */
    public void addFeed(String prefix, FeedType type, TextChannel channel, String content) throws MalformedURLException {
        new Thread(() -> {
            try {
                Connection connection = omicron.getConnection();

                PreparedStatement statement = connection.prepareStatement("REPLACE INTO feeds (prefix, type, channel, content) " +
                        "VALUES (?, ?, ?, ?);");
                statement.setString(1, prefix);
                statement.setString(2, type.name());
                statement.setLong(3, channel.getIdLong());
                statement.setString(4, content);

                statement.execute();

                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();

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
    public void loadFeed(String prefix, FeedType type, TextChannel channel, String content) throws MalformedURLException {
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
        handler.start();

        loadedHandlers.put(handler, listener);

        System.out.println("Registered feed " + prefix + " : " + type + " : " + content);
        System.out.println("Channel: " + channel.getId());
    }
}
