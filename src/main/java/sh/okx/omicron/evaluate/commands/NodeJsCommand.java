package sh.okx.omicron.evaluate.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

public class NodeJsCommand extends Command {
    public NodeJsCommand(Omicron omicron) {
        super(omicron, "nodejs", Category.EVAL,
                "Run NodeJS code. This does not run in a REPL, and as such you will need `console.log` statements.\n" +
                        "This is completely isolated from the main bot.\n" +
                        "Usage: **o/nodejs <command(s)>**",
                "node", "javascript");
    }

    @Override
    public void run(Guild guild, MessageChannel channel, Member member, Message message, String content) {
        omicron.getEvaluateManager().commandLanguage(channel, content, "javascript-node");
    }
}
