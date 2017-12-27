package sh.okx.omicron.logging;

import java.time.OffsetDateTime;

public class CachedMessage {
    private long id;
    private String oldContent;
    private OffsetDateTime time;

    public CachedMessage(long id, String oldContent, OffsetDateTime time) {
        this.id = id;
        this.oldContent = oldContent;
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public String getOldContent() {
        return oldContent;
    }

    public OffsetDateTime getTime() {
        return time;
    }

    public void setOldContent(String oldContent) {
        this.oldContent = oldContent;
    }
}
