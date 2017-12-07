package sh.okx.omicron.evaluate.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

public class Python2Command extends Command {
    public Python2Command(Omicron omicron) {
        super(omicron, "python2", Category.EVAL,
                "Run Python 2 code. This does not run in a REPL, and as such you will need `print` statements.\n" +
                        "This is completely isolated from the main bot.\n" +
                        "Usage: **o/python2 <command(s)>**",
                "py2");
    }

    @Override
    public void run(Guild guild, MessageChannel channel, Member member, Message message, String content) {
        if(content.isEmpty()) {
            channel.sendMessage("Usage: **o/python2 <command(s)>**").queue();
            return;
        }

        omicron.getEvaluateManager().commandLanguage(channel, content, "python2");
    }
}
