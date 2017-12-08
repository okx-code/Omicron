package sh.okx.omicron.alias;

import sh.okx.omicron.Omicron;

import java.sql.*;

public class AliasManager {
    private Omicron omicron;

    public AliasManager(Omicron omicron) {
        this.omicron = omicron;

        omicron.getJDA().addEventListener(new AliasListener(omicron));

        new Thread(() -> {
            try(Connection connection = omicron.getConnection(); Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS aliases (command VARCHAR(255) UNIQUE KEY, alias TEXT);");

                System.out.println("Loaded aliases.");
            } catch(SQLException ex) {
                ex.printStackTrace();
            }
        }).start();

    }

    public String getAlias(String command) {
        try(Connection connection = omicron.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT alias FROM aliases WHERE command=?;")) {
            statement.setString(1, command);

            ResultSet resultSet = statement.executeQuery();
            if(!resultSet.next()) {
                return null;
            }

            return resultSet.getString("alias");
        } catch(SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void setAlias(String command, String alias) {
        new Thread(() -> {
            try (Connection connection = omicron.getConnection();
                 PreparedStatement statement = connection.prepareStatement("REPLACE INTO aliases (command, alias) " +
                         "VALUES (?, ?);")) {
                statement.setString(1, command);
                statement.setString(2, alias);

                statement.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public void removeAlias(String command) {
        new Thread(() -> {
            try (Connection connection = omicron.getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM aliases WHERE command=?")) {
                statement.setString(1, command);

                statement.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}
