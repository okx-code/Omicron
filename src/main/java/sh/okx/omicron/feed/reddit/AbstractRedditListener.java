package sh.okx.omicron.feed.reddit;

import net.dean.jraw.fluent.SubredditReference;
import net.dean.jraw.models.Submission;
import net.dv8tion.jda.core.entities.MessageChannel;
import sh.okx.omicron.feed.FeedListener;

public abstract class AbstractRedditListener extends FeedListener {
  public AbstractRedditListener(String prefix, MessageChannel channel) {
    super(prefix, channel);
  }

  public abstract void on(SubredditReference subreddit, Submission submission);
}
