package land.face;

import land.face.listeners.DamageListener;
import land.face.commands.PartyCommands;
import land.face.listeners.PlayerJoinListener;
import land.face.listeners.PlayerQuitListener;
import land.face.managers.SnazzyPartiesManager;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class SnazzyPartiesPlugin extends JavaPlugin {

  private static SnazzyPartiesPlugin snazzyPartiesPlugin;
  private SnazzyPartiesManager snazzyPartiesManager;

  public void onEnable() {
    snazzyPartiesPlugin = this;
    snazzyPartiesManager = new SnazzyPartiesManager(this);

    Bukkit.getPluginManager().registerEvents(new DamageListener(this), this);
    Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
    Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

    this.getCommand("party").setExecutor(new PartyCommands(this));

    getConfig().options().copyDefaults(true);
    saveConfig();
    reloadConfig();

    Bukkit.getServer().getLogger().info("Snazzy Parties enabled!");
  }

  public void onDisable() {
    HandlerList.unregisterAll(this);
    Bukkit.getScheduler().cancelTasks(this);
    snazzyPartiesManager.parties.forEach(party -> snazzyPartiesManager.disbandParty(party));
    Bukkit.getServer().getLogger().info("Snazzy Parties disabled!");
  }

  public SnazzyPartiesManager getSnazzyPartiesManager() {
    return  snazzyPartiesManager;
  }

  public static SnazzyPartiesPlugin getInstance() {
    return snazzyPartiesPlugin;
  }
}