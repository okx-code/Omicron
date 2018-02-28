package sh.okx.omicron.minecraft;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import sh.okx.omicron.Omicron;
import sh.okx.omicron.command.Category;
import sh.okx.omicron.command.Command;

import java.util.HashMap;
import java.util.Map;

public class TokenCommand extends Command {
  private Map<String, String> tokens = new HashMap<>();

  public TokenCommand(Omicron omicron) {
    super(omicron, "token", Category.MISC,
        "Link your discord account with your Minecraft account." +
            "Connect to `mc.okx.sh` to get a token and do " +
            "**o/token <token>** to verify your Minecraft account.");

    subscribe();
  }

  private void subscribe() {
    Jedis subscriber = new Jedis("localhost", 6379);
    subscriber.connect();

    new Thread(() -> subscriber.subscribe(new JedisPubSub() {
      @Override
      public void onMessage(String channel, String message) {
        String[] parts = message.split(" ");
        tokens.put(parts[1], parts[0]);
      }
    }, "token")).start();
  }

  @Override
  public void run(Message message, String content) {
    MessageChannel channel = message.getChannel();

    if (content.isEmpty()) {
      channel.sendMessage(description).queue();
      return;
    }

    String name = tokens.remove(content);
    if (name == null) {
      channel.sendMessage("Invalid token!").queue();
      return;
    }

    omicron.getMinecraftManager().setUsername(message.getAuthor().getIdLong(), name);
    channel.sendMessage("Linked Minecraft account: " + name).queue();
  }
}
