package land.face.listeners;

import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import org.bukkit.Bukkit;
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
    e.getPlayer().setScoreboard(plugin.getPartyManager().getDefaultBoard());
    Party party = plugin.getPartyManager().getParty(e.getPlayer().getUniqueId());
    if (party == null) {
      return;
    }
    Bukkit.getScheduler()
        .runTaskLater(plugin, () -> e.getPlayer().setScoreboard(party.getScoreboard()), 2L);
  }
}
