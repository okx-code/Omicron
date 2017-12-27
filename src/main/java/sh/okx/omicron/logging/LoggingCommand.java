package sh.okx.omicron.logging;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

import java.util.ArrayList;
import java.util.List;

public class LoggingCommand extends Command {
    public LoggingCommand(Omicron omicron) {
        super(omicron, "log", Category.MISC,
                "Log a certain event in this channel.\n" +
                        "Manage server permission is required to run this command.\n" +
                        "Usage: **o/log <id>** to toggle logging of that ID in this channel.\n" +
                        "Use **o/log list** to see the ID of each event.\n" +
                        "Use **o/log active** to see what this channel is currently logging.",
                "logging", "logger", "logs");
    }

    @Override
    public void run(Message message, String args) {
        if(message.getChannelType() != ChannelType.TEXT) {
            message.getChannel().sendMessage("This must be run in a guild!").queue();
            return;
        }

        TextChannel channel = message.getTextChannel();

        if(!message.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            channel.sendMessage("You must have permission to manage the server to run this command!").queue();
            return;
        }

        if(args.equalsIgnoreCase("list")) {
            channel.sendMessage("Logging event IDs:\n" +
                    "```\n" +
                    "0  Message edit                 1  Message delete\n" +
                    "2  Voice channel delete         3  Voice channel name change\n" +
                    "4  Voice channel create         5  Category delete\n" +
                    "6  Category name change         7  Category create\n" +
                    "8  Guild icon changed           9  Guild name changed\n" +
                    "10 Guild region change          11 Member join\n" +
                    "12 Member leave                 13 Role added to member\n" +
                    "14 Role removed from member     15 Nickname changed\n" +
                    "16 Member joined voice          17 Member changed voice channel\n" +
                    "18 Member left voice            19 Member guild muted\n" +
                    "20 Member guild deafened        21 Role created\n" +
                    "22 Role delete                  23 Role name changed\n" +
                    "24 Role permissions changed     25 Emote added\n" +
                    "26 Emote removed                27 Emote name changed\n" +
                    "```").queue();
            return;
        } else if(args.equalsIgnoreCase("active")) {
            omicron.getLoggingManager().getLogging(channel).thenAccept(l -> {
                List<String> ids = new ArrayList<>();
                for(int i = 0; i < 28; i++) {
                    if(omicron.getLoggingManager().nbit(l, i) > 0) {
                        ids.add(String.valueOf(i));
                    }
                }

                channel.sendMessage("Active logging IDs for this channel: " +
                        "`" + (ids.isEmpty() ? "None." : String.join(", ", ids)) + "`").queue();
            });
            return;
        } else if(!args.isEmpty()) {
            int id;
            try {
                id = Integer.parseInt(args);
            } catch (Exception ex) {
                channel.sendMessage("Invalid ID! Use **o/log list** to see valid IDs.").queue();
                return;
            }

            if (id < 0 || id > 28) {
                channel.sendMessage("That ID does not exist as an event.").queue();
                return;
            }

            omicron.getLoggingManager().getLogging(channel).thenAccept(l -> {
                if (omicron.getLoggingManager().nbit(l, id) > 0) {
                    l = omicron.getLoggingManager().unsetNbit(l, id);
                    channel.sendMessage("Disabled logging ID " + id).queue();
                } else {
                    l = omicron.getLoggingManager().setNbit(l, id);
                    channel.sendMessage("Enabled logging ID " + id).queue();
                }

                omicron.getLoggingManager().setLogging(message.getGuild(), channel, l);
            });
            return;
        }

        channel.sendMessage(description).queue();
    }
}
