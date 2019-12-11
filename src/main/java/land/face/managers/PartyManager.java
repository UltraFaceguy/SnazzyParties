package land.face.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.SnazzyPartiesPlugin;
import land.face.data.Invitation;
import land.face.data.Party;
import land.face.data.Party.RemoveReasons;
import land.face.data.PartyMember;
import land.face.utils.Text;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PartyManager {

  private SnazzyPartiesPlugin plugin;

  private Map<Integer, String> partyBoardKeys = new HashMap<>();
  private Scoreboard defaultBoard;
  private int maxOfflineMillis;
  private int maxInviteMillis;

  private List<Party> parties;
  private HashMap<UUID, List<Invitation>> invitations;

  private String leaderPrefix;
  private String nameFormat;
  private String offlineNameFormat;
  private String infoFormat;
  private String offlineInfoFormat;
  private String borderFormat;

  public PartyManager(SnazzyPartiesPlugin plugin) {
    this.plugin = plugin;
    this.parties = new ArrayList<>();
    this.invitations = new HashMap<>();
    maxOfflineMillis = plugin.getSettings()
        .getInt("config.offline-timeout-milliseconds", 300000);
    maxInviteMillis = plugin.getSettings()
        .getInt("config.invite-timeout-milliseconds", 60000);
    leaderPrefix = plugin.getSettings()
        .getString("config.scoreboard.leader-prefix", "★");
    nameFormat = plugin.getSettings()
        .getString("config.scoreboard.name-line", "%player_displayname%");
    offlineNameFormat = plugin.getSettings()
        .getString("config.scoreboard.offline-name-line", "%player_displayname%");
    infoFormat = plugin.getSettings()
        .getString("config.scoreboard.data-line", "&c%player_health_rounded%❤");
    offlineInfoFormat = plugin.getSettings()
        .getString("config.scoreboard.offline-data-line", "&c%player_health_rounded%❤");
    borderFormat = plugin.getSettings().getString("config.scoreboard.border-line", "++++++++++++");

    Map<Integer, ChatColor> chatColorOrdinals = new HashMap<>();
    for (ChatColor chatColor : ChatColor.values()) {
      chatColorOrdinals.put(chatColor.ordinal(), chatColor);
    }

    for (int i = 16; i >= 0; i--) {
      partyBoardKeys.put(i, ChatColor.BLACK + "" + chatColorOrdinals.get(i));
    }

    defaultBoard = Bukkit.getScoreboardManager().getNewScoreboard();
    Objective objective = defaultBoard.registerNewObjective("blank", "blank");
    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
  }

  public List<Party> getParties() {
    return parties;
  }

  public HashMap<UUID, List<Invitation>> getInvitations() {
    return invitations;
  }

  public void partyAnnounce(Player player, String message) {
    Party party = getParty(player);
    if (party == null) {
      return;
    }
    partyAnnounce(party, player.getDisplayName() + ": " + message);
  }

  public void partyAnnounce(Party party, String message) {
    for (PartyMember player : getOnlinePartyMembers(party)) {
      Bukkit.getPlayer(player.getUUID()).sendMessage(TextUtils.color(party.getPrefix() + message));
    }
  }

  public void createParty(Player player) {
    createParty(player, "Party");
  }

  public void createParty(Player player, String name) {
    int colorLength = name.length() - name.replaceAll("&([0-9a-fk-or])", "").length();
    if (name.length() - colorLength > 18) {
      name = name.substring(0, 17);
    }
    Party party = new Party(new PartyMember(player), setupScoreboard(name), name);
    parties.add(party);
    player.setScoreboard(party.getScoreboard());
    player.sendMessage(Text.colorize(PlaceholderAPI.setPlaceholders(player, plugin.getSettings().getString("config.message.create", "Congrats boss your party has been created"))));
  }

  public void disbandParty(Party party) {
    partyAnnounce(party, plugin.getConfig().getString("config.message.disband", "Your party has been disbanded"));
    getOnlinePartyMembers(party)
        .forEach(player -> Bukkit.getPlayer(player.getUsername()).setScoreboard(defaultBoard));
    party.getPartyTask().cancel();
    parties.remove(party);
  }

  public boolean addPlayer(Party party, Player player) {
    if (party.getPartySize() >= party.getMaxPartySize()) {
      return false;
    }
    party.getMembers().add(new PartyMember(player));
    resetScoreboard(party);
    addToScoreboard(player);
    return true;
  }

  public void removePlayer(Player target, RemoveReasons reason) {
    removePlayer(target.getUniqueId(), reason);
  }

  public void removePlayer(UUID uuid, RemoveReasons reason) {
    Party party = getParty(uuid);
    if (party.getPartySize() == 1) {
      disbandParty(party);
      return;
    }
    if (party.getLeader().getUUID().equals(uuid)) {
      promoteNextInLine(party);
      partyAnnounce(party, Text.colorize(PlaceholderAPI.setPlaceholders(Bukkit.getPlayer(party.getLeader().getUUID()), plugin.getSettings().getString("config.message.new-leader", "&f%player_name% is now the leader of the party!"))));
    }
    partyAnnounce(party, PlaceholderAPI.setPlaceholders(Bukkit.getPlayer(uuid), Text.colorize(reason.getMessage())));
    resetScoreboard(party);
    party.getMembers().remove(party.getMember(uuid));
    Player player = Bukkit.getPlayer(uuid);
    if (player != null && player.isValid()) {
      player.setScoreboard(defaultBoard);
    }
  }

  public void invitePlayer(Player cmdSender, Player target) {
    if (hasParty(target)) {
      cmdSender.sendMessage(Text.colorize(PlaceholderAPI.setPlaceholders(cmdSender, plugin.getSettings().getString("config.message.has-party.target", "They're already in a party"))));
      return;
    }
    Party party = getParty(cmdSender);
    invitePlayer(party, target);
  }

  public void invitePlayer(Party party, Player target) {
    if (hasParty(target)) {
      return;
    }
    List<Invitation> list = new ArrayList();
    if (invitations.get(target.getUniqueId()) != null) {
      list = invitations.get(target.getUniqueId());
    }
    list.add(new Invitation(party));
    invitations.put(target.getUniqueId(), list);
    target.sendMessage(Text.colorize(PlaceholderAPI.setPlaceholders(Bukkit.getPlayer(party.getLeader().getUUID()), plugin.getSettings().getString("config.message.invite", "&fYou've been invited to %player_name%'s party!"))));
  }

  public Set<Player> getOnlinePlayers(Party party) {
    Set<Player> players = new HashSet<>();
    for (PartyMember member : party.getMembers()) {
      Player p = Bukkit.getServer().getPlayer(member.getUUID());
      if (p != null && p.isOnline()) {
        players.add(p);
      }
    }
    return players;
  }

  public List<PartyMember> getOnlinePartyMembers(Party party) {
    ArrayList<PartyMember> list = new ArrayList<>();
    for (PartyMember member : party.getMembers()) {
      if (Bukkit.getServer().getPlayer(member.getUUID()) != null) {
        list.add(member);
      }
    }
    return list;
  }

  public Set<Player> getNearbyPlayers(Player player, Double range) {
    if (!hasParty(player)) {
      return null;
    }
    return getNearbyPlayers(getParty(player), player.getLocation(), range);
  }

  public Set<Player> getNearbyPlayers(Party party, Location location, Double range) {
    Set<Player> players = null;
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (getParty(player) == party && player.getWorld() == location.getWorld() && player.getLocation().distance(location) <= Math.pow(range, range)) {
        players.add(player);
      }
    }
    return players;
  }

  public Party getParty(Player player) {
    return getParty(player.getUniqueId());
  }

  public Party getParty(UUID uuid) {
    for (Party party : parties) {
      for (PartyMember member : party.getMembers()) {
        if (member.getUUID().equals(uuid)) {
          return party;
        }
      }
    }
    return null;
  }

  public boolean hasParty(Player player) {
    return getParty(player.getUniqueId()) != null;
  }

  public void promotePlayer(Player player) {
    Party party = getParty(player.getUniqueId());
    party.setLeader(player);
    partyAnnounce(party, Text.colorize(PlaceholderAPI.setPlaceholders(Bukkit.getPlayer(party.getLeader().getUUID()), plugin.getSettings().getString("config.message.new-leader", "&f%player_name% is now the leader of the party!"))));
  }

  private void promoteNextInLine(Party party) {
    for (PartyMember member : party.getMembers()) {
      if (member.getUUID() != party.getLeader().getUUID()) {
        party.setLeader(member);
        return;
      }
    }
  }

  public boolean isLeader(Player player) {
    return isLeader(player.getUniqueId());
  }

  public boolean isLeader(UUID uuid) {
    Party party = getParty(uuid);
    if (party == null) {
      return false;
    }
    return party.getLeader().getUUID().equals(uuid);
  }

  public boolean isValidInvite(Invitation invitation) {
    return System.currentTimeMillis() - invitation.getTimestamp() < maxInviteMillis;
  }

  public Boolean areInSameParty(Player p1, Player p2) {
    return getParty(p1.getUniqueId()) == getParty(p2.getUniqueId());
  }

  public Scoreboard setupScoreboard() {
    return setupScoreboard("Party");
  }

  public Scoreboard setupScoreboard(String partyName) {
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    Objective objective = scoreboard
        .registerNewObjective("objective", "dummy", TextUtils.color(partyName));
    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    return scoreboard;
  }

  public void addToScoreboard(Player player) {
    Scoreboard scoreboard = getParty(player.getUniqueId()).getScoreboard();
    scoreboard.getObjective(DisplaySlot.SIDEBAR);
    player.setScoreboard(scoreboard);
  }

  public void tickOfflinePartyMembers() {
    Set<UUID> removeMap = new HashSet<>();
    for (Party party : parties) {
      for (PartyMember member : party.getMembers()) {
        if (Bukkit.getPlayer(member.getUUID()) == null
            && System.currentTimeMillis() - member.getQuitTimestamp() > maxOfflineMillis) {
          removeMap.add(member.getUUID());
        }
      }
    }
    for (UUID uuid : removeMap) {
      removePlayer(uuid, RemoveReasons.TimeOut);
    }
  }

  private String getScoreboardKey(int i) {
    return partyBoardKeys.get(i);
  }

  public void updateScoreboard(Party party) {
    Scoreboard scoreboard = party.getScoreboard();
    int i = 16;

    addScoreboardLine(scoreboard, null, borderFormat, i);
    i--;

    for (PartyMember member : party.getMembers()) {
      Player player = Bukkit.getPlayer(member.getUUID());
      boolean validPlayer = player == null || !player.isValid();
      String actualNameFormat = validPlayer ? offlineNameFormat : nameFormat;
      String actualInfoFormat = validPlayer ? offlineInfoFormat : infoFormat;
      actualNameFormat = actualNameFormat.replace("{name}", member.getUsername())
          .replace("{uuid}", member.getUUID().toString());
      actualInfoFormat = actualInfoFormat.replace("{name}", member.getUsername())
          .replace("{uuid}", member.getUUID().toString());

      if (isLeader(member.getUUID())) {
        actualNameFormat = leaderPrefix + actualNameFormat;
      }
      addScoreboardLine(scoreboard, player, actualNameFormat, i);
      i--;
      addScoreboardLine(scoreboard, player, actualInfoFormat, i);
      i--;
    }

    addScoreboardLine(scoreboard, null, borderFormat, i);
  }

  private void addScoreboardLine(Scoreboard board, Player player, String text, int lineNumber) {
    Objective objective = board.getObjective("objective");

    Team teamLine = board.getTeam(String.valueOf(lineNumber));
    if (teamLine == null) {
      teamLine = board.registerNewTeam(String.valueOf(lineNumber));
      teamLine.addEntry(getScoreboardKey(lineNumber));
    }

    if (player == null) {
      teamLine.setPrefix(TextUtils.color(text));
    } else {
      teamLine.setPrefix(PlaceholderAPI.setPlaceholders(player, TextUtils.color(text)));
    }

    objective.getScore(getScoreboardKey(lineNumber)).setScore(lineNumber);
  }

  private void resetScoreboard(Party party) {
    party.setScoreboard(setupScoreboard(party.getPartyName()));
    getOnlinePartyMembers(party).forEach(
        partyMember -> addToScoreboard(Bukkit.getPlayer(partyMember.getUUID())));
  }
}
