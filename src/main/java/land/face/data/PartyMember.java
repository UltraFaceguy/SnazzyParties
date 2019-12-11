package land.face.data;

import java.util.UUID;
import org.bukkit.entity.Player;

public class PartyMember {

  private UUID uuid;
  private String username;
  private long quitTimestamp;
  private Boolean scoreboardToggle;

  public PartyMember(Player player) {
    this.uuid = player.getUniqueId();
    this.username = player.getDisplayName();
    this.scoreboardToggle = true;
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

  public Boolean getScoreboardToggle() {
    return scoreboardToggle;
  }

  public void setScoreboardToggle(Boolean scoreboardToggle) {
    this.scoreboardToggle = scoreboardToggle;
  }
}
