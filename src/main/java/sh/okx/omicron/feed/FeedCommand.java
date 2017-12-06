
package sh.okx.omicron.feed;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

import java.net.MalformedURLException;

public class FeedCommand extends Command {
    public FeedCommand(Omicron omicron) {
        super(omicron, "feed", Category.MISC,
                "This command allows you to add a feed to a channel. " +
                "Users must have the manage messages permission to use this command.\n" +
                "Usage: **o/feed <youtube/reddit> <channel id/subreddit> <prefix>**\n" +
                "The prefix is a message that will be sent before the item from the feed.\n" +
                "For subreddits, use the name - such as 'videos'.\n" +
                "For YouTube channels, use the channel ID. This will be in the URL bar when on the channel, " +
                "or, for verified channels, you will have to look at the source code as described here: " +
                "https://stackoverflow.com/a/16326307/3524942");
    }

    @Override
    public void run(Guild guild, TextChannel channel, Member member, Message message, String content) {
        if(!member.hasPermission(Permission.MESSAGE_MANAGE)) {
            channel.sendMessage("You must have the manage messages permission to use this command.").queue();
            return;
        }

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
                omicron.getFeedManager().addFeed(parts.length < 3 ? "" : parts[2], type, channel, parts[1]);
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
