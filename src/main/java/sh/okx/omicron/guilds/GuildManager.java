package sh.okx.omicron.guilds;

import net.dv8tion.jda.core.JDA;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;
import sh.okx.omicron.Omicron;

import java.io.IOException;

public class GuildManager {
  private final OkHttpClient client = new OkHttpClient();
  private Omicron omicron;

  public GuildManager(Omicron omicron) {
    this.omicron = omicron;
  }

  public void updateGuildCount() {
    try {
      updateDiscordBotList();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private void updateDiscordBotList() throws IOException {
    JSONObject payload = new JSONObject().put("server_count", omicron.getJDA().getGuilds().size());
    JDA.ShardInfo info = omicron.getJDA().getShardInfo();
    if (info != null) {
      payload.put("shard_id", info.getShardId()).put("shard_count", info.getShardTotal());
    }

    client.newCall(new Request.Builder()
        .url("https://discordbots.org/api/bots/" + omicron.getJDA().getSelfUser().getId() + "/stats")
        .addHeader("Content-Type", "application/json")
        .addHeader("Authorization", "YOUR-TOKEN-GOES-HERE")
        .post(RequestBody.create(MediaType.parse("application/json"), payload.toString()))
        .build()
    ).execute().close();

  }
}
