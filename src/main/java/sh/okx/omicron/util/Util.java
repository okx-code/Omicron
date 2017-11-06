package sh.okx.omicron.util;

public class Util {
    public static String limit(String string, int length) {
        string = string.trim();
        String substring = string.substring(0, Math.min(string.length(), length - 3));
        if(substring.length() < string.length()) {
            return substring.trim() + "...";
        }
        return string;
    }

    public static String stripHtml(String string) {
        return string
                .replaceAll("</?b>", "**")
                .replaceAll("</?i>", "*")
                .replaceAll("</?[a-z]+>", "");
    }
}
