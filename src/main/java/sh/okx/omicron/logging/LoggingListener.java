package sh.okx.omicron.logging;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.channel.category.CategoryCreateEvent;
import net.dv8tion.jda.core.events.channel.category.CategoryDeleteEvent;
import net.dv8tion.jda.core.events.channel.category.update.CategoryUpdateNameEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.voice.update.VoiceChannelUpdateNameEvent;
import net.dv8tion.jda.core.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.core.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.core.events.emote.update.EmoteUpdateNameEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateIconEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateRegionEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceGuildDeafenEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.events.role.RoleCreateEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.core.events.role.update.RoleUpdatePermissionsEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import sh.okx.omicron.Omicron;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LoggingListener extends ListenerAdapter {
  private final Cache<Long, CachedMessage> messages =
      CacheBuilder.newBuilder()
          .expireAfterAccess(5, TimeUnit.MINUTES)
          .maximumSize(1000)
          .build();

  private Omicron omicron;

  public LoggingListener(Omicron omicron) {
    this.omicron = omicron;
  }

  @Override
  public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
    if (e.isWebhookMessage() || e.getAuthor().isBot()) {
      return;
    }

    Message message = e.getMessage();
    messages.put(message.getIdLong(), new CachedMessage(message));
  }

  @Override
  public void onGuildMessageUpdate(GuildMessageUpdateEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getChannel(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Message edited");

          eb.setAuthor(e.getAuthor().getName() + "#" + e.getAuthor().getDiscriminator(),
              null, e.getAuthor().getAvatarUrl());

          Message message = e.getMessage();
          CachedMessage cachedMessage = messages.getIfPresent(message.getIdLong());
          if(cachedMessage == null) {
            return;
          }

          eb.addField("Old Message", cachedMessage.getContent(), false);
          cachedMessage.setContent(message.getContentRaw());

          eb.addField("New Message", message.getContentRaw(), false);

          eb.addField("Edited at", message.getEditedTime()
              .format(DateTimeFormatter.RFC_1123_DATE_TIME), false);

          eb.addField("Channel", e.getChannel().getAsMention(), false);

          eb.setTimestamp(Instant.now());
          eb.setFooter("ID: " + message.getIdLong(), null);

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onGuildMessageDelete(GuildMessageDeleteEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getChannel(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Message deleted");

          CachedMessage cachedMessage = messages.getIfPresent(e.getMessageIdLong());
          if(cachedMessage == null) {
            return;
          }

          User author = cachedMessage.getAuthor();
          eb.setAuthor(author.getName() + "#" + author.getDiscriminator(),
              null, author.getAvatarUrl());
          eb.addField("Message", cachedMessage.getContent(), false);
          eb.addField("Created", cachedMessage.getTime()
              .format(DateTimeFormatter.RFC_1123_DATE_TIME), false);

          eb.addField("Channel", e.getChannel().getAsMention(), false);
          eb.setFooter("ID: " + e.getMessageIdLong(), null);
          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onVoiceChannelDelete(VoiceChannelDeleteEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getChannel(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Voice channel deleted");

          eb.addField("Name", e.getChannel().getName(), false);
          eb.setFooter("ID: " + e.getChannel().getIdLong(), null);

          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onVoiceChannelUpdateName(VoiceChannelUpdateNameEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getChannel(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Voice channel name changed");

          eb.addField("New Name", e.getChannel().getName(), true);
          eb.addField("Old Name", e.getOldName(), true);
          eb.setFooter("ID: " + e.getChannel().getIdLong(), null);
          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onVoiceChannelCreate(VoiceChannelCreateEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getChannel(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Voice channel created");
          eb.addField("Name", e.getChannel().getName(), true);
          eb.setFooter("ID: " + e.getChannel().getIdLong(), null);
          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onCategoryDelete(CategoryDeleteEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getCategory(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Category deleted");
          eb.addField("Name", e.getCategory().getName(), false);
          if (e.getCategory().getChannels().size() > 0) {
            eb.addField("Channels", String.join(", ", e.getCategory().getChannels()
                .stream().map(Channel::getName).collect(Collectors.toList())), false);
          }
          eb.setFooter("ID: " + e.getIdLong(), null);
          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onCategoryUpdateName(CategoryUpdateNameEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getCategory(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Category name changed");
          eb.addField("New Name", e.getCategory().getName(), true);
          eb.addField("Old Name", e.getOldName(), true);
          eb.setFooter("ID: " + e.getIdLong(), null);
          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onCategoryCreate(CategoryCreateEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getCategory(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Category created");
          eb.addField("Name", e.getCategory().getName(), false);
          eb.setFooter("ID: " + e.getIdLong(), null);
          eb.setTimestamp(e.getCategory().getCreationTime());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onGuildUpdateIcon(GuildUpdateIconEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getGuild(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Guild icon changed");
          eb.setDescription("[New Icon](" + e.getGuild().getIconUrl() + " \"Click to see new icon.\")\n" +
              "Old Icon:");
          eb.setImage(e.getOldIconUrl());
          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onGuildUpdateName(GuildUpdateNameEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getGuild(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Guild name changed");
          eb.addField("Old Name", e.getOldName(), false);
          eb.addField("New Name", e.getGuild().getName(), false);

          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onGuildUpdateRegion(GuildUpdateRegionEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getGuild(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Guild region changed");
          eb.addField("Old Name", e.getOldRegion().getName(), false);
          eb.addField("New Name", e.getNewRegion().getName(), false);

          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onGuildMemberJoin(GuildMemberJoinEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getGuild(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Member joined");

          eb.setAuthor(e.getUser().getName() + "#" + e.getUser().getDiscriminator(),
              null, e.getUser().getAvatarUrl());

          eb.addField("Created account", e.getUser().getCreationTime()
              .format(DateTimeFormatter.RFC_1123_DATE_TIME), false);

          eb.setFooter("ID: " + e.getUser().getId(), null);
          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onGuildMemberLeave(GuildMemberLeaveEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getGuild(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Member left");

          eb.setAuthor(e.getUser().getName() + "#" + e.getUser().getDiscriminator(),
              null, e.getUser().getAvatarUrl());

          eb.addField("Created account", e.getMember().getJoinDate()
              .format(DateTimeFormatter.RFC_1123_DATE_TIME), false);

          eb.setFooter("ID: " + e.getUser().getId(), null);
          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getGuild(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Role added");

          eb.setAuthor(e.getUser().getName() + "#" + e.getUser().getDiscriminator(),
              null, e.getUser().getAvatarUrl());

          eb.addField("Role", String.join(", ", e.getRoles()
              .stream().map(Role::getName).collect(Collectors.toList())), false);

          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getGuild(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Role removed");

          eb.setAuthor(e.getUser().getName() + "#" + e.getUser().getDiscriminator(),
              null, e.getUser().getAvatarUrl());

          eb.addField("Role", String.join(", ", e.getRoles()
              .stream().map(Role::getName).collect(Collectors.toList())), false);

          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onGuildMemberNickChange(GuildMemberNickChangeEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getGuild(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Nickname removed");

          eb.setAuthor(e.getUser().getName() + "#" + e.getUser().getDiscriminator(),
              null, e.getUser().getAvatarUrl());

          eb.addField("Old Nickname", e.getPrevNick(), false);
          eb.addField("New Nickname", e.getNewNick(), false);

          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onGuildVoiceJoin(GuildVoiceJoinEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getChannelJoined(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Member joined voice");

          User user = e.getMember().getUser();
          eb.setAuthor(user.getName() + "#" + user.getDiscriminator(),
              null, user.getAvatarUrl());

          eb.addField("Channel", e.getChannelJoined().getName(), false);

          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onGuildVoiceMove(GuildVoiceMoveEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getChannelJoined(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Member moved voice channel");

          User user = e.getMember().getUser();
          eb.setAuthor(user.getName() + "#" + user.getDiscriminator(),
              null, user.getAvatarUrl());

          eb.addField("Old channel", e.getChannelLeft().getName(), false);
          eb.addField("New channel", e.getChannelJoined().getName(), false);

          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onGuildVoiceLeave(GuildVoiceLeaveEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getChannelLeft(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Member left voice");

          User user = e.getMember().getUser();
          eb.setAuthor(user.getName() + "#" + user.getDiscriminator(),
              null, user.getAvatarUrl());

          eb.addField("Channel", e.getChannelLeft().getName(), false);

          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onGuildVoiceGuildMute(GuildVoiceGuildMuteEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getGuild(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Member guild " + (e.isGuildMuted() ? "" : "un") + " muted");

          User user = e.getMember().getUser();
          eb.setAuthor(user.getName() + "#" + user.getDiscriminator(),
              null, user.getAvatarUrl());

          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onGuildVoiceGuildDeafen(GuildVoiceGuildDeafenEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getGuild(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Member guild " + (e.isGuildDeafened() ? "" : "un") + "deafened");

          User user = e.getMember().getUser();
          eb.setAuthor(user.getName() + "#" + user.getDiscriminator(),
              null, user.getAvatarUrl());

          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onRoleCreate(RoleCreateEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getGuild(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Role created");

          Role role = e.getRole();
          eb.addField("Name", role.getName(), true);

          Set<String> flags = new HashSet<>();
          if (role.isHoisted()) {
            flags.add("hoisted");
          }
          if (role.isManaged()) {
            flags.add("managed");
          }
          if (role.isMentionable()) {
            flags.add("mentionable");
          }
          if (flags.size() > 0) {
            eb.addField("Flags", StringUtils.capitalize(String.join(", ", flags)), true);
          }

          eb.addField("Permissions", String.join(", ", role.getPermissions()
              .stream().map(Permission::getName).collect(Collectors.toSet())), true);

          eb.setColor(role.getColor());

          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onRoleDelete(RoleDeleteEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getGuild(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Role deleted");

          Role role = e.getRole();
          eb.addField("Name", role.getName(), true);

          eb.setColor(role.getColor());

          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onRoleUpdateName(RoleUpdateNameEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getGuild(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Role name changed");

          Role role = e.getRole();
          eb.addField("Old Name", e.getOldName(), true);
          eb.addField("New Name", role.getName(), true);

          eb.setColor(role.getColor());

          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onRoleUpdatePermissions(RoleUpdatePermissionsEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getGuild(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Role permissions changed");

          Role role = e.getRole();
          eb.addField("Name", role.getName(), true);

          eb.addField("Old Permissions", String.join(", ", e.getOldPermissions()
              .stream().map(Permission::getName).collect(Collectors.toSet())), true);
          eb.addField("New Permissions", String.join(", ", role.getPermissions()
              .stream().map(Permission::getName).collect(Collectors.toSet())), true);

          eb.setColor(role.getColor());

          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onEmoteAdded(EmoteAddedEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getGuild(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Emote added");

          Emote emote = e.getEmote();

          eb.setDescription(emote.getName());
          eb.setImage(emote.getImageUrl());
          eb.setTimestamp(emote.getCreationTime());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onEmoteRemoved(EmoteRemovedEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getGuild(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Emote removed");

          Emote emote = e.getEmote();

          eb.setDescription(emote.getName());
          eb.setImage(emote.getImageUrl());
          eb.addField("Created at", emote.getCreationTime()
              .format(DateTimeFormatter.RFC_1123_DATE_TIME), false);
          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }

  @Override
  public void onEmoteUpdateName(EmoteUpdateNameEvent e) {
    omicron.getLoggingManager().isValid(e.getGuild(), e.getGuild(), e.getClass())
        .thenAccept(r -> {
          if (!r.isSuccess()) {
            return;
          }

          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Emote name changed");

          Emote emote = e.getEmote();

          eb.addField("Old Name", e.getOldName(), false);
          eb.addField("New Name", e.getNewName(), false);
          eb.setImage(emote.getImageUrl());
          eb.setTimestamp(Instant.now());

          r.getChannel().sendMessage(eb.build()).queue();
        });
  }
}
