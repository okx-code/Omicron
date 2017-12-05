
package sh.okx.omicron.feed;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;

import java.net.MalformedURLException;

public class FeedCommand extends Command {
    public FeedCommand(Omicron omicron) {
        super(omicron, "feed");
    }

    @Override
    public void run(Omicron omicron, Guild guild, TextChannel channel, Member member, Message message, String content) {
        String[] parts = content.split(" ", 3);
        if(parts.length < 2) {
            channel.sendMessage("Usage: **" +
                    omicron.getCommandManager().getPrefix() + name +
                    "** <type=youtube/reddit> <feed/user id/subreddit>").queue();
            return;
        }
        FeedType type;
        try {
            type = FeedType.valueOf(parts[0].toUpperCase());
        } catch(IllegalArgumentException e) {
            channel.sendMessage("Invalid type. Valid types are: YouTube, and Reddit.").queue();
            return;
        }

        if(type == FeedType.RSS/*&& !RssHandler.isValid(content)*/) {
            channel.sendMessage("Invalid feed URL.").queue();
            return;
        }

        if(!omicron.getFeedManager().hasFeed(channel.getId(), parts[1])) {
            try {
                omicron.getFeedManager().loadFeed(parts.length < 3 ? "" : parts[2], type, channel, parts[1]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                channel.sendMessage("An error occured loading the feed.").queue();
                return;
            }
            channel.sendMessage("Added feed from: " + parts[1]).queue();
        } else {
            omicron.getFeedManager().removeFeed(channel.getId(), parts[1]);
            channel.sendMessage("Removed feed from: " + parts[1]).queue();
        }
    }
}
