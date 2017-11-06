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
    private DateTime lastCheck = new DateTime(System.currentTimeMillis()-10000000);
    private TimerTask task;

    private Set<AbstractYoutubeListener> listeners = new HashSet<>();

    public void addListener(AbstractYoutubeListener listener) {
        listeners.add(listener);
    }

    public YoutubeHandler(String id) {
        YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), request -> {})
                .setApplicationName("omicron-okx").build();
        task = new TimerTask() {
            @Override
            public void run() {
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

                    lastCheck = results.get(results.size()-1).getSnippet().getPublishedAt();
                } catch (IOException e) {
                    e.printStackTrace();
                    this.cancel();
                    return;
                }
            }
        };
    }

    @Override
    public void cancel() {
        task.cancel();
    }

    @Override
    public void start() {
        new Timer().scheduleAtFixedRate(task, 0, 120*1000);
    }

    private int compare(DateTime thisTime, DateTime anotherTime) {
        return Long.compare(thisTime.getValue(), anotherTime.getValue());
    }
}