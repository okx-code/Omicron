package sh.okx.omicron.util;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
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
    }

    public void save() throws IOException {
        Files.write(dataFile.toPath(), data.toString().getBytes(),
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
    }

    public JSONObject getJSONObject(String key) {
        if(!data.has(key)) {
            data.put(key, new JSONObject());
        }

        return data.getJSONObject(key);
    }

    public JSONArray getJSONArray(String key) {
        if(!data.has(key)) {
            data.put(key, new JSONArray());
        }

        return data.getJSONArray(key);
    }

    public void set(String key, Object json) {
        data.put(key, json);
    }
}
