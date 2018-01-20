package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

public class InfoCommand extends Command {
    public InfoCommand(Omicron omicron) {
        super(omicron, "info", Category.MISC,
                "Show information about the bot", "information");
    }

    @Override
    public void run(Message message, String args) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Omicron");
        eb.addField("Links",
                "[Invite](https://omicron.okx.sh/invite)\n" +
                "[Discord](https://omicron.okx.sh/discord)", false);
        eb.addField("Code",
                "Library: [JDA](https://github.com/DV8FromTheWorld/JDA)\n" +
                "Language: Java\n" +
                "Source code: [GitHub](https://github.com/okx-code/Omicron)", false);
        eb.addField("Stats",
                "Guilds: **" + omicron.getJDA().getGuilds().size() + "**\n" +
                        "", false);

        message.getChannel().sendMessage(eb.build()).queue();
    }
}
