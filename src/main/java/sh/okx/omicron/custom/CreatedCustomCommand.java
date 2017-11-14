package sh.okx.omicron.custom;

public class CreatedCustomCommand {
    private String guildId;
    private MemberPermission permission;
    private String command;
    private String response;

    public CreatedCustomCommand(String guildId, MemberPermission permission, String command, String response) {
        this.permission = permission;
        this.command = command;
        this.response = response;
        this.guildId = guildId;
    }

    public String getGuildId() {
        return guildId;
    }

    public MemberPermission getPermission() {
        return permission;
    }

    public String getCommand() {
        return command;
    }


    public String getResponse() {
        return response;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || !(o instanceof CreatedCustomCommand)) {
            return false;
        }

        CreatedCustomCommand command = (CreatedCustomCommand) o;
        return command.getCommand().equalsIgnoreCase(this.getCommand())
                && command.getGuildId().equals(this.getGuildId())
                && command.getPermission().equals(this.getPermission());
    }

}
