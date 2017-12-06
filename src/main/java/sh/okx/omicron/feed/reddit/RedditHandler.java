package sh.okx.omicron.feed.reddit;

import net.dean.jraw.RedditClient;
import net.dean.jraw.fluent.FluentRedditClient;
import net.dean.jraw.fluent.SubredditReference;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import org.apache.commons.io.IOUtils;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.feed.FeedHandler;
import sh.okx.omicron.feed.FeedListener;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

public class RedditHandler implements FeedHandler {
    private Instant lastChecked = Instant.now();
    private String subredditName;
    private TimerTask task;
    private boolean cancelled = false;
    private Set<AbstractRedditListener> listeners = new HashSet<>();

    public void addListener(FeedListener listener) {
        listeners.add((AbstractRedditListener) listener);
    }

    public RedditHandler(String subredditName) {
        this.subredditName = subredditName;
    }

    @Override
    public void cancel() {
        cancelled = task.cancel();
    }

    @Override
    public void start() {
        String[] lines;
        try {
            lines = IOUtils
                    .toString(Omicron.class.getResourceAsStream("/reddit_authentication.txt"), "UTF-8")
                    .split("\n");
        } catch (IOException e) {
            e.printStackTrace();
            cancelled = true;
            return;
        }

        task = new TimerTask() {
            @Override
            public void run() {

                System.out.println("Last checked start: " + lastChecked.toString());

                try {
                    // Create a unique User-Agent for our bot
                    UserAgent userAgent = UserAgent.of("desktop", "sh.okx.omicron", "0.1-SNAPSHOT", lines[0]);
                    RedditClient redditClient = new RedditClient(userAgent);

                    Credentials credentials = Credentials.script(lines[0], lines[1], lines[2], lines[3]);
                    AtomicReference<OAuthData> authData = new AtomicReference<>();
                    try {
                        authData.set(redditClient.getOAuthHelper().easyAuth(credentials));
                    } catch (OAuthException e) {
                        cancelled = true;
                        e.printStackTrace();
                        return;
                    }
                    redditClient.authenticate(authData.get());

                    SubredditReference subreddit = new FluentRedditClient(redditClient)
                            .subreddit(subredditName)
                            .newest();

                    Listing<Submission> fetch = subreddit.fetch();
                    for (int i = fetch.size() - 1; i >= 0; i--) {
                        Submission submission = fetch.get(i);
                        if (submission.getCreated().toInstant().compareTo(lastChecked) > 0) {
                            listeners.forEach(listener -> {
                                listener.handlePrefix();
                                listener.on(subreddit, submission);
                            });
                        }
                    }
                    lastChecked = fetch.get(0).getCreated().toInstant();

                    System.out.println("Last end: " + lastChecked.toString());
                } catch(Exception ex) {
                    cancelled = true;
                    ex.printStackTrace();
                    this.cancel();
                }
            }
        };

        new Timer().scheduleAtFixedRate(task, 0, 120*1000);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
