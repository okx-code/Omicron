package sh.okx.omicron.custom;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

public class MemberPermission {
  private User user = null;
  private Role role = null;

  public MemberPermission(User user) {
    this.user = user;
  }

  public MemberPermission(Role role) {
    this.role = role;
  }

  public MemberPermission() {

  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof MemberPermission)) {
      return false;
    }

    MemberPermission permission = (MemberPermission) o;
    return permission.toLong() == this.toLong();
  }

  public String getReadableAccess() {
    if (user != null) {
      return user.getName() + "#" + user.getDiscriminator();
    } else if (role != null) {
      return role.getName();
    }

    return "anyone";
  }

  public boolean hasPermission(Member check) {
    if (user != null) {
      return check.getUser().getId().equals(user.getId());
    } else if (role != null) {
      return check.getRoles().contains(role);
    }

    return true;
  }

  @Override
  public String toString() {
    return user != null ? user.getId() : role != null ? role.getId() : "";
  }

  public long toLong() {
    return user != null ? user.getIdLong() : role != null ? role.getIdLong() : 0;
  }

  public static MemberPermission fromLong(JDA jda, long value) {
    if (value < 1) {
      return new MemberPermission();
    }

    User user = jda.getUserById(value);
    if (user != null) {
      return new MemberPermission(user);
    }
    return new MemberPermission(jda.getRoleById(value));
  }
}
