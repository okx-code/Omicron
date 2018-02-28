package sh.okx.omicron.feed;

public class Feed {
  private String prefix;
  private FeedType type;
  private String location;
  private String channel;
  private FeedHandler handler;

  public Feed(String prefix, FeedType type, String location, String channel, FeedHandler handler) {
    this.prefix = prefix;
    this.type = type;
    this.location = location;
    this.channel = channel;
    this.handler = handler;
  }

  public String getPrefix() {
    return prefix;
  }

  public FeedType getType() {
    return type;
  }

  public String getLocation() {
    return location;
  }

  public String getChannel() {
    return channel;
  }

  public FeedHandler getHandler() {
    return handler;
  }
}
