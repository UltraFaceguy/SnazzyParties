package land.face.timers;

import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import org.bukkit.scheduler.BukkitRunnable;

public class PartyTimer extends BukkitRunnable {

  private Party party;

  public PartyTimer(Party party) {
    this.party = party;
    runTaskTimer(SnazzyPartiesPlugin.getInstance(), 0L, 2L);
  }

  @Override
  public void run() {
    SnazzyPartiesPlugin.getInstance().getPartyManager().updateScoreboard(party);
  }
}
