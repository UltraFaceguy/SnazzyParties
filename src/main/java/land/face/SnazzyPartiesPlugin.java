package land.face;

import land.face.listeners.InventoryClickListener;
import land.face.commands.GenericCommand;
import land.face.managers.PlayerFilterManager;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class SnazzyPartiesPlugin extends JavaPlugin {

  private PlayerFilterManager playerFilterManager;

  public void onEnable() {
    playerFilterManager = new PlayerFilterManager();

    Bukkit.getPluginManager().registerEvents(new InventoryClickListener(this), this);

    this.getCommand("genericcommand").setExecutor(new GenericCommand(this));

    getConfig().options().copyDefaults(true);
    saveConfig();
    reloadConfig();

    Bukkit.getServer().getLogger().info("Snazzy Parties enabled!");
  }

  public void onDisable() {
    HandlerList.unregisterAll(this);
    Bukkit.getServer().getLogger().info("Snazzy Parties disabled!");
  }

  // This can be accessed from any manager that accepts the plugin as a param
  // Useful to call other managers from within one
  public PlayerFilterManager getPlayerFilterManager() {
    return playerFilterManager;
  }
}