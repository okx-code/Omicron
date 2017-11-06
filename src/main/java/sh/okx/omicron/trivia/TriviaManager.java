package sh.okx.omicron.trivia;

import sh.okx.omicron.Omicron;

import java.util.HashMap;
import java.util.Map;

public class TriviaManager {
    private Omicron omicron;

    private Map<String, String> answers = new HashMap<>();

    public TriviaManager(Omicron omicron) {
        this.omicron = omicron;

        omicron.getJDA().addEventListener(new TriviaListener(omicron));
    }

    public void addAnswer(String id, String answer) {
        answers.put(id, answer);
    }

    public String getAnswer(String id) {
        return answers.get(id);
    }

    public void removeAnswer(String id) {
        answers.remove(id);
    }
}
