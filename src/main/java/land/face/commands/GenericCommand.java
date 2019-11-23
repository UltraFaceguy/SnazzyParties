package land.face.commands;

import land.face.SnazzyPartiesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GenericCommand implements CommandExecutor {

  private SnazzyPartiesPlugin plugin;

  public GenericCommand(SnazzyPartiesPlugin plugin) {
    this.plugin = plugin;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

    Player p = (Player) sender;

    if (!(sender instanceof Player)) {
      Bukkit.getServer().getLogger().info("Console can not run this command!");
      return false;
    }

    if (cmd.getLabel().equalsIgnoreCase("REEEEEEEEEEEEEE")) {
      // epic gaming
      // example pull request
    }
    return true;
  }
}
