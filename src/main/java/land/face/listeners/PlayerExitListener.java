package land.face.listeners;

import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerExitListener implements Listener {

  private SnazzyPartiesPlugin plugin;

  public PlayerExitListener(SnazzyPartiesPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    Party party = plugin.getPartyManager().getParty(e.getPlayer());
    if (party == null) {
      return;
    }
    party.getMember(e.getPlayer()).setQuitTimestamp(System.currentTimeMillis());
  }

  @EventHandler
  public void onKick(PlayerKickEvent e) {
    Party party = plugin.getPartyManager().getParty(e.getPlayer());
    if (party == null) {
      return;
    }
    party.getMember(e.getPlayer()).setQuitTimestamp(System.currentTimeMillis());
  }
}
