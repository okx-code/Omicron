package sh.okx.omicron.logging;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.time.OffsetDateTime;

public class CachedMessage {
  private String content;
  private OffsetDateTime time;
  private User author;

  public CachedMessage(Message message) {
    this.content = message.getContentRaw();
    this.time = message.getCreationTime();
    this.author = message.getAuthor();
  }

  public String getContent() {
    return content;
  }

  public OffsetDateTime getTime() {
    return time;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public User getAuthor() {
    return author;
  }
}
