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

public class FeedManager {
    private Omicron omicron;

    public FeedManager(Omicron omicron) {
        this.omicron = omicron;

        try {
            // load the driver
            Class.forName("com.mysql.jdbc.Driver");

            Connection connection = omicron.getConnection();

            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS feeds (prefix VARCHAR(255), " +
                    "type VARCHAR(20), " +
                    "channel BIGINT(20), " +
                    "content VARCHAR(255) );");

            // close connections
            statement.close();
            connection.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
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

                // close connections
                statement.close();
                connection.close();
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

            statement.execute();

            ResultSet rs = statement.getResultSet();
            boolean yes = rs.next();

            // close connections
            rs.close();
            statement.close();
            connection.close();

            return yes;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

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

                // close connections
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();

        loadFeed(prefix, type, channel, content);
    }

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

        System.out.println("Registered feed " + prefix + " : " + type + " : " + content);
        System.out.println("Channel: " + channel.getId());
    }
}
