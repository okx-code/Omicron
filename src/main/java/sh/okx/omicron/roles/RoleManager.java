package sh.okx.omicron.roles;

import sh.okx.omicron.Omicron;
import sh.okx.sql.api.SqlException;
import sh.okx.sql.api.query.QueryResults;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RoleManager {
    private Omicron omicron;

    public RoleManager(Omicron omicron) {
        this.omicron = omicron;
        omicron.getJDA().addEventListener(new RoleListener(omicron));

        omicron.runConnectionAsync(connection ->
            connection.table("roles")
                .create()
                .ifNotExists()
                .column("guild BIGINT(20)")
                .column("role BIGINT(20)")
                .column("used BIT") // 0 = add on join, 1 = can be manually obtainable
                .executeAsync()
                .thenAccept(i -> omicron.getLogger().info("Loaded default roles with status " + i)));
    }

    public CompletableFuture<Set<Long>> getFreeRoles(long guildId) {
        return omicron.runConnectionAsync(connection ->
            connection.table("roles")
                .select()
                .where().prepareEquals("guild", guildId).and().prepareEquals("used", 1)
                .then()
                .executeAsync()
                .thenApply(qr -> {
                    Set<Long> roles = new HashSet<>();
                    ResultSet rs = qr.getResultSet();
                    try {
                        while(rs.next()) {
                            roles.add(rs.getLong("role"));
                        }
                    } catch(SQLException ex) {
                        throw new SqlException(ex);
                    }
                    return roles;
                }));
    }

    public void addFreeRole(long guild, long role) {
        CompletableFuture.runAsync(() -> {
            try(PreparedStatement statement = omicron.getConnection().getUnderlying()
                    .prepareStatement("INSERT INTO roles (guild, role, used) VALUES (?, ?, ?);")) {
                statement.setLong(1, guild);
                statement.setLong(2, role);
                statement.setLong(3, 1);

                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void removeFreeRole(long guild, long role) {
        omicron.runConnectionAsync(connection ->
                connection.table("roles").delete().where()
                .prepareEquals("guild", guild).and()
                .prepareEquals("role", role).and()
                .prepareEquals("used", 1)
                .then().executeAsync());
    }

    public CompletableFuture<Boolean> hasDefaultRole(long guildId) {
        return omicron.runConnectionAsync(connection ->
                connection.table("roles")
                .select()
                .where().prepareEquals("guild", guildId).and().equals("used", 0)
                .then()
                .executeAsync()
                .thenApply(QueryResults::next));
    }

    public void setDefaultRole(long guild, long role) {
        CompletableFuture.runAsync(() -> {
            try(PreparedStatement statement = omicron.getConnection().getUnderlying()
                        .prepareStatement("REPLACE INTO roles (guild, role, used) VALUES (?, ?, ?);")) {
                statement.setLong(1, guild);
                statement.setLong(2, role);
                statement.setLong(3, 0);

                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void removeDefaultRole(long guild) {
        omicron.runConnectionAsync(connection -> connection.table("roles")
                .delete().where().prepareEquals("guild", guild).then().executeAsync());
    }

    public CompletableFuture<Long> getDefaultRole(long guildId) {
        return omicron.runConnectionAsync(connection -> connection.table("roles")
            .select("role")
            .where().prepareEquals("guild", guildId).and().equals("used", 0).then()
            .executeAsync()
            .thenApply(qr -> {
                if(!qr.next()) {
                    return -1L;
                }
                try {
                    return qr.getResultSet().getLong("role");
                } catch (SQLException e) {
                    throw new SqlException(e);
                }
            }));
    }
}
