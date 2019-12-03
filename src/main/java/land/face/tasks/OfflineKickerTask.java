package land.face.tasks;

import land.face.managers.PartyManager;
import org.bukkit.scheduler.BukkitRunnable;

public class OfflineKickerTask extends BukkitRunnable {

  private PartyManager partyManager;

  public OfflineKickerTask(PartyManager partyManager) {
    this.partyManager = partyManager;
  }

  @Override
  public void run() {
    partyManager.tickOfflinePartyMembers();
  }
}
