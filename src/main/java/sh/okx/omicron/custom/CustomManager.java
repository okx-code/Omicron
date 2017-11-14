package sh.okx.omicron.custom;

import net.dv8tion.jda.core.entities.Member;
import org.json.JSONArray;
import org.json.JSONObject;
import sh.okx.omicron.Omicron;

import java.util.ArrayList;
import java.util.List;

public class CustomManager {
    private Omicron omicron;
    private List<CreatedCustomCommand> createdCustomCommands = new ArrayList<>();

    public CustomManager(Omicron omicron) {
        this.omicron = omicron;

        omicron.getJDA().addEventListener(new CustomListener(omicron));

        load();
    }

    public void load() {
        JSONArray existing = omicron.getData().getJSONArray("commands");
        for(int i = 0; i < existing.length(); i++) {
            JSONObject command = existing.getJSONObject(i);

            createdCustomCommands.add(new CreatedCustomCommand(command.getString("guild"),
                    MemberPermission.fromString(omicron.getJDA(), command.getString("permission")),
                    command.getString("command"),
                    command.getString("response")));
        }
    }

    public void save() {
        JSONArray commands = new JSONArray();
        for(CreatedCustomCommand command : createdCustomCommands) {
            JSONObject jsonCommand = new JSONObject();
            jsonCommand.put("guild", command.getGuildId());
            jsonCommand.put("permission", command.getPermission().toString());
            jsonCommand.put("command", command.getCommand());
            jsonCommand.put("response", command.getResponse());

            commands.put(jsonCommand);
        }

        omicron.getData().set("commands", commands);
    }

    public void addCommand(CreatedCustomCommand command) {
        createdCustomCommands.add(command);
    }

    public void removeCommand(CreatedCustomCommand command) {
        createdCustomCommands.remove(command);
    }

    public CreatedCustomCommand getCommand(String command, String guildId, Member member) {
        for(CreatedCustomCommand createdCustomCommand : createdCustomCommands) {
            if(createdCustomCommand.getCommand().equalsIgnoreCase(command) &&
                    createdCustomCommand.getGuildId().equals(guildId) &&
                    createdCustomCommand.getPermission().hasPermission(member)) {
                return createdCustomCommand;
            }
        }

        return null;
    }

    public boolean hasCommand(CreatedCustomCommand command) {
        return createdCustomCommands.contains(command);
    }
}
