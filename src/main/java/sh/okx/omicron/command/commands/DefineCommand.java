package sh.okx.omicron.command.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

import java.net.URLEncoder;

public class DefineCommand extends Command {
    public DefineCommand(Omicron omicron) {
        super(omicron, "define", Category.MISC, "Define a word.\n" +
                "Usage: **o/define <word>**",
                "def", "definition", "dict", "dictionary");
    }

    @Override
    public void run(Message message, String args) {
        MessageChannel channel = message.getChannel();

        HttpClient client = HttpClientBuilder.create().build();

        try {
            HttpGet get = new HttpGet("https://www.wordsapi.com/mashape/words/" + URLEncoder.encode(args, "UTF-8") +
                    "?when=2017-12-28T22:29:09.375Z&encrypted=8cfdb28de723919be89907bded58bcb0aeb0240933f892b8");

            HttpResponse execute = client.execute(get);

            JSONObject json = new JSONObject(IOUtils.toString(execute.getEntity().getContent(), "UTF-8"));
            if(!json.optBoolean("success", true)) {
                channel.sendMessage("Could not find definition.").queue();
                return;
            }

            JSONArray results = json.getJSONArray("results");
            EmbedBuilder eb = new EmbedBuilder();

            for(int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                eb.addField("Result",
                        "Part of speech: **" + result.getString("partOfSpeech") + "**\n" +
                        "Definition: **" + result.getString("definition") + "**", false);
            }

            if(json.has("syllables")) {
                JSONObject syllables = json.getJSONObject("syllables");
                eb.addField("Syllables",
                        "Count: **" + syllables.getInt("count") + "**\n" +
                                "Syllables: **" +
                                StringUtils.join(syllables.getJSONArray("list").toList(), " ") + "**",
                        false);
            }

            eb.setTitle(json.getString("word"));
            channel.sendMessage(eb.build()).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
