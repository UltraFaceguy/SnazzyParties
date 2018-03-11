package me.joshuaemq;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.joshuaemq.commands.FilterCommand;
import me.joshuaemq.data.FilterSetting;
import me.joshuaemq.data.PlayerFilterData;
import me.joshuaemq.listeners.InventoryClickListener;
import me.joshuaemq.listeners.ItemPickupListener;
import me.joshuaemq.listeners.JoinListener;
import me.joshuaemq.managers.FilterGuiManager;
import me.joshuaemq.managers.PlayerFilterManager;
import me.joshuaemq.tasks.SaveTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class TogglePickupsPlugin extends JavaPlugin {

    private PlayerFilterManager playerFilterManager;
    private FilterGuiManager filterGuiManager;

    private SaveTask saveTask;

    private File fileName;
    private FileConfiguration config;
    private FileConfiguration playerFile;

    public void createPlayerFile(Player player) {

        Player p = player.getPlayer();
        fileName = new File(getDataFolder(), p.getUniqueId().toString() + ".yml");

        if (!fileName.exists()) {
            try {
                fileName.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            config = YamlConfiguration.loadConfiguration(fileName);

            try {
                config.save(fileName);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        if (!(getPlayerFilterManager().getPlayerFilterMap().containsKey(p.getUniqueId()))) {

            File playersFile = new File(getDataFolder(), p.getUniqueId().toString() + ".yml");
            playerFile = YamlConfiguration.loadConfiguration(playersFile);

            if (playerFile.get(p.getUniqueId().toString()) == null) {

                PlayerFilterData data = new PlayerFilterData();
                data.setFilterEnabled(playerFile.getBoolean(p.getUniqueId().toString() + ".drops-enabled"));
                data.setLootFilterEntries(new ArrayList<>());
                getPlayerFilterManager().getPlayerFilterMap().put(p.getUniqueId(), data);
            } else {
                PlayerFilterData data = new PlayerFilterData();
                data.setFilterEnabled(playerFile.getBoolean(p.getUniqueId().toString() + ".drops-enabled"));
                List<String> playersLootEntries = playerFile.getStringList(p.getUniqueId().toString() + ".loot-filter-entries");
                List<FilterSetting> playerSettings = new ArrayList<>();
                for (String str : playersLootEntries) {
                    FilterSetting setting = FilterSetting.valueOf(str);
                    playerSettings.add(setting);
                }
                data.setLootFilterEntries(playerSettings);
                getPlayerFilterManager().getPlayerFilterMap().put(p.getUniqueId(), data);
            }

        }
    }

    public void onEnable() {
        playerFilterManager = new PlayerFilterManager();
        filterGuiManager = new FilterGuiManager(this);
        saveTask = new SaveTask(this);

        saveTask.runTaskTimer(this,
                4 * 60 * 20L,
                5 * 60 * 20L
        );

        Bukkit.getPluginManager().registerEvents(new ItemPickupListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);

        this.getCommand("toggledrops").setExecutor(new FilterCommand(this));

        getConfig().options().copyDefaults(true);
        saveConfig();
        reloadConfig();
        loadPlayerData();

        Bukkit.getServer().getLogger().info("Toggleable Drops By Joshuaemq Enabled!");
    }

    public void onDisable() {
        saveTask.saveAll();
        saveTask.cancel();
        HandlerList.unregisterAll(this);
        playerFilterManager = null;
        filterGuiManager = null;
        saveTask = null;

        Bukkit.getServer().getLogger().info("Toggleable Drops By Joshuaemq Disabled!");
    }
    private FileConfiguration playerFile2;

    public void loadPlayerData() {
        if (getDataFolder() != null) {

            for (File file : getDataFolder().listFiles()) {
                playerFile2 = YamlConfiguration.loadConfiguration(file);

                String fileName = file.getName();
                int index = fileName.indexOf(".");
                String str = fileName.substring(0, index);

                if (!(fileName.equals("config.yml"))) {
                    Boolean dropsEnabled = playerFile2.getBoolean(str + ".drops-enabled");
                    List<String> playerFilterEntries = playerFile2.getStringList(str + ".loot-filter-entries");

                    List<FilterSetting> playerSettings = new ArrayList<>();
                    for (String s : playerFilterEntries) {
                        FilterSetting setting = FilterSetting.valueOf(s);
                        playerSettings.add(setting);
                    }

                    PlayerFilterData playerFilterData = new PlayerFilterData();
                    playerFilterData.setFilterEnabled(dropsEnabled);
                    playerFilterData.setLootFilterEntries(playerSettings);

                    UUID uuid = UUID.fromString(str);
                    playerFilterManager.getPlayerFilterMap().put(uuid, playerFilterData);
                }
            }
        }
    }

    public PlayerFilterManager getPlayerFilterManager () {
        return playerFilterManager;
    }
    public FilterGuiManager getFilterGuiManager () {
        return filterGuiManager;
    }
}