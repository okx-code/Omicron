package sh.okx.omicron.feed.youtube;

import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchResultSnippet;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.util.Util;

import java.time.Instant;

public class YoutubeListener extends AbstractYoutubeListener {

    public YoutubeListener(String prefix, TextChannel channel) {
        super(prefix, channel);
    }

    @Override
    public void on(ResourceId id, SearchResultSnippet result) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setAuthor(result.getChannelTitle(), "https://www.youtube.com/channel/" + result.getChannelId());
        eb.setTitle(result.getTitle(), "https://youtube.com/watch?v=" + id.getVideoId());
        eb.setImage(result.getThumbnails().getMedium().getUrl());
        eb.setDescription(Util.limit(result.getDescription(), 100));
        eb.setTimestamp(Instant.ofEpochMilli(result.getPublishedAt().getValue()));

        channel.sendMessage(eb.build()).queue();
    }
}