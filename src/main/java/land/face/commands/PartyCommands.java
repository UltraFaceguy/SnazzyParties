package land.face.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import land.face.SnazzyPartiesPlugin;
import land.face.data.Invitation;
import land.face.data.Party;
import land.face.data.PartyMember;
import land.face.data.RemoveReason;
import land.face.managers.PartyManager;
import land.face.utils.Text;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("party|p")
public class PartyCommands extends BaseCommand {

  private final SnazzyPartiesPlugin plugin;
  private final PartyManager partyManager;

  public PartyCommands(SnazzyPartiesPlugin plugin, PartyManager partyManager) {
    this.plugin = plugin;
    this.partyManager = partyManager;
  }

  @Default
  @Subcommand("chat|msg")
  public void onChat(Player player, String message) {
    if (!partyCheck(player, false)) return;
    partyManager.sendPartyMessage(player, message);
  }

  @Subcommand("help") @HelpCommand
  public void onHelp(Player player) {
    //TODO
  }

  @Subcommand("reload")
  public void onReload(Player player) {
    if (player.hasPermission(plugin.getSettings().getString("config.permission.reload", "OP"))) {
      plugin.onDisable();
      plugin.onEnable();
    }
  }

  @Subcommand("rename")
  public void onRename(Player player, String partyName) {
    if (!partyCheck(player, true) || !isLeader(player, true)) return;
    int colorLength = partyName.length() - partyName.replaceAll("&([0-9a-fk-or])", "").length();
    if (partyName.length() - colorLength > 18) partyName = partyName.substring(0, 18);
    partyManager.getParty(player).setPartyName(partyName);
  }

  @Subcommand("create")
  public void onCreate(Player player, @Optional String partyName) {
    if (partyCheck(player, false)) {
      player.sendMessage(Text.configHandler(player, plugin.getSettings()
              .getString("config.language.has-party.player", "You're already in a party.")));
      return;
    }
    if (partyName == null) {
      partyManager.createParty(player);
      return;
    }
    partyManager.createParty(player, partyName);
  }

  @Subcommand("disband")
  public void onDisband(Player player) {
    if (isLeader(player, true)) partyManager.disbandParty(partyManager.getParty(player));
  }

  @Subcommand("invite")
  public void onInvite(Player player, OnlinePlayer target) {
    if (partyCheck(player, false) && !isLeader(player, true)) return;
    if (partyCheck(target.getPlayer(), false)) {
      player.sendMessage(Text.configHandler(player, plugin.getSettings()
              .getString("config.language.has-party.target",
                      "They're already in the party")));
      return;
    }

    if (!partyCheck(player, false)) {
      partyManager.createParty(player);
      if (target.getPlayer() != player) {
        player.sendMessage(Text.configHandler(target.getPlayer(), plugin.getSettings()
                .getString("config.language.party-invited-player",
                        "%player_name% has been invited to your party.")));
        partyManager.invitePlayer(player, target.getPlayer());
      }
    }
    else if (isLeader(player, false)) {
      if (target.getPlayer() == player) {
        player.sendMessage(Text.configHandler(player, plugin.getSettings()
                .getString("config.language.party-already-in-party",
                        "You're already in the party")));
      }
      else {
        player.sendMessage(Text.configHandler(target.getPlayer(), plugin.getSettings()
                .getString("config.language.party-invited-player",
                        "%player_name% has been invited to your party.")));
        partyManager.invitePlayer(player, target.getPlayer());
      }
    }
  }

