package sh.okx.omicron.command;

public enum Category {
    MISC("Miscellaneous"),
    MUSIC("Music");

    private String string;

    Category(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}
