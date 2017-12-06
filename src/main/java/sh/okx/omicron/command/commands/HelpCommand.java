
package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;

import java.util.HashMap;
import java.util.Map;

public class HelpCommand extends Command {
    private Map<String, String> help = new HashMap<>();

    public HelpCommand(Omicron omicron) {
        super(omicron, "help");
        // Misc
        help.put("feed", "This command allows you to add a feed to a channel.\n" +
                "You can add a feed from an RSS URL eg **o/feed rss <url>**,\n" +
                "a YouTube user - using their channel ID, not their username - eg **o/feed youtube UD3sfEfisf6N2Kq_YGfI2z**,\n" +
                "or posts from subreddits such as **o/feed reddit gifs** for posts from /r/gifs.");
        help.put("trivia", "Trivia questions. Run without any arguments for a random category,\n" +
                "Use **o/trivia categories** to list categories, and use\n" +
                "**o/trivia <category>** for a question in a specific category.");
        help.put("role", "Give a default role to people when they join\n" +
                        "To set the role, use **o/role default <role id/name/mention>**.\n" +
                        "To check what the default role currently is, use **o/role get**.");
        help.put("think", "Have a think **o/think**.");
        help.put("custom", "Register custom commands for a guild.\n" +
                "Usage: **o/custom <who> | <command> [| <response>]");

        // Music
        help.put("play", "Play a song. This only supports YouTube videos currently.\n" +
                "The bot will automatically join whichever voice channel you are in, " +
                "or the first it has access to if you are not in a voice channel\n" +
                "Usage: **o/play <url / search videos>**.");
        help.put("loop", "Toggle looping. When enabled, this will repeat the next queued song forever.\n" +
                "Usage: **o/loop**.");
        help.put("join", "Join the channel of whoever sent the message.");
        help.put("remove", "Remove the song at the specified position in the queue.\n" +
                "You must either have manage channels permission or have added the song to the queue.\n" +
                "Usage: **o/remove <index>**");
        help.put("skip", "Skips the currently playing song. People with manage channels permission will instantly skip, " +
                "otherwise at least 50% of people in the bot's voice channel must vote to skip.\n" +
                "Usage: **o/skip**");
    }

    @Override
    public void run(Guild guild, TextChannel channel, Member member, Message message, String content) {
        String prefix = omicron.getCommandManager().getPrefix();
        content = content.replaceFirst(prefix, "");

        EmbedBuilder eb = new EmbedBuilder();
        if(help.containsKey(content)) {
            eb.setTitle(prefix + content);
            eb.setDescription(help.get(content));
        } else {
            eb.setTitle("Omicron");
            eb.setDescription(prefix + "feed <type=rss/youtube/reddit> <feed/user id/subreddit> <prefix>\n" +
                    prefix + "trivia <category>\n" +
                    prefix + "role <method=default/get> <role if using method 'default'>\n" +
                    prefix + "think");
            eb.addField("Music",
                    prefix + "play <youtube url/search>\n" +
                            prefix + "loop\n" +
                            prefix + "join\n" +
                            prefix + "queue\n" +
                            prefix + "remove <index>\n" +
                            prefix + "skip",
                    false);
        }
        eb.setFooter("Use " + prefix + name + " <command> to get help with a specific command,\n" +
            "eg " + prefix + name + " feed. **<>** is required, **[]** is optional", null);
        channel.sendMessage(eb.build()).queue();
    }
}
