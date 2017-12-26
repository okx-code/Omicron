package sh.okx.omicron.custom;

import net.dv8tion.jda.core.entities.Member;
import sh.okx.omicron.Omicron;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class CustomManager {
    private Omicron omicron;
    public CustomManager(Omicron omicron) {
        this.omicron = omicron;

        omicron.getJDA().addEventListener(new CustomListener(omicron));

        omicron.runConnectionAsync(connection -> connection.table("commands")
                .create().ifNotExists()
                .column("guild BIGINT(20)")
                .column("permission BIGINT(20)")
                .column("command VARCHAR(255)")
                .column("response TEXT")
                .executeAsync()
                .thenAccept(i -> omicron.getLogger().info("Loaded custom commands with status {}", i)));
    }

    public void addCommand(CreatedCustomCommand command) {
        CompletableFuture.runAsync(() -> {
            try(Connection connection = omicron.getConnection().getUnderlying();
                PreparedStatement statement = connection.prepareStatement(
                        "REPLACE INTO commands (guild, permission, command, response) VALUES (?, ?, ?, ?)")) {
                statement.setLong(1, command.getGuildId());
                statement.setLong(2, command.getPermission().toLong());
                statement.setString(3, command.getCommand());
                statement.setString(4, command.getResponse());

                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void removeCommand(long guild, MemberPermission permission, String command) {
        CompletableFuture.runAsync(() -> {
            try(Connection connection = omicron.getConnection().getUnderlying();
                 PreparedStatement statement = connection.prepareStatement(
                         "DELETE FROM commands WHERE guild=? AND permission=? AND command=?")) {
                statement.setLong(1, guild);
                statement.setLong(2, permission.toLong());
                statement.setString(3, command);

                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<CreatedCustomCommand> getCommand(long guild, Member member, String command) {
        return omicron.runConnectionAsync(connection -> connection.table("commands").select()
                .where().prepareEquals("guild", guild).and().prepareEquals("command", command)
                .then().executeAsync()
                .thenApply(qr -> {
                    try {
                        ResultSet rs = qr.getResultSet();
                        while (rs.next()) {
                            MemberPermission permission = MemberPermission.fromLong(omicron.getJDA(), rs.getLong("permission"));

                            if (!permission.hasPermission(member)) {
                                continue;
                            }

                            return new CreatedCustomCommand(rs.getLong("guild"),
                                    permission,
                                    rs.getString("command"),
                                    rs.getString("response"));
                        }
                    } catch(SQLException ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }));
    }
}
