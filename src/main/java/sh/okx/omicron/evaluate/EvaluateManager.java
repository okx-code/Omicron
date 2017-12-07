package sh.okx.omicron.evaluate;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class EvaluateManager {
    public void commandLanguage(MessageChannel channel, String content, String language) {
        Evaluation eval = eval(content, language);
        if(eval == null) {
            channel.sendMessage("An unexpected error occured trying to evaluate that code. " +
                    "This has been reported to the developers.").queue();
            return;
        }

        channel.sendMessage(buildEmbed(eval)).queue();
    }

    public Evaluation eval(String text, String language) {
        try {
            long start = System.currentTimeMillis();

            HttpClient httpClient = HttpClientBuilder.create().build();

            HttpPost request = new HttpPost("https://tio.run/cgi-bin/run/api/");

            request.setEntity(new ByteArrayEntity(encode(language, text, "")));

            HttpResponse response = httpClient.execute(request);

            long end = System.currentTimeMillis();

            String out = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            String sep = out.substring(0, 16);
            String[] split = out.split(Pattern.quote(sep));

            String debug = null;
            StringBuilder output = new StringBuilder();
            for (String line : split[1].split("\\n\\n")) {
                output.append(line).append("\n");
            }

            String[] split2 = split[2].split("\\n\\nReal time: ");

            if(split2.length > 1) {
                debug = split2[0];
            }

            return new Evaluation(text, output.toString(), debug, language, end-start);
        } catch(IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public MessageEmbed buildEmbed(Evaluation evaluation) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Evaluation");

        embedBuilder.addField("Input", "```" + evaluation.getLanguage() + "\n" +
                evaluation.getInput() + "\n```", false);

        embedBuilder.addField("Output", "```\n" + evaluation.getOutput() + "\n```", true);
        if(evaluation.getDebug() != null) {
            embedBuilder.addField("Debug", "```\n" + evaluation.getDebug() + "\n```", true);
        }

        embedBuilder.setFooter(StringUtils.capitalize(evaluation.getLanguage()) + " | Took " +
                evaluation.getMilliseconds() + "ms.", null);
        return embedBuilder.build();
    }

    private byte[] compress(String str) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // create deflater without header
            DeflaterOutputStream def = new DeflaterOutputStream(out, new Deflater(Deflater.BEST_COMPRESSION, true));
            def.write(str.getBytes());
            def.close();
            return out.toByteArray();
        } catch(IOException ex) {
            return null;
        }
    }

    private byte[] encode(String lang, String code, String input) {
        if(lang.contains("\0") || input.contains("\0") || code.contains("\0")) {
            throw new IllegalArgumentException("Argument contains NUL character.");
        }

        return compress("Vlang\0001\000" + lang + "\000F.input.tio\000" + input.length() + "\000" + input + "\000" +
                "F.code.tio\000" + code.length() + "\000" + code + "R");
    }
}
