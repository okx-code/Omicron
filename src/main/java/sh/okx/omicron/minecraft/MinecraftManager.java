package sh.okx.omicron.minecraft;

import sh.okx.omicron.Omicron;
import sh.okx.sql.api.SqlException;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class MinecraftManager {
    private Omicron omicron;

    public MinecraftManager(Omicron omicron) {
        this.omicron = omicron;

        omicron.getConnection().table("minecraft")
                .create()
                .ifNotExists()
                .column("user BIGINT(20) UNIQUE KEY")
                .column("uuid VARCHAR(36) UNIQUE")
                .executeAsync()
                .thenAccept(i -> omicron.getLogger().info("Loaded Minecraft with status {}", i));
    }

    public CompletableFuture<String> getUsername(long id) {
        return omicron.getConnection()
                .table("minecraft")
                .select("mc")
                .where().prepareEquals("user", id)
                .then().executeAsync()
                .thenApply(rs -> {
                    if(!rs.next()) {
                        return null;
                    }
                    try {
                        return rs.getResultSet().getString("mc");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return null;
                    }
                });
    }

    public void setUsername(long id, String mc) {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement statement = omicron.getConnection().getUnderlying()
                        .prepareStatement("REPLACE INTO minecraft (user, mc) VALUES (?, ?);");
                statement.setLong(1, id);
                statement.setString(2, mc);

                statement.execute();
            } catch(SQLException ex) {
                throw new SqlException(ex);
            }
        });
    }
}
