package sh.okx.omicron.trivia;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TriviaCommand extends Command {
    private Map<String, Integer> categories = new HashMap<>();


    public TriviaCommand(Omicron omicron) {
        super(omicron, "trivia", Category.MISC,
                "Trivia questions. Run without any arguments for a random category,\n" +
                        "Use **o/trivia categories** to list categories, and use\n" +
                        "**o/trivia <category>** for a question in a specific category.");

        categories.put("General Knowledge", 9);
        categories.put("Books", 10);
        categories.put("Film", 11);
        categories.put("Music", 12);
        categories.put("Musicals", 13);
        categories.put("Theatres", 13);
        categories.put("Television", 14);
        categories.put("Video Games", 15);
        categories.put("Board Games", 16);
        categories.put("Science", 17);
        categories.put("Nature", 17);
        categories.put("Computers", 18);
        categories.put("Mathematics", 19);
        categories.put("Mythology", 20);
        categories.put("Sports", 21);
        categories.put("Geography", 22);
        categories.put("History", 23);
        categories.put("Politics", 24);
        categories.put("Art", 25);
        categories.put("Celebrities", 26);
        categories.put("Animals", 27);
        categories.put("Vehicles", 28);
        categories.put("Comics", 29);
        categories.put("Gadgets", 30);
        categories.put("Anime", 31);
        categories.put("Manga", 31);
        categories.put("Cartoons", 32);
        categories.put("Animations", 32);
    }

    @Override
    public void run(Message message, String content) {
        MessageChannel channel = message.getChannel();

        if(content.equalsIgnoreCase("categories")) {
            sendCategories(channel);
            return;
        }

        int category = 0;

        if(!content.isEmpty()) {
            for(Map.Entry<String, Integer> entry : categories.entrySet()) {
                if(entry.getKey().equalsIgnoreCase(content)) {
                    category = entry.getValue();
                    break;
                }
            }
        }

        String query = "amount=1";

        if(category != 0) {
            query += "&category=" + category;
        }

        String response;
        try {
            response = IOUtils.toString(new URL("https://opentdb.com/api.php?" + query), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            channel.sendMessage("An unknown error occured when trying to get a trivia question.").queue();
            return;
        }

        JSONObject json = new JSONObject(response);

        JSONObject results = json.getJSONArray("results").getJSONObject(0);

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(results.getString("category"), null);

        StringBuilder desc = new StringBuilder();

        desc.append("Type: ").append(results.getString("type").equals("boolean") ? "True/False" : "Multiple Choice").append("\n");
        desc.append("Difficulty: ").append(StringUtils.capitalize(results.getString("difficulty")));

        eb.setDescription(desc.toString());

        eb.addField("Question", StringEscapeUtils.unescapeHtml4(results.getString("question")), false);

        List<String> answers = new ArrayList<>();

        answers.add(results.getString("correct_answer"));

        JSONArray wrong = results.getJSONArray("incorrect_answers");

        for(Object o : wrong) {
            answers.add(String.valueOf(o));
        }

        Collections.shuffle(answers);

        AtomicInteger j = new AtomicInteger(0);
        List<String> displayedAnswers = answers.stream().map(str -> (char) ('A' + j.getAndIncrement()) + ". " + str)
                .collect(Collectors.toList());
        eb.addField("Answers", StringEscapeUtils.unescapeHtml4(String.join("\n", displayedAnswers)), false);

        Message trivia = channel.sendMessage(eb.build()).complete();
        String correct = "\uD83C" + (char) (0xDDE6 + answers.indexOf(results.getString("correct_answer")));
        omicron.getTriviaManager().addAnswer(trivia.getIdLong(), correct);

        for(int i = 0; i < answers.size(); i++) {
            trivia.addReaction("\uD83C" + (char) (0xDDE6 + i)).queue();
        }
    }

    private void sendCategories(MessageChannel channel) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Categories", null);

        eb.setDescription(String.join("\t", categories.keySet()));

        channel.sendMessage(eb.build()).queue();
    }
}
