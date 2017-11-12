package sh.okx.omicron.roles;

import org.json.JSONArray;
import org.json.JSONObject;
import sh.okx.omicron.Omicron;

import java.util.HashMap;
import java.util.Map;

public class RoleManager {
    private Omicron omicron;
    private Map<String, String> roles = new HashMap<>();

    public RoleManager(Omicron omicron) {
        this.omicron = omicron;
        omicron.getJDA().addEventListener(new RoleListener(omicron));
        load();
    }

    public void load() {
        JSONArray rolesJson = omicron.getData().getJSONArray("roles");
        for(int i = 0; i < rolesJson.length(); i++) {
            JSONObject roleJson = rolesJson.getJSONObject(i);
            roles.put(roleJson.getString("guild"), roleJson.getString("role"));
        }
    }

    public void save() {
        JSONArray rolesJson = new JSONArray();
        for(Map.Entry<String, String> role : roles.entrySet()) {
            JSONObject roleJson = new JSONObject();
            roleJson.put("guild", role.getKey());
            roleJson.put("role", role.getValue());

            rolesJson.put(roleJson);
        }

        omicron.getData().set("roles", rolesJson);
    }

    public void setDefaultRole(String guild, String role) {
        roles.put(guild, role);
    }

    public void removeDefaultRole(String guild) {
        roles.remove(guild);
    }

    public String getDefaultRole(String guild) {
        return roles.get(guild);
    }
}
