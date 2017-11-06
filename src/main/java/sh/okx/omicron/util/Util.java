package sh.okx.omicron.util;

public class Util {
    public static String limit(String string, int length) {
        string = string.trim();
        return string.substring(0, Math.min(string.length(), length - 3)).trim() + "...";
    }

    public static String stripHtml(String string) {
        return string
                .replaceAll("</?b>", "**")
                .replaceAll("</?i>", "*")
                .replaceAll("</?[a-z]+>", "");
    }
}
