package land.face.listeners;

import land.face.SnazzyPartiesPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

  private SnazzyPartiesPlugin plugin;

  public PlayerJoinListener(SnazzyPartiesPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onJoin(PlayerJoinEvent e) {
    if (plugin.getPartyManager().hasParty(e.getPlayer())) {
      plugin.getPartyManager().addToScoreboard(e.getPlayer());
    }
  }
}
