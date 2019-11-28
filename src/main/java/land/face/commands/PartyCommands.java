package land.face.commands;

import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import land.face.managers.SnazzyPartiesManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCommands implements CommandExecutor {

  private SnazzyPartiesPlugin plugin;

  public PartyCommands(SnazzyPartiesPlugin plugin) {
    this.plugin = plugin;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

    SnazzyPartiesManager partyManager = plugin.getSnazzyPartiesManager();

    Player player = (Player) sender;

    if (!(sender instanceof Player)) {
      Bukkit.getServer().getLogger().info("Console can not run this command!");
      return false;
    }

    if (args.length == 0 ) {
      player.sendMessage("Invalid Args ... or uh maybe show em the args here?");
      return true;
    }

    Boolean isLeader = partyManager.isLeader(player);
    Party party = partyManager.getParty(player);
    String arg = args[0].toLowerCase();

    switch (arg){
      case "create":
        if (!partyManager.hasParty(player)){
          partyManager.createParty(player);
          return true;
        }
        player.sendMessage("You're already in a party bro?");
        return true;
      case "disband":
        if (!partyCheck(player)){
          return true;
        }
        if (isLeader){
          partyManager.disbandParty(party);
          return true;
        }
        player.sendMessage("Bro? You're not the leader");
        return true;
      case "invite":
        if (!partyCheck(player)){
          return true;
        }
        if (isLeader){
          if (Bukkit.getPlayer(args[1]) == null){
            player.sendMessage(args[1] + " is offline");
            return true;
          }
          Player target = Bukkit.getPlayer(args[1]);
          if (target == player){
            player.sendMessage("BRO? You're the leader and already in the party???");
            return true;
          }
          partyManager.invitePlayer(player, target);
          return true;
        }
        player.sendMessage("Only party leader can run this command");
        return false;
      case "join":
        if (partyCheck(player)){
          player.sendMessage("You're already in a party");
          return true;
        }
        if (partyManager.invitations.get(player.getUniqueId()) != null) {
          partyManager.addPlayer(partyManager.invitations.get(player.getUniqueId()), player);
        }
        else {
          player.sendMessage("You were not invited to any parties");
        }
      case "accept":
        if (partyCheck(player)){
          player.sendMessage("You're already in a party");
          return true;
        }
        if (partyManager.invitations.get(player.getUniqueId()) != null) {
          partyManager.addPlayer(partyManager.invitations.get(player.getUniqueId()), player);
        }
        else {
          player.sendMessage("You were not invited to any parties");
        }
      case "kick":
        if (!partyCheck(player)){
          return true;
        }
        if (isLeader){
          if (Bukkit.getPlayer(args[1]) == null){
            player.sendMessage(args[1] + " is offline");
            return true;
          }
          Player target = Bukkit.getPlayer(args[1]);
          if (target == player){
            player.sendMessage("You cannot kick yourself... Dude if you wanna leave so bad just use /party leave");
            return true;
          }
          if (partyManager.areInSameParty(player, target)){
            partyManager.removePlayer(player, target, Party.RemoveReasons.Kicked);
            return true;
          }
          player.sendMessage("Only party leader can run this command");
          return true;
        }
        return true;
      case "leave":
        if (!partyCheck(player)){
          return true;
        }
        partyManager.removePlayer(party, player, Party.RemoveReasons.Quit);
        return true;
      case "promote":
        if (!partyCheck(player)){
          return true;
        }
        if (isLeader){
          Player target = Bukkit.getPlayer(args[1]);
          if (target == player){
            player.sendMessage("BRO? You're the leader already???");
            return true;
          }
          partyManager.promotePlayer(player);
          return true;
        }
        player.sendMessage("Only party leader can run this command");
        return true;
      case "pvp":
        if (!partyCheck(player)){
          return true;
        }
        if (isLeader){
          party.setFriendlyFire(!party.getFriendlyFire());
          if (party.getFriendlyFire()){
            partyManager.systemSendMessage(party, "friendly fire has been enabled");
            return true;
          }
          partyManager.systemSendMessage(party, "friendly fire has been disabled");
          return true;
        }
        player.sendMessage("Only party leader can run this command");
        return true;
      default:
        if (!partyCheck(player)){
          return true;
        }
        partyManager.sendMessage(player, String.join(" ", args));
    }
    return true;
  }

  public boolean partyCheck(Player player) {
    if (plugin.getSnazzyPartiesManager().hasParty(player)){
      return true;
    }
    player.sendMessage("You need to be in a party to use this command.");
    return false;
  }
}
