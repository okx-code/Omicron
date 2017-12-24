package sh.okx.omicron.roles;

import sh.okx.omicron.Omicron;
import sh.okx.sql.api.SqlException;
import sh.okx.sql.api.query.QueryResults;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class RoleManager {
    private Omicron omicron;

    public RoleManager(Omicron omicron) {
        this.omicron = omicron;
        omicron.getJDA().addEventListener(new RoleListener(omicron));

        omicron.getConnection()
                .table("roles")
                .create()
                .ifNotExists()
                .column("guild BIGINT(20)")
                .column("role BIGINT(20)")
                .executeAsync()
                .thenAccept(i -> omicron.getLogger().info("Loaded default roles with status " + i));
    }

    public CompletableFuture<Boolean> hasDefaultRole(long guildId) {
        return omicron.getConnection()
                .table("roles")
                .select()
                .where().prepareEquals("guild", guildId)
                .then()
                .executeAsync()
                .thenApply(QueryResults::next);
    }

    public void setDefaultRole(long guild, long role) {
        CompletableFuture.runAsync(() -> {
            try {
                Connection connection = omicron.getConnection().getUnderlying();

                PreparedStatement statement = connection.prepareStatement("REPLACE INTO roles (guild, role) " +
                        "VALUES (?, ?);");
                statement.setLong(1, guild);
                statement.setLong(2, role);

                statement.execute();

                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void removeDefaultRole(long guild) {
        CompletableFuture.runAsync(() -> {
            try {
                Connection connection = omicron.getConnection().getUnderlying();

                PreparedStatement statement = connection.prepareStatement("DELETE FROM roles WHERE guild=?;");
                statement.setLong(1, guild);

                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Long> getDefaultRole(long guild) {
        return omicron.getConnection().table("roles")
                .select("role")
                .where().prepareEquals("guild", guild).then()
                .executeAsync()
                .thenApply(qr -> {
                    try {
                        return qr.getResultSet().getLong("role");
                    } catch (SQLException e) {
                        throw new SqlException(e);
                    }
                });
    }
}
