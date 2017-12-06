package sh.okx.omicron.custom;

import net.dv8tion.jda.core.entities.Member;
import sh.okx.omicron.Omicron;

import java.sql.*;

public class CustomManager {
    private Omicron omicron;
    public CustomManager(Omicron omicron) {
        this.omicron = omicron;

        omicron.getJDA().addEventListener(new CustomListener(omicron));

        new Thread(() -> {
            try {
                Connection connection = omicron.getConnection();

                Statement statement = connection.createStatement();

                statement.execute("CREATE TABLE IF NOT EXISTS commands (guild BIGINT(20), permission BIGINT(20), " +
                        "command VARCHAR(255), response TEXT );");

                statement.close();
                connection.close();

                System.out.println("Loaded custom commands.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void addCommand(CreatedCustomCommand command) {
        new Thread(() -> {
            try {
                Connection connection = omicron.getConnection();

                PreparedStatement statement = connection.prepareStatement("REPLACE INTO commands " +
                        "(guild, permission, command, response) VALUES (?, ?, ?, ?)");
                statement.setLong(1, command.getGuildId());
                statement.setLong(2, command.getPermission().toLong());
                statement.setString(3, command.getCommand());
                statement.setString(4, command.getResponse());

                statement.execute();

                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void removeCommand(long guild, MemberPermission permission, String command) {
        new Thread(() -> {
            try {
                Connection connection = omicron.getConnection();

                PreparedStatement statement = connection.prepareStatement("DELETE FROM commands " +
                        "WHERE guild=? AND permission=? AND command=?");
                statement.setLong(1, guild);
                statement.setLong(2, permission.toLong());
                statement.setString(3, command);

                statement.execute();

                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public CreatedCustomCommand getCommand(long guild, Member member, String command) {
        try {
            Connection connection = omicron.getConnection();

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM commands " +
                    "WHERE guild=? AND command=?");
            statement.setLong(1, guild);
            statement.setString(2, command);

            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                MemberPermission permission = MemberPermission.fromLong(omicron.getJDA(), rs.getLong("permission"));
                if(!permission.hasPermission(member)) {
                    continue;
                }

                return new CreatedCustomCommand(rs.getLong("guild"),
                        permission,
                        rs.getString("command"),
                        rs.getString("response"));
            }

            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
