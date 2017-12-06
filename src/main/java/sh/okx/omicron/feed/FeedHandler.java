package sh.okx.omicron.feed;

public interface FeedHandler {
    void cancel();
    void start();
    boolean isCancelled();
    void addListener(FeedListener listener);
}
