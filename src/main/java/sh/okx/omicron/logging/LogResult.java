package sh.okx.omicron.logging;

import net.dv8tion.jda.core.entities.TextChannel;

public class LogResult {
  private boolean success;
  private TextChannel channel;

  public LogResult(boolean success, TextChannel channel) {
    this.success = success;
    this.channel = channel;
  }

  public boolean isSuccess() {
    return success;
  }

  public TextChannel getChannel() {
    return channel;
  }
}
