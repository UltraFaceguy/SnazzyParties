package land.face.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import land.face.data.Party.RemoveReasons;
import land.face.data.PartyMember;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PartyManager {

  private SnazzyPartiesPlugin plugin;

  public List<Party> parties = new CopyOnWriteArrayList<>();
  public HashMap<UUID, Party> invitations = new HashMap<>();

  private Map<Integer, String> partyBoardKeys = new HashMap<>();
  private Scoreboard defaultBoard;

  private int maxOfflineMillis = 600000;

  private String leaderPrefix;
  private String nameFormat;
  private String offlineNameFormat;
  private String infoFormat;
  private String offlineInfoFormat;
  private String borderFormat;

  public PartyManager(SnazzyPartiesPlugin plugin) {
    this.plugin = plugin;
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

  public void partyAnnounce(Player player, String message) {
    Party party = getParty(player);
    if (party == null) {
      return;
    }
    partyAnnounce(party, message);
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
    if (name.length() > 18) {
      name = name.substring(0, 17);
    }
    Party party = new Party(new PartyMember(player), setupScoreboard(name), name);
    parties.add(party);
    player.setScoreboard(party.getScoreboard());
    player.sendMessage("Congrats boss you've created a party");
  }

  public void disbandParty(Party party) {
    partyAnnounce(party, "Your party has been disbanded");
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
    PartyMember partyMember = party.getMember(uuid);
    if (party.getLeader().getUUID() == uuid) {
      promoteNextInLine(party);
      partyAnnounce(party, party.getPrefix() + partyMember.getUsername() + reason.getMessage());
      partyAnnounce(party, party.getPrefix() + partyMember.getUsername() + " is now the leader of the party!");
      return;
    } else {
      partyAnnounce(party, partyMember.getUsername() + reason.getMessage());
    }
    resetScoreboard(party);
    party.getMembers().removeIf(loopMember -> partyMember.getUUID().equals(loopMember.getUUID()));

    Player player = Bukkit.getPlayer(uuid);
    if (player != null && player.isValid()) {
      player.setScoreboard(defaultBoard);
    }
  }

  public void invitePlayer(Player cmdSender, Player target) {
    if (hasParty(target)) {
      cmdSender.sendMessage("They're already in a party");
      return;
    }
    Party party = getParty(cmdSender);
    invitePlayer(party, target);
  }

  public void invitePlayer(Party party, Player target) {
    if (hasParty(target)) {
      return;
    }
    invitations.put(target.getUniqueId(), party);
    target.sendMessage("You've been invited to " + party.getLeader().getUsername() + "'s party");
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

  public Party getParty(Player player) {
    return getParty(player.getUniqueId());
  }

  public Party getParty(UUID uuid) {
    for (Party party : parties) {
      for (PartyMember member : party.getMembers()) {
        if (member.getUUID().toString().equals(uuid.toString())) {
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
    partyAnnounce(party, "ayo " + player.getName() + " has been promoted to leader!");
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
