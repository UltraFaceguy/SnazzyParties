package land.face.data;

import java.util.UUID;
import org.bukkit.entity.Player;

public class PartyMember {

  private Player player;
  private UUID uuid;
  private String username;

  public PartyMember(Player player) {
    this.player = player;
    this.uuid = player.getUniqueId();
    this.username = player.getDisplayName();
  }

  public Player getPlayer() {
    return player;
  }

  public UUID getUUID() {
    return uuid;
  }

  public String getUsername() {
    return username;
  }

}
