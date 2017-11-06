package sh.okx.omicron.util;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class Data {
    private static JSONObject data;
    private static File dataFile;

    public Data(String pathName) throws IOException {
        load(pathName);
    }

    public void load(String pathName) throws IOException {
        dataFile = new File(pathName);
        if(!dataFile.exists()) {
            Files.write(dataFile.toPath(), "{}".getBytes(),
                    StandardOpenOption.CREATE_NEW);
        }

        data = new JSONObject(IOUtils.toString(dataFile.toURI(), "UTF-8"));

        if(!data.has("global")) {
            data.put("global", new JSONObject());
        }
    }

    public void save() throws IOException {
        Files.write(dataFile.toPath(), data.toString().getBytes(),
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
    }

    public JSONObject getGlobalData() {
        return data.getJSONObject("global");
    }

    public JSONObject getGuildData(Guild guild) {
        if(!data.has(guild.getId())) {
            data.put(guild.getId(), new JSONObject());
        }

        return data.getJSONObject(guild.getId());
    }

    public void setGuildData(Guild guild, JSONObject json) {
        data.put(guild.getId(), json);
    }

    public JSONObject getChannelData(TextChannel channel) {
        if(!data.has(channel.getId())) {
            data.put(channel.getId(), new JSONObject());
        }

        return data.getJSONObject(channel.getId());
    }

    public void setChannelData(TextChannel channel, JSONObject json) {
        data.put(channel.getId(), json);
    }
}
