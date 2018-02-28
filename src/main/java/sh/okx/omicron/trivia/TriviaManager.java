package sh.okx.omicron.trivia;

import sh.okx.omicron.Omicron;

import java.util.HashMap;
import java.util.Map;

public class TriviaManager {
  private Omicron omicron;

  private Map<Long, String> answers = new HashMap<>();

  public TriviaManager(Omicron omicron) {
    this.omicron = omicron;

    omicron.getJDA().addEventListener(new TriviaListener(omicron));
  }

  public void addAnswer(long message, String answer) {
    answers.put(message, answer);
  }

  public String getAnswer(long message) {
    return answers.get(message);
  }

  public void removeAnswer(long message) {
    answers.remove(message);
  }
}
