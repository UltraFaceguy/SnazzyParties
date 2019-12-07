package land.face.listeners;

import land.face.SnazzyPartiesPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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
    if (!(e.getEntity() instanceof Player)) {
      return;
    }
    LivingEntity attacker = getAttacker(e.getDamager());
    if (!(attacker instanceof Player)) {
      return;
    }
    if (plugin.getPartyManager().areInSameParty((Player) e.getEntity(), (Player) attacker) && plugin
        .getPartyManager().getParty((Player) e.getEntity()).getFriendlyFire()) {
      e.isCancelled();
    }
  }

  private static LivingEntity getAttacker(Entity entity) {
    if (entity instanceof LivingEntity) {
      return (LivingEntity) entity;
    } else if (entity instanceof Projectile) {
      if (((Projectile) entity).getShooter() instanceof LivingEntity) {
        return (LivingEntity) ((Projectile) entity).getShooter();
      }
    } else if (entity instanceof EvokerFangs) {
      return ((EvokerFangs) entity).getOwner();
    }
    return null;
  }
}
