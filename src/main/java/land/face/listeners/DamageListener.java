package land.face.listeners;

import land.face.SnazzyPartiesPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

  private SnazzyPartiesPlugin plugin;

  public DamageListener(SnazzyPartiesPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onDamage(EntityDamageByEntityEvent e) {
      if (e.getDamager() instanceof Player && e.getEntity() instanceof Player){
          Player attacker = (Player) e.getDamager();
          Player victim = (Player) e.getEntity();
          if (plugin.getSnazzyPartiesManager().areInSameParty(attacker, victim) && plugin.getSnazzyPartiesManager().getParty(victim).getFriendlyFire()){
              e.isCancelled();
          }

      }

  }

}
