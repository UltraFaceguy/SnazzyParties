package land.face;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import co.aikar.commands.BukkitCommandManager;
import land.face.commands.PartyCommands;
import land.face.data.Invitation;
import land.face.data.Party;
import land.face.data.PartyMember;
import land.face.listeners.ChatListener;
import land.face.listeners.DamageListener;
import land.face.listeners.PlayerExitListener;
import land.face.listeners.PlayerJoinListener;
import land.face.managers.PartyManager;
import land.face.tasks.OfflineKickerTask;
import land.face.utils.config.MasterConfiguration;
import land.face.utils.config.VersionedConfiguration;
import land.face.utils.config.VersionedSmartYamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class SnazzyPartiesPlugin extends JavaPlugin {

  private static SnazzyPartiesPlugin instance;
  private PartyManager partyManager;

  private MasterConfiguration settings;
  private VersionedSmartYamlConfiguration configYAML;

  public static SnazzyPartiesPlugin getInstance() {
    return instance;
  }

  public SnazzyPartiesPlugin() {
    instance = this;
  }

  public void onEnable() {
    instance = this;
    List<VersionedSmartYamlConfiguration> configurations = new ArrayList<>();
    configurations.add(configYAML = defaultSettingsLoad("config.yml"));

    for (VersionedSmartYamlConfiguration config : configurations) {
      if (config.update()) {
        getLogger().info("Updating " + config.getFileName());
      }
    }

    settings = MasterConfiguration.loadFromFiles(configYAML);

    partyManager = new PartyManager(this);

    Bukkit.getPluginManager().registerEvents(new DamageListener(partyManager), this);
    Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
    Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    Bukkit.getPluginManager().registerEvents(new PlayerExitListener(this), this);

    OfflineKickerTask kickerTask = new OfflineKickerTask(partyManager);
    kickerTask.runTaskTimer(this, 20L, 100);

    BukkitCommandManager commandManager = new BukkitCommandManager(this);
    commandManager.registerCommand(new PartyCommands(this, partyManager));

    commandManager.getCommandCompletions().registerCompletion("partyInvites", c -> {
      CommandSender sender = c.getSender();
      if (sender instanceof Player) {
        Player player = (Player) sender;
        return partyManager.getInvitations(player.getUniqueId())
                .stream()
                .map(Invitation::getParty)
                .map(Party::getLeader)
                .map(PartyMember::getUsername)
                .collect(Collectors.toList());
      }
      return null;
    });

    commandManager.getCommandCompletions().registerCompletion("partyMembers", c -> {
      CommandSender sender = c.getSender();
      if (sender instanceof Player) {
        Player player = (Player) sender;
        return partyManager.getParty(player.getUniqueId()).getMembers()
                .stream()
                .map(PartyMember::getUsername)
                .filter(s -> !s.equalsIgnoreCase(player.getName()))
                .collect(Collectors.toList());
      }
      return null;
    });

    Bukkit.getServer().getLogger().info("Snazzy Parties enabled!");
  }

  public void onDisable() {
    HandlerList.unregisterAll(this);
    Bukkit.getScheduler().cancelTasks(this);
    for (int i = 0; i < partyManager.getParties().size(); i++) {
      partyManager.disbandParty(partyManager.getParties().get(i));
    }
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

}