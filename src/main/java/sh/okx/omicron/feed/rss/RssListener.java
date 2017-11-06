package sh.okx.omicron.feed.rss;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndPerson;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.util.Util;

import java.util.List;

public class RssListener extends AbstractRssListener {
    private TextChannel channel;

    public RssListener(TextChannel channel) {
        this.channel = channel;
    }

    @Override
    public void on(SyndFeed feed, SyndEntry entry) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(entry.getTitle(), entry.getLink());
        List<SyndPerson> authors = entry.getAuthors();
        SyndPerson author = authors.get(0);

        eb.setAuthor(author.getName(), author.getUri());

        eb.setDescription(Util.limit(Util.stripHtml(entry.getDescription().getValue()), 100));

        eb.setFooter(feed.getTitle(), null);

        channel.sendMessage(eb.build()).queue();
    }
}
