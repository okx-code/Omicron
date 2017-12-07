package sh.okx.omicron.feed.reddit;

import net.dean.jraw.fluent.SubredditReference;
import net.dean.jraw.models.Submission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import org.apache.commons.text.StringEscapeUtils;
import sh.okx.omicron.util.Util;

public class RedditListener extends AbstractRedditListener {
    public RedditListener(String prefix, MessageChannel channel) {
        super(prefix, channel);
    }

    @Override
    public void on(SubredditReference subreddit, Submission submission) {
        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor("/u/" + submission.getAuthor(), "https://reddit.com/u/" + submission.getAuthor())
                .setDescription(StringEscapeUtils.unescapeHtml4(Util.limit(submission.getSelftext(), 100)))
                .setTimestamp(submission.getCreated().toInstant())
                .setTitle(submission.getTitle(), "http://reddit.com/" + submission.getPermalink())
                .setFooter(subreddit.info().getTitle(), "https://reddit.com/r/" + subreddit.info().getDisplayName())
                .setThumbnail(submission.getThumbnail());
        channel.sendMessage(eb.build()).queue();
    }
}
