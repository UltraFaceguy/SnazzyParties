package land.face.listeners;

import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import land.face.managers.SnazzyPartiesManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerQuitListener implements Listener {

    private SnazzyPartiesPlugin plugin;

    public PlayerQuitListener(SnazzyPartiesPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {

        SnazzyPartiesManager manager = plugin.getSnazzyPartiesManager();
        Party party = manager.getParty(e.getPlayer());

        if (manager.hasParty(e.getPlayer())){
            if (manager.isLeader(e.getPlayer()) && party.getPartySize() > 1){
                manager.promoteNextInLine(party);
            }
        }

        new BukkitRunnable() {
            public void run() {
                if (!e.getPlayer().isOnline()){
                    manager.removePlayer(party, e.getPlayer(), Party.RemoveReasons.TimeOut);
                }
            }
        }.runTaskLater(this.plugin, 600);
    }
}
