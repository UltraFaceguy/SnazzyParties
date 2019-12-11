package land.face.listeners;

import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

  private SnazzyPartiesPlugin plugin;

  public PlayerJoinListener(SnazzyPartiesPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    Party party = plugin.getPartyManager().getParty(e.getPlayer().getUniqueId());
    if (party == null) {
      return;
    }
    if (party.getMember(e.getPlayer().getUniqueId()).getScoreboardToggle()) {
      e.getPlayer().setScoreboard(party.getScoreboard());
    }
  }
}
