package land.face.listeners;

import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import land.face.data.PartyMember;
import org.bukkit.entity.Player;
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
  public void onQuit(PlayerQuitEvent event) {
    doLeaveStuff(event.getPlayer());
  }

  @EventHandler
  public void onKick(PlayerKickEvent event) {
    doLeaveStuff(event.getPlayer());
  }

  private void doLeaveStuff(Player player) {
    Party party = plugin.getPartyManager().getParty(player);
    if (party == null) {
      return;
    }
    PartyMember member = party.getMember(player);
    member.setQuitTimestamp(System.currentTimeMillis());
    member.setShowScoreboard(true);
  }
}
