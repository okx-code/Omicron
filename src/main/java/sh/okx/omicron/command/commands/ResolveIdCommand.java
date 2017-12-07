package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Command;

public class ResolveIdCommand extends Command {
    public ResolveIdCommand(Omicron omicron) {
        super(omicron, "resolveid", sh.okx.omicron.command.Category.MISC,
                "Find out what a specific ID is used for.\n" +
                        "Usage: **o/resolveid <id>**\n" +
                        "This can resolve roles, guilds, categories, emotes, and channels; but not messages. " +
                        "As well as that, the bot needs to know about the ID. It can't find a role in a guild it's not in.");
    }

    @Override
    public void run(Guild commandGuild, MessageChannel channel, Member member, Message message, String content) {
        if(content.isEmpty()) {
            channel.sendMessage("Usage **o/resolveid <id>**").queue();
            return;
        }

        String type;
        String name;

        JDA jda = omicron.getJDA();
        try {
            Role role = jda.getRoleById(content);
            Guild guild = jda.getGuildById(content);
            Category category = jda.getCategoryById(content);
            Emote emote = jda.getEmoteById(content);
            User user = jda.getUserById(content);
            TextChannel textChannel = jda.getTextChannelById(content);
            PrivateChannel privateChannel = jda.getPrivateChannelById(content);
            VoiceChannel voiceChannel = jda.getVoiceChannelById(content);

            if(role != null) {
                type = "Role";
                name = role.getName();
            } else if(guild != null) {
                type = "Guild";
                name = guild.getName();
            } else if(category != null) {
                type = "Category";
                name = category.getName();
            } else if(emote != null) {
                type = "Emote";
                name = emote.getName();
            } else if(user != null) {
                type = "User";
                name = user.getName();
            } else if(textChannel != null) {
                type = "Text Channel";
                name = textChannel.getName();
            } else if(privateChannel != null) {
                type = "Private Channel";
                name = privateChannel.getName();
            } else if(voiceChannel != null) {
                type = "Voice Channel";
                name = voiceChannel.getName();
            } else {
                channel.sendMessage("Cannot resolve ID.").queue();
                return;
            }

            channel.sendMessage(type + ": " + name).queue();
        } catch(NumberFormatException ex) {
            channel.sendMessage("ID must be a valid number.").queue();
        }
    }
}
