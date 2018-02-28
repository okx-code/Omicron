package sh.okx.omicron.command;

import net.dv8tion.jda.core.entities.Message;
import sh.okx.omicron.Omicron;

public abstract class Command {
  protected Omicron omicron;
  protected String name;
  protected Category category;
  protected String description;
  protected String[] aliases;

  public Command(Omicron omicron, String name, Category category, String description, String... aliases) {
    this.omicron = omicron;
    this.name = name;
    this.category = category;
    this.description = description;
    this.aliases = aliases;
  }

  public final Omicron getOmicron() {
    return omicron;
  }

  public final String getName() {
    return name;
  }

  public final String[] getAliases() {
    return aliases;
  }

  public final Category getCategory() {
    return category;
  }

  public final String getDescription() {
    return description;
  }

  public abstract void run(Message message, String args);
}
