package sh.okx.omicron.feed.youtube;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import org.apache.commons.io.IOUtils;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.feed.FeedHandler;

import java.io.IOException;
import java.util.*;

public class YoutubeHandler implements FeedHandler {
    private DateTime lastCheck = new DateTime(System.currentTimeMillis());
    private boolean cancelled;
    private TimerTask task;

    private Set<AbstractYoutubeListener> listeners = new HashSet<>();

    public void addListener(AbstractYoutubeListener listener) {
        listeners.add(listener);
    }

    public YoutubeHandler(String id) {
        task = new TimerTask() {
            @Override
            public void run() {
                YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), request -> {})
                        .setApplicationName("omicron-okx").build();

                try {
                    YouTube.Search.List search = youtube.search().list("id,snippet");
                    search.setKey(IOUtils.toString(Omicron.class.getResourceAsStream("/google_api_key.txt"), "UTF-8"));
                    search.setChannelId(id);
                    search.setOrder("date");
                    search.setMaxResults(20L);
                    search.setType("video");

                    SearchListResponse searchResponse = search.execute();
                    List<SearchResult> results = searchResponse.getItems();
                    for(SearchResult result : results) {
                        SearchResultSnippet snippet = result.getSnippet();
                        if(compare(snippet.getPublishedAt(), lastCheck) <= 0) {
                            continue;
                        }

                        listeners.forEach(listener -> {
                            listener.handlePrefix();
                            listener.on(result.getId(), snippet);
                        });
                    }

                    lastCheck = results.get(0).getSnippet().getPublishedAt();
                } catch (IOException e) {
                    e.printStackTrace();
                    this.cancel();
                    cancelled = true;
                }
            }
        };
    }

    @Override
    public void cancel() {
        cancelled = task.cancel();
    }

    @Override
    public void start() {
        new Timer().scheduleAtFixedRate(task, 0, 120*1000);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    private int compare(DateTime thisTime, DateTime anotherTime) {
        return Long.compare(thisTime.getValue(), anotherTime.getValue());
    }
}