package sh.okx.omicron.logging;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.channel.category.CategoryCreateEvent;
import net.dv8tion.jda.core.events.channel.category.CategoryDeleteEvent;
import net.dv8tion.jda.core.events.channel.category.update.CategoryUpdateNameEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.voice.update.VoiceChannelUpdateNameEvent;
import net.dv8tion.jda.core.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.core.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.core.events.emote.update.EmoteUpdateNameEvent;
import net.dv8tion.jda.core.events.guild.member.*;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateIconEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateRegionEvent;
import net.dv8tion.jda.core.events.guild.voice.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.events.role.RoleCreateEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.core.events.role.update.RoleUpdatePermissionsEvent;
import sh.okx.omicron.Omicron;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LoggingManager {

    private Omicron omicron;
    private Map<Class<? extends Event>, Integer> indexes = new HashMap<>();

    public LoggingManager(Omicron omicron) {
        this.omicron = omicron;

        omicron.getJDA().addEventListener(new LoggingListener(omicron));

        omicron.runConnectionAsync(connection ->
                connection.table("logging")
                .create().ifNotExists()
                .column("guild BIGINT(20)")
                .column("channel BIGINT(20)")
                .column("log BIGINT(20)")
                .column("PRIMARY KEY (guild, channel)")
                .executeAsync()
                .thenAccept(i -> omicron.getLogger().info("Loaded loggers with status {}", i)));

        /*
        Info for log - max 66
        0  GuildMessageUpdateEvent
        1  GuildMessageDeleteEvent
        2  VoiceChannelDeleteEvent
        3  VoiceChannelUpdateNameEvent
        4  VoiceChannelCreateEvent
        5  CategoryDeleteEvent
        6  CategoryUpdateNameEvent
        7  CategoryCreateEvent
        8  GuildUpdateIconEvent
        9  GuildUpdateNameEvent
        10 GuildUpdateRegionEvent
        11 GuildMemberJoinEvent
        12 GuildMemberLeaveEvent
        13 GuildMemberRoleAddEvent
        14 GuildMemberRoleRemoveEvent
        15 GuildMemberNickChangeEvent
        16 GuildVoiceJoinEvent
        17 GuildVoiceMoveEvent
        18 GuildVoiceLeaveEvent
        19 GuildVoiceGuildMuteEvent
        20 GuildVoiceGuildDeafenEvent
        21 RoleCreateEvent
        22 RoleDeleteEvent
        23 RoleUpdateNameEvent
        24 RoleUpdatePermissionsEvent
        25 EmoteAddedEvent
        26 EmoteRemovedEvent
        27 EmoteUpdateNameEvent
         */

        indexes.put(GuildMessageUpdateEvent.class,      0 );
        indexes.put(GuildMessageDeleteEvent.class,      1 );
        indexes.put(VoiceChannelDeleteEvent.class,      2 );
        indexes.put(VoiceChannelUpdateNameEvent.class,  3 );
        indexes.put(VoiceChannelCreateEvent.class,      4 );
        indexes.put(CategoryDeleteEvent.class,          5 );
        indexes.put(CategoryUpdateNameEvent.class,      6 );
        indexes.put(CategoryCreateEvent.class,          7 );
        indexes.put(GuildUpdateIconEvent.class,         8 );
        indexes.put(GuildUpdateNameEvent.class,         9 );
        indexes.put(GuildUpdateRegionEvent.class,       10);
        indexes.put(GuildMemberJoinEvent.class,         11);
        indexes.put(GuildMemberLeaveEvent.class,        12);
        indexes.put(GuildMemberRoleAddEvent.class,      13);
        indexes.put(GuildMemberRoleRemoveEvent.class,   14);
        indexes.put(GuildMemberNickChangeEvent.class,   15);
        indexes.put(GuildVoiceJoinEvent.class,          16);
        indexes.put(GuildVoiceMoveEvent.class,          17);
        indexes.put(GuildVoiceLeaveEvent.class,         18);
        indexes.put(GuildVoiceGuildMuteEvent.class,     19);
        indexes.put(GuildVoiceGuildDeafenEvent.class,   20);
        indexes.put(RoleCreateEvent.class,              21);
        indexes.put(RoleDeleteEvent.class,              22);
        indexes.put(RoleUpdateNameEvent.class,          23);
        indexes.put(RoleUpdatePermissionsEvent.class,   24);
        indexes.put(EmoteAddedEvent.class,              25);
        indexes.put(EmoteRemovedEvent.class,            26);
        indexes.put(EmoteUpdateNameEvent.class,         27);
    }

    public CompletableFuture<LogResult> isValid(Guild guild, ISnowflake channel, Class<? extends Event> clazz) {
        return omicron.runConnectionAsync(connection ->
            connection.table("logging")
                    .select("log", "channel")
                    .where().prepareEquals("guild", guild.getIdLong()).then()
                    .executeAsync()
                    .thenApply(qr -> {
                        if(!qr.next()) {
                            return new LogResult(false, null);
                        }

                        try {
                            ResultSet rs = qr.getResultSet();
                            return new LogResult(nbit(rs.getLong("log"), indexes.get(clazz)) > 0 &&
                                    (channel.getIdLong() != rs.getLong("channel")),
                                omicron.getJDA().getTextChannelById(rs.getLong("channel")));
                        } catch(SQLException ex) {
                            ex.printStackTrace();
                            return new LogResult(false, null);
                        }
                    }));
    }

    public long nbit(long i, long n) {
        return (i >> n) & 1;
    }

    public long setNbit(long i, long n) {
        return i | (1 << n);
    }

    public long unsetNbit(long i, long n) {
        return i & ~(1 << n);
    }

    public void setLogging(Guild guild, TextChannel channel, long logging) {
        CompletableFuture.runAsync(() -> {
            omicron.runConnection(connection -> connection
                    .executeUpdate("REPLACE INTO logging (guild, channel, log) VALUES (?, ?, ?)",
                            guild.getId(), channel.getId(), String.valueOf(logging)));
        });
    }

    public CompletableFuture<Long> getLogging(TextChannel channel) {
        return omicron.runConnectionAsync(connection ->
                connection.table("logging")
                .select().where()
                .prepareEquals("guild", channel.getGuild().getId()).and()
                .prepareEquals("channel", channel.getId()).then()
                .executeAsync()
                .thenApply(qr -> {
                    if(!qr.next()) {
                        return 0L;
                    }

                    try {
                        return qr.getResultSet().getLong("log");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return 0L;
                    }
                }));
    }
}
