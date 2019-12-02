package land.face;

import io.pixeloutlaw.minecraft.spigot.config.MasterConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import land.face.commands.PartyCommands;
import land.face.listeners.DamageListener;
import land.face.listeners.PlayerQuitListener;
import land.face.managers.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class SnazzyPartiesPlugin extends JavaPlugin {

  private static SnazzyPartiesPlugin snazzyPartiesPlugin;
  private PartyManager partyManager;

  private MasterConfiguration settings;
  private VersionedSmartYamlConfiguration configYAML;

  public void onEnable() {
    snazzyPartiesPlugin = this;

    List<VersionedSmartYamlConfiguration> configurations = new ArrayList<>();
    configurations.add(configYAML = defaultSettingsLoad("config.yml"));

    for (VersionedSmartYamlConfiguration config : configurations) {
      if (config.update()) {
        getLogger().info("Updating " + config.getFileName());
      }
    }

    settings = MasterConfiguration.loadFromFiles(configYAML);

    partyManager = new PartyManager(this);

    Bukkit.getPluginManager().registerEvents(new DamageListener(this), this);
    Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);

    PartyCommands partyCommands = new PartyCommands(this);
    this.getCommand("party").setExecutor(partyCommands);
    this.getCommand("party").setTabCompleter(partyCommands);

    getConfig().options().copyDefaults(true);
    saveConfig();
    reloadConfig();

    Bukkit.getServer().getLogger().info("Snazzy Parties enabled!");
  }

  public void onDisable() {
    HandlerList.unregisterAll(this);
    Bukkit.getScheduler().cancelTasks(this);
    partyManager.parties.forEach(party -> partyManager.disbandParty(party));
    Bukkit.getServer().getLogger().info("Snazzy Parties disabled!");
  }

  private VersionedSmartYamlConfiguration defaultSettingsLoad(String name) {
    return new VersionedSmartYamlConfiguration(new File(getDataFolder(), name),
        getResource(name), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
  }

  public MasterConfiguration getSettings() {
    return settings;
  }

  public PartyManager getPartyManager() {
    return partyManager;
  }

  public static SnazzyPartiesPlugin getInstance() {
    return snazzyPartiesPlugin;
  }
}