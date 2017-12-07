package sh.okx.omicron.feed.youtube;

import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchResultSnippet;
import net.dv8tion.jda.core.entities.MessageChannel;
import sh.okx.omicron.feed.FeedListener;

public abstract class AbstractYoutubeListener extends FeedListener {
    public AbstractYoutubeListener(String prefix, MessageChannel channel) {
        super(prefix, channel);
    }

    public abstract void on(ResourceId id, SearchResultSnippet result);
}
