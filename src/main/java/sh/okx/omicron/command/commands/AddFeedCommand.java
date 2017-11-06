package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;
import sh.okx.omicron.feed.rss.RssHandler;

public class AddFeedCommand extends Command {
    public AddFeedCommand(Omicron omicron) {
        super(omicron, "addfeed");
    }

    @Override
    public void run(Omicron omicron, Guild guild, TextChannel channel, Member member, Message message, String content) {
        if(!RssHandler.isValid(content)) {
            channel.sendMessage("Invalid feed URL.").queue();
            return;
        }

        omicron.getFeedManager().addFeed(channel, content);
        channel.sendMessage("Added feed from URL: " + content).queue();
    }
}
