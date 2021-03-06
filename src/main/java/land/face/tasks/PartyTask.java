package land.face.tasks;

import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import org.bukkit.scheduler.BukkitRunnable;

public class PartyTask extends BukkitRunnable {

  private Party party;

  public PartyTask(Party party) {
    this.party = party;
    runTaskTimer(SnazzyPartiesPlugin.getInstance(), 0L, 2L);
  }

  @Override
  public void run() {
    SnazzyPartiesPlugin.getInstance().getPartyManager().updateScoreboard(party);
  }
}
