package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;
import sh.okx.omicron.feed.FeedType;
import sh.okx.omicron.feed.rss.RssHandler;

public class FeedCommand extends Command {
    public FeedCommand(Omicron omicron) {
        super(omicron, "feed");
    }

    @Override
    public void run(Omicron omicron, Guild guild, TextChannel channel, Member member, Message message, String content) {
        if(!RssHandler.isValid(content)) {
            channel.sendMessage("Invalid feed URL.").queue();
            return;
        }

        if(omicron.getFeedManager().hasFeed(content)) {
            omicron.getFeedManager().addFeed(channel, content, FeedType.RSS);
            channel.sendMessage("Added feed from URL: " + content).queue();
        } else {
            omicron.getFeedManager().removeFeed(content);
            channel.sendMessage("Removed feed from URL: " + content).queue();
        }
    }
}
