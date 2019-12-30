package land.face.commands;

import java.util.ArrayList;
import java.util.List;
import land.face.SnazzyPartiesPlugin;
import land.face.data.Invitation;
import land.face.data.Party;
import land.face.data.PartyMember;
import land.face.data.RemoveReason;
import land.face.managers.PartyManager;
import land.face.utils.Text;
import me.clip.placeholderapi.PlaceholderAPI;
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

    boolean isLeader = partyManager.isLeader(player);
    Party party = partyManager.getParty(player);
    String arg = args[0].toLowerCase();

    switch (arg){
      case "reload":
        if (player.hasPermission(plugin.getSettings().getString("config.permission.reload", "OP"))) {
            plugin.onDisable();
            plugin.onEnable();
        }
        return true;
      case "rename":
        if (!partyCheck(player)) {
          return true;
        }
        if (isLeader) {
          if (args.length < 1) {
            player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.party-invalid-name", "Invalid party name")));
            return true;
          }
          else {
            args[0] = "";
            String partyName = String.join(" ", args);
            int colorLength = partyName.length() - partyName.replaceAll("&([0-9a-fk-or])", "").length();
            if (partyName.length() - colorLength > 18) {
              partyName = partyName.substring(0, 18);
            }
            party.setPartyName(partyName);
            party.getScoreboard().getObjective("objective").setDisplayName(Text.colorize(partyName));
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
        player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.has-party.player", "You're already in a party.")));
        return true;
      case "disband":
        if (!partyCheck(player)){
          return true;
        }
        if (isLeader){
          partyManager.disbandParty(party);
          return true;
        }
        player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.party-not-leader", "Only the part leader can run this command")));
        return true;
      case "invite":
        if (!partyManager.hasParty(player)){
          if (args.length < 2) {
            player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.specify-player", "Specify a player")));
            return true;
          }
          if (Bukkit.getPlayer(args[1]) != null) {
            partyManager.createParty(player);
            player.sendMessage(Text.configHandler(Bukkit.getPlayer(args[1]), plugin.getSettings().getString("config.language.party-invited-player", "%player_name% has been invited to your party.")));
            partyManager.invitePlayer(player, Bukkit.getPlayer(args[1]));
            return true;
          }
          partyManager.createParty(player);
          player.sendMessage(Text.configHandler(Bukkit.getOfflinePlayer(args[1]).getName(), plugin.getSettings().getString("config.language.party-offline-player", "%player_name% is offline and cannot be invited")));
          return true;
        }
        if (isLeader){
          if (args.length < 2) {
            player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.specify-player", "Specify a player")));
            return true;
          }
          if (Bukkit.getPlayer(args[1]) == null) {
            player.sendMessage(Text.configHandler(Bukkit.getOfflinePlayer(args[1]).getName(), plugin.getSettings().getString("config.language.party-offline-player", "%player_name% is offline and cannot be invited")));
            return true;
          }
          Player target = Bukkit.getPlayer(args[1]);
          if (target == player){
            player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.party-already-in-party", "You're already in the party")));
            return true;
          }
          player.sendMessage(Text.configHandler(target, plugin.getSettings().getString("config.language.party-invited-player", "%player_name% has been invited to your party.")));
          partyManager.invitePlayer(player, target);
          return true;
        }
          player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.party-not-leader", "Only the part leader can run this command")));
        return false;
      case "join":
      case "accept":
        if (partyManager.hasParty(player)){
            player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.has-party.player", "You're already in a party.")));
          return true;
        }
        if (partyManager.getInvitations().get(player.getUniqueId()) != null) {
          partyJoin(player, args);
          return true;
        }
        else {
            player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.party-no-invite", "You don't have any party invites.")));
            return true;
        }
      case "kick":
        if (!partyCheck(player)){
          return true;
        }
        if (isLeader){
          if (args.length < 2) {
            player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.specify-player", "Specify a player")));
            return true;
          }
          if (Bukkit.getPlayer(args[1]) == null){
            player.sendMessage(Text.configHandler(Bukkit.getOfflinePlayer(args[1]).getName(), plugin.getSettings().getString("config.language.party-offline-player", "%player_name% is offline and cannot be invited")));
            return true;
          }
          Player target = Bukkit.getPlayer(args[1]);
          if (target == player){
              player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.party-cannot-kick-self", "You cannot kick yourself")));
            return true;
          }
          if (partyManager.areInSameParty(player, target)) {
            partyManager.removePlayer(target, RemoveReason.KICKED);
            return true;
          }
            player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.party-not-leader", "Only the part leader can run this command")));
          return true;
        }
        return true;
      case "leave":
        if (!partyCheck(player)){
          return true;
        }
        partyManager.removePlayer(player, RemoveReason.QUIT);
        return true;
      case "promote":
        if (!partyCheck(player)){
          return true;
        }
        if (isLeader){
          if (args.length < 2) {
            player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.specify-player", "Specify a player")));
            return true;
          }
          Player target = Bukkit.getPlayer(args[1]);
          if (target == player){
              player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.party-already-leader", "You're already the party leader")));
            return true;
          }
          partyManager.promotePlayer(target);
          return true;
        }
          player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.party-not-leader", "Only the part leader can run this command")));
        return true;
      case "pvp":
        if (!partyCheck(player)){
          return true;
        }
        if (isLeader){
          party.setFriendlyFire(!party.getFriendlyFire());
          if (party.getFriendlyFire()){
              partyManager.partyAnnounce(party, Text.configHandler(player, plugin.getSettings().getString("config.language.party-friendly-fire.enabled", "Friendly fire has been enabled")));
              return true;
          }
              partyManager.partyAnnounce(party, Text.configHandler(player, plugin.getSettings().getString("config.language.party-friendly-fire.disabled", "Friendly fire has been disabled")));
          return true;
        }
          player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.party-not-leader", "Only the part leader can run this command")));
        return true;
      case "show":
        if (!partyCheck(player)) {
          return true;
        }
        if (party.getMember(player).getScoreboardToggle()) {
          player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.party-shown", "Your scoreboard is already shown")));
          return true;
        }
        player.setScoreboard(party.getScoreboard());
        party.getMember(player).setScoreboardToggle(true);
        return true;
      case "hide":
        if (!partyCheck(player)) {
          return true;
        }
        if (!party.getMember(player).getScoreboardToggle()) {
          player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.party-hidden", "Your scoreboard is already hidden")));
          return true;
        }
        player.setScoreboard(partyManager.getDefaultBoard());
        party.getMember(player).setScoreboardToggle(false);
        return true;
      default:
        if (!partyCheck(player)){
          return true;
        }
        partyManager.sendPartyMessage(player, String.join(" ", args));
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
      if (party.getMember(player).getScoreboardToggle()) {
        list.add("hide");
      }
      else {
        list.add("show");
      }
      if (leader.getUUID() == player.getUniqueId()) {
        list.add("pvp");
        list.add("promote");
        list.add("kick");
        list.add("invite");
        list.add("disband");
        list.add("leave");
        list.add("rename");
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
    player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.party-no-party", "You must to be in a party to use this command.")));
    return false;
  }

  private void partyHelp(Player player) {
    player.sendMessage("/party create");
  }

  private void partyJoin(Player player, String[] arg) {
      List<Invitation> list = plugin.getPartyManager().getInvitations().get(player.getUniqueId());
      if (list.size() == 0) {
          player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.party-no-invite", "You don't have any party invites.")));
          return;
      }
      if (arg.length < 2) {
        Invitation invite = list.get(list.size()-1);
        partyJoin(player, invite);
        return;
      }
      for (Invitation invite : list) {
          for (PartyMember member : invite.getParty().getMembers()) {
              if (member.getUsername().equalsIgnoreCase(arg[1])) {
                partyJoin(player, invite);
                return;
              }
          }
      }
      player.sendMessage(Text.colorize(PlaceholderAPI.setPlaceholders(player, plugin.getSettings().getString("config.language.party-no-invite", "You don't have any party invites."))));
  }
  private void partyJoin(Player player, Invitation invite) {
      if (!plugin.getPartyManager().isValidInvite(invite)) {
          plugin.getPartyManager().getInvitations().get(player.getUniqueId()).remove(invite);
          player.sendMessage(Text.configHandler(player, plugin.getSettings().getString("config.language.party-invite-expired", "Party invite expired")));
          return;
      }
      plugin.getPartyManager().getInvitations().get(player.getUniqueId()).remove(invite);
      plugin.getPartyManager().addPlayer(invite.getParty(), player);
  }
}
