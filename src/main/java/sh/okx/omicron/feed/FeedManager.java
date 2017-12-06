package sh.okx.omicron.feed;

import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.io.IOUtils;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.feed.reddit.RedditHandler;
import sh.okx.omicron.feed.reddit.RedditListener;
import sh.okx.omicron.feed.rss.RssHandler;
import sh.okx.omicron.feed.rss.RssListener;
import sh.okx.omicron.feed.youtube.YoutubeHandler;
import sh.okx.omicron.feed.youtube.YoutubeListener;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class FeedManager {
    private Omicron omicron;
    private String password;

    public FeedManager(Omicron omicron) {
        this.omicron = omicron;
        try {
            this.password = IOUtils.toString(new File("db_password.txt").toURI(), "UTF-8").trim();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // load the driver
            Class.forName("com.mysql.jdbc.Driver");

            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/omicron",
                    "root", password);

            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS feeds (prefix VARCHAR(255), " +
                    "type ENUM(RSS, YOUTUBE, REDDIT), " +
                    "channel INT(20), " +
                    "content VARCHAR(255) );");

            // close connections
            statement.close();
            connection.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        load();
    }

    private Set<Feed> feeds = new HashSet<>();

    public void load() {
        new Thread(() -> {
            try {
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/omicron",
                        "root", password);

                Statement statement = connection.createStatement();
                statement.execute("SELECT * FROM feeds;");

                ResultSet rs = statement.getResultSet();
                while (rs.next()) {
                    long channelId = rs.getLong("channel");
                    TextChannel channel = omicron.getJDA().getTextChannelById(channelId);
                    if (channel == null) {
                        // invalid entry
                        // TODO: Delete invalid entry
                        return;
                    }

                    loadFeed(rs.getString("prefix"),
                            FeedType.valueOf(rs.getString("type")),
                            channel,
                            rs.getString("content"));
                }

                // close connections
                rs.close();
                statement.close();
                connection.close();
            } catch (SQLException | MalformedURLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void removeFeed(String channel, String content) {
        feeds.removeIf(feed -> {
            boolean remove = feed.getLocation().equalsIgnoreCase(content) &&
                    feed.getChannel().equals(channel);
            if(remove) {
                new Thread(() -> {
                    try {
                        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/omicron",
                                "root", password);

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

    public void addFeed(String prefix, FeedType type, TextChannel channel, String content) throws MalformedURLException {
        new Thread(() -> {
            try {
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/omicron",
                        "root", password);

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

        System.out.println("Adding feed " + prefix + " : " + type + " : " + content);
        System.out.println("Channel: " + channel.getId());
        feeds.add(new Feed(prefix, type, content, channel.getId(), handler));
    }
}