  @Subcommand("invites")
  public void onInvites(Player player) {
    if (partyCheck(player, false)) {
      player.sendMessage(Text.configHandler(player, plugin.getSettings()
              .getString("config.language.has-party.player", "You're already in a party.")));
      return;
    }
    if (partyManager.getInvitations().get(player.getUniqueId()) == null) {
      player.sendMessage(Text.configHandler(player, plugin.getSettings()
              .getString("config.language.party-no-invite", "You don't have any party invites.")));
      return;
    }
    List<Invitation> list = plugin.getPartyManager().getInvitations().get(player.getUniqueId());
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < list.size(); i++) {
      sb.append("(").append((i + 1)).append(") ")
              .append(list.get(i).getParty().getLeader().getUsername()).append(", ");
    }
    String string = String.valueOf(sb);
    player.sendMessage(string.substring(0, string.length() - 2));
  }

  @Subcommand("join|accept")
  public void onJoin(Player player, @Optional String inviteName) {
    if (partyCheck(player, false)) {
      player.sendMessage(Text.configHandler(player, plugin.getSettings()
              .getString("config.language.has-party.player", "You're already in a party.")));
      return;
    }

    List<Invitation> list = plugin.getPartyManager().getInvitations().get(player.getUniqueId());
    if (list == null || list.isEmpty()) {
      player.sendMessage(Text.configHandler(player, plugin.getSettings()
              .getString("config.language.party-no-invite", "You don't have any party invites.")));
      return;
    }
    if (inviteName == null || inviteName.equals("")) {
      Invitation invite = list.get(list.size() - 1);
      partyJoin(player, invite);
      return;
    }
    for (Invitation invite : list) {
      for (PartyMember member : invite.getParty().getMembers()) {
        if (member.getUsername().equalsIgnoreCase(inviteName)) {
          partyJoin(player, invite);
          return;
        }
      }
    }
    player.sendMessage(Text.colorize(PlaceholderAPI.setPlaceholders(player, plugin.getSettings()
            .getString("config.language.party-no-invite", "You don't have any party invites."))));
  }
  private void partyJoin(Player player, Invitation invite) {
    if (!plugin.getPartyManager().isValidInvite(invite)) {
      plugin.getPartyManager().getInvitations().get(player.getUniqueId()).remove(invite);
      player.sendMessage(Text.configHandler(player, plugin.getSettings()
              .getString("config.language.party-invite-expired", "Party invite expired")));
      return;
    }
    plugin.getPartyManager().getInvitations().get(player.getUniqueId()).remove(invite);
    plugin.getPartyManager().addPlayer(invite.getParty(), player);
  }

  @Subcommand("reject|decline")
  public void onReject(Player player, String inviteName) {
    if (partyManager.hasParty(player)) {
      player.sendMessage(Text.configHandler(player, plugin.getSettings()
              .getString("config.language.has-party.player", "You're already in a party.")));
      return;
    }
    else if (partyManager.getInvitations().get(player.getUniqueId()) == null) {
      player.sendMessage(Text.configHandler(player, plugin.getSettings()
              .getString("config.language.party-no-invite", "You don't have any party invites.")));
      return;
    }

    List<Invitation> list2 = partyManager.getInvitations().get(player.getUniqueId());
    if (inviteName == null || inviteName.equals("")) {
      Invitation invite = list2.get(list2.size() - 1);
      player.sendMessage(Text.configHandler(player, plugin.getSettings()
              .getString("config.language.party-deny.receiver",
                      "That wasn't very friendly of you... :(")));
      if (Bukkit.getPlayer(invite.getInviter()) != null) {
        Bukkit.getPlayer(invite.getInviter()).sendMessage(Text.configHandler(player,
                plugin.getSettings().getString("config.language.party-deny.sender",
                        "%player_name% rejected your invite... :(")));
      }
      list2.remove(invite);
      partyManager.getInvitations().put(player.getUniqueId(), list2);
      return;
    }
    for (Invitation invite : list2) {
      for (PartyMember member : invite.getParty().getMembers()) {
        if (member.getUsername().equalsIgnoreCase(inviteName)) {
          player.sendMessage(Text.configHandler(player, plugin.getSettings()
                  .getString("config.language.party-deny.receiver",
                          "That wasn't very friendly of you... :(")));
          if (Bukkit.getPlayer(invite.getInviter()) != null) {
            Bukkit.getPlayer(invite.getInviter()).sendMessage(Text.configHandler(player,
                    plugin.getSettings().getString("config.language.party-deny.sender",
                            "%player_name% rejected your invite... :(")));
          }
          list2.remove(invite);
          partyManager.getInvitations().put(player.getUniqueId(), list2);
          return;
        }
      }
    }
  }

  @Subcommand("kick")
  public void onKick(Player player, OfflinePlayer target) {
    if (!partyCheck(player, true) || !isLeader(player, true)) return;
    if (target == player) {
      player.sendMessage(Text.configHandler(player, plugin.getSettings()
              .getString("config.language.party-cannot-kick-self", "You cannot kick yourself")));
      return;
    }
    if (partyManager.areInSameParty(player, target.getPlayer())) {
      partyManager.removePlayer(Objects.requireNonNull(target.getPlayer()), RemoveReason.KICKED);
    }
  }

  @Subcommand("leave")
  public void onLeave(Player player) {
    if (!partyCheck(player, true)) return;
    partyManager.removePlayer(player, RemoveReason.QUIT);
  }

  @Subcommand("promote")
  public void onPromote(Player player, OnlinePlayer target) {
    if (!partyCheck(player, true) || !isLeader(player, true)) return;

    if (target.getPlayer() == player) {
      player.sendMessage(Text.configHandler(player, plugin.getSettings()
              .getString("config.language.party-already-leader",
                      "You're already the party leader")));
      return;
    }

    if (partyManager.areInSameParty(player, target.getPlayer())) partyManager.promotePlayer(target.getPlayer());
  }

  @Subcommand("pvp")
  public void onToggleFriendlyFire(Player player) {
    if (!partyCheck(player, true) || !isLeader(player, true)) return;

    Party party = partyManager.getParty(player);
    party.setFriendlyFire(!party.getFriendlyFire());

    if (party.getFriendlyFire()) {
      partyManager.partyAnnounce(party, Text.configHandler(player, plugin.getSettings()
              .getString("config.language.party-friendly-fire.enabled",
                      "Friendly fire has been enabled")));
    }
    else {
      partyManager.partyAnnounce(party, Text.configHandler(player, plugin.getSettings()
              .getString("config.language.party-friendly-fire.disabled",
                      "Friendly fire has been disabled")));
    }
  }

  @Subcommand("toggle|toggleboard")
  public void onToggleScoreboard(Player player, @Optional Boolean mode) {
    if (!partyCheck(player, true)) return;
    Party party = partyManager.getParty(player);

    boolean currentMode = party.getMember(player).isShowScoreboard();
    if (mode == null) mode = !currentMode;

    if (mode) {
      if (currentMode) {
        player.sendMessage(Text.configHandler(player, plugin.getSettings()
                .getString("config.language.party-shown", "Your scoreboard is already shown")));
        return;
      }
      player.setScoreboard(party.getScoreboard());
      party.getMember(player).setShowScoreboard(true);
      return;
    }

    if (!currentMode) {
      player.sendMessage(Text.configHandler(player, plugin.getSettings()
              .getString("config.language.party-hidden", "Your scoreboard is already hidden")));
      return;
    }
    player.setScoreboard(partyManager.getDefaultBoard());
    party.getMember(player).setShowScoreboard(false);
  }

  @Subcommand("show|showboard")
  public void onShowScoreboard(Player player) {
    onToggleScoreboard(player, true);
  }

  @Subcommand("hide|hideboard")
  public void onHideScoreboard(Player player) {
    onToggleScoreboard(player, false);
  }

  //broken
  public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    List<String> list = new ArrayList<>();

    Player player = (Player) sender;
    PartyManager partyManager = plugin.getPartyManager();

    list.add("help");

    if (sender == null) return null;

    if (args.length > 1) {
      return null;
    }

    if (partyManager.hasParty(player)) {
      Party party = partyManager.getParty(player);
      PartyMember leader = party.getLeader();
      if (party.getMember(player).isShowScoreboard()) {
        list.add("hide");
      } else {
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
      } else {
        list.add("leave");
        return list;
      }
    } else {
      list.add("create");
      list.add("accept");
      list.add("invite");
      list.add("invites");
      return list;
    }
  }

  private boolean isLeader(Player player, boolean sendMessage) {
    if (partyManager.isLeader(player)) return true;
    if (!sendMessage) return false;
    player.sendMessage(Text.configHandler(player, plugin.getSettings()
            .getString("config.language.party-not-leader",
                    "Only the part leader can run this command")));
    return false;
  }

  private boolean partyCheck(Player player, boolean sendMessage) {
    if (partyManager.hasParty(player)) return true;
    if (!sendMessage) return false;
    player.sendMessage(Text.configHandler(player, plugin.getSettings()
        .getString("config.language.party-no-party",
            "You must to be in a party to use this command.")));
    return false;
  }
}
