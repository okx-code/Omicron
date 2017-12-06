package sh.okx.omicron.roles;

import sh.okx.omicron.Omicron;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class RoleManager {
    private Omicron omicron;
    private Map<String, String> roles = new HashMap<>();

    public RoleManager(Omicron omicron) {
        this.omicron = omicron;
        omicron.getJDA().addEventListener(new RoleListener(omicron));

        new Thread(() -> {
            try {
                Connection connection = omicron.getConnection();

                Statement table = connection.createStatement();

                table.execute("CREATE TABLE IF NOT EXISTS roles (guild BIGINT(20), role BIGINT(20) );");

                table.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public boolean hasDefaultRole(long guildId) {
        try {
            Connection connection = omicron.getConnection();

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM roles WHERE guild=?");
            statement.setLong(1, guildId);

            boolean yes = statement.executeQuery().next();

            statement.close();
            connection.close();

            return yes;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setDefaultRole(long guild, long role) {
        new Thread(() -> {
            try {
                Connection connection = omicron.getConnection();

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
        }).start();
    }

    public void removeDefaultRole(long guild) {
        new Thread(() -> {
            try {
                Connection connection = omicron.getConnection();

                PreparedStatement statement = connection.prepareStatement("DELETE FROM roles WHERE guild=?;");
                statement.setLong(1, guild);

                statement.execute();

                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public long getDefaultRole(long guild) {
        try {
            Connection connection = omicron.getConnection();

            PreparedStatement statement = connection.prepareStatement("SELECT role FROM roles WHERE guild=?");
            statement.setLong(1, guild);

            ResultSet rs = statement.executeQuery();
            rs.next();
            long role = rs.getLong("role");

            statement.close();
            connection.close();

            return role;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
