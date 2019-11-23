package land.face.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.data.ObjectStuff;

public class PlayerFilterManager {

  // Managers are where you do stuff that would normally be static, tbh

  private Map<UUID, ObjectStuff> playerFilterMap;

  public PlayerFilterManager() {
    this.playerFilterMap = new HashMap<>();
  }

  public Map<UUID, ObjectStuff> getPlayerFilterMap() {
    return playerFilterMap;
  }
}
