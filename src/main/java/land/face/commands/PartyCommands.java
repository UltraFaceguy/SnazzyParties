package land.face.commands;

import java.util.ArrayList;
import java.util.List;
import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import land.face.data.PartyMember;
import land.face.managers.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class PartyCommands implements TabExecutor {

  private SnazzyPartiesPlugin plugin;

  public PartyCommands(SnazzyPartiesPlugin plugin) {
    this.plugin = plugin;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

    PartyManager partyManager = plugin.getPartyManager();

    Player player = (Player) sender;

    if (!(sender instanceof Player)) {
      Bukkit.getServer().getLogger().info("Console can not run this command!");
      return false;
    }

    if (args.length == 0 ) {
      partyHelp(player);
      return true;
    }

    Boolean isLeader = partyManager.isLeader(player);
    Party party = partyManager.getParty(player);
    String arg = args[0].toLowerCase();

    switch (arg){
      case "rename":
        if (!partyCheck(player)) {
          return true;
        }
        if (isLeader) {
          if (args.length < 1) {
            player.sendMessage("Invalid party name");
            return true;
          }
          else {
            args[0] = "";
            String partyName = String.join(" ", args);
            if (partyName.length() > 18) {
              partyName = partyName.substring(0, 17);
            }
            party.setPartyName(partyName);
            party.getScoreboard().getObjective("objective").setDisplayName(partyName);
            return true;
          }
        }
        return true;
      case "help":
        partyHelp(player);
        return true;
      case "create":
        if (!partyManager.hasParty(player)) {
          if (args.length > 1) {
            args[0] = "";
            partyManager.createParty(player, String.join(" ", args));
          } else {
            partyManager.createParty(player);
          }
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
        if (!partyManager.hasParty(player)){
          if (args.length < 2) {
            player.sendMessage("Specify a player to invite");
            return true;
          }
          if (Bukkit.getPlayer(args[1]) != null) {
            partyManager.createParty(player);
            partyManager.invitePlayer(player, Bukkit.getPlayer(args[1]));
            return true;
          }
          return true;
        }
        if (isLeader){
          if (args.length < 2) {
            player.sendMessage("Specify a player to invite");
            return true;
          }
          if (Bukkit.getPlayer(args[1]) == null) {
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
        if (partyManager.hasParty(player)){
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
          if (args.length < 2) {
            player.sendMessage("Specify a player");
            return true;
          }
          if (Bukkit.getPlayer(args[1]) == null){
            player.sendMessage(args[1] + " is offline");
            return true;
          }
          Player target = Bukkit.getPlayer(args[1]);
          if (target == player){
            player.sendMessage("You cannot kick yourself... Dude if you wanna leave so bad just use /party leave");
            return true;
          }
          if (partyManager.areInSameParty(player, target)) {
            partyManager.removePlayer(target, Party.RemoveReasons.Kicked);
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
        partyManager.removePlayer(player, Party.RemoveReasons.Quit);
        return true;
      case "promote":
        if (!partyCheck(player)){
          return true;
        }
        if (isLeader){
          if (args.length < 2) {
            player.sendMessage("Specify a player");
            return true;
          }
          Player target = Bukkit.getPlayer(args[1]);
          if (target == player){
            player.sendMessage("BRO? You're the leader already???");
            return true;
          }
          partyManager.promotePlayer(target);
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
            partyManager.partyAnnounce(party, "friendly fire has been enabled");
            return true;
          }
          partyManager.partyAnnounce(party, "friendly fire has been disabled");
          return true;
        }
        player.sendMessage("Only party leader can run this command");
        return true;
      default:
        if (!partyCheck(player)){
          return true;
        }
        partyManager.partyAnnounce(player, String.join(" ", args));
    }
    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    List<String> list = new ArrayList<>();

    Player player = (Player) sender;
    PartyManager partyManager = plugin.getPartyManager();

    list.add("help");

    if (!(sender instanceof Player)) {
      return null;
    }

    if (args.length > 1) {
      return null;
    }

    if (partyManager.hasParty(player)) {
      Party party = partyManager.getParty(player);
      PartyMember leader = party.getLeader();
      if (leader.getUUID() == player.getUniqueId()) {
        list.add("pvp");
        list.add("promote");
        list.add("kick");
        list.add("invite");
        list.add("disband");
        list.add("leave");
        return list;
      }
      else {
        list.add("leave");
        return list;
      }
    }
    else {
      list.add("create");
      list.add("accept");
      list.add("invite");
      return list;
    }
  }

  private boolean partyCheck(Player player) {
    if (plugin.getPartyManager().hasParty(player)){
      return true;
    }
    player.sendMessage("You need to be in a party to use this command.");
    return false;
  }

  private void partyHelp(Player player) {
    player.sendMessage("/party create");
  }

}
