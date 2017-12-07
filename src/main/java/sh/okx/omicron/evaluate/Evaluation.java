package sh.okx.omicron.evaluate;

public class Evaluation {
    private String input;
    private String output;
    private String debug;
    private String language;
    private long milliseconds;

    public Evaluation(String input, String output, String debug, String language, long milliseconds) {
        this.input = input;
        this.output = output;
        this.debug = debug;
        this.language = language;
        this.milliseconds = milliseconds;
    }


    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    public String getDebug() {
        return debug;
    }

    public String getLanguage() {
        return language;
    }

    public long getMilliseconds() {
        return milliseconds;
    }
}
