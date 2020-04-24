package land.face.data;

import java.util.UUID;
import org.bukkit.entity.Player;

public class PartyMember {

  private UUID uuid;
  private String username;
  private long quitTimestamp;
  private boolean showScoreboard;

  public PartyMember(Player player) {
    this.uuid = player.getUniqueId();
    this.username = player.getDisplayName();
    this.showScoreboard = true;
  }

  public UUID getUUID() {
    return uuid;
  }

  public String getUsername() {
    return username;
  }

  public long getQuitTimestamp() {
    return quitTimestamp;
  }

  public void setQuitTimestamp(long quitTimestamp) {
    this.quitTimestamp = quitTimestamp;
  }

  public boolean isShowScoreboard() {
    return showScoreboard;
  }

  public void setShowScoreboard(boolean showScoreboard) {
    this.showScoreboard = showScoreboard;
  }
}
