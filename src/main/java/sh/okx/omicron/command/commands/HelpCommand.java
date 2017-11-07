package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;

import java.util.Map;

public class HelpCommand extends Command {
    private Map<String, String> help = Map.of(
            "feed", "This command allows you to add a feed to a channel.\n" +
                    "You can add a feed from an RSS URL eg **o/feed rss <url>**,\n" +
                    "a YouTube user - using their channel ID, not their username - eg **o/feed youtube UD3sfEfisf6N2Kq_YGfI2z**,\n" +
                    "or posts from subreddits such as **o/feed reddit gifs** for posts from /r/gifs.",
            "trivia", "Trivia questions. Run without any arguments for a random category,\n" +
                    "Use **o/trivia categories** to list categories, and use\n" +
                    "**o/trivia <category>** for a question in a specific category.",
            "role", "Give a default role to people when they join\n" +
                    "To set the role, use **o/role default <role id/name/mention>**.\n" +
                    "To check what the default role currently is, use **o/role get**.");

    public HelpCommand(Omicron omicron) {
        super(omicron, "help");
    }

    @Override
    public void run(Omicron omicron, Guild guild, TextChannel channel, Member member, Message message, String content) {
        String prefix = omicron.getCommandManager().getPrefix();
        content = content.replaceFirst(prefix, "");

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(help.containsKey(content) ? "o/" + content : "Omicron")
                .setDescription(help.getOrDefault(content,
                        prefix + "feed <type=rss/youtube/reddit> <feed/user id/subreddit> <prefix>\n" +
                                prefix + "trivia <category>\n" +
                                prefix + "role <method=default/get> <role if using method 'default'>"))
                .setFooter("Use " + prefix + name + " <command> to get help with a specific command,\n" +
                        "eg " + prefix + name + " feed", null);
        channel.sendMessage(eb.build()).queue();
    }
}
