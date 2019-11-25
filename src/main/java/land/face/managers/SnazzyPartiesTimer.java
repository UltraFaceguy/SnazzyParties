package land.face.managers;

import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import org.bukkit.scheduler.BukkitRunnable;

public class SnazzyPartiesTimer extends BukkitRunnable {

    Party party;

    public SnazzyPartiesTimer(Party party) {
        this.party = party;
        runTaskTimer(SnazzyPartiesPlugin.getInstance(), 0L, 2L);
    }

    @Deprecated
    @Override
    public void run() {
        SnazzyPartiesPlugin.getInstance().getSnazzyPartiesManager().updateScoreboard(party);
    }
}
