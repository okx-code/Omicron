package sh.okx.omicron.minecraft;

import sh.okx.omicron.Omicron;

import java.sql.*;

public class MinecraftManager {
    private Omicron omicron;

    public MinecraftManager(Omicron omicron) {
        new Thread(() -> {
            try(Connection connection = omicron.getConnection(); Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS minecraft (user BIGINT(20) UNIQUE KEY, mc VARCHAR(20) UNIQUE);");

                System.out.println("Loaded Minecraft.");
            } catch(SQLException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public String getUsername(long id) {
        try (Connection connection = omicron.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT mc FROM minecraft WHERE user=?")) {
            statement.setLong(1, id);

            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                return null;
            }

            return rs.getString("mc");
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void setUsername(long id, String mc) {
        new Thread(() -> {
            try (Connection connection = omicron.getConnection();
                 PreparedStatement statement = connection.prepareStatement("REPLACE INTO minecraft (user, mc) VALUES (?, ?);")) {
                statement.setLong(1, id);
                statement.setString(2, mc);

                statement.execute();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}
