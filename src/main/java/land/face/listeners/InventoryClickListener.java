package land.face.listeners;

import land.face.SnazzyPartiesPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

  private SnazzyPartiesPlugin plugin;

  public InventoryClickListener(SnazzyPartiesPlugin plugin) {
    this.plugin = plugin;
  }

  // Listener setup
  @EventHandler(priority = EventPriority.LOWEST)
  public void onInvyClick(InventoryClickEvent e) {
    // Hey look we used a manager from within a listener
    plugin.getPlayerFilterManager().getPlayerFilterMap();
  }

}
