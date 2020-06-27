package land.face.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import land.face.SnazzyPartiesPlugin;
import land.face.data.Invitation;
import land.face.data.Party;
import land.face.data.PartyMember;
import land.face.data.RemoveReason;
import land.face.utils.Text;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
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
  private HashMap<UUID, Party> playerParty;

  private String prefix;
  private String leaderPrefix;
  private String nameFormat;
  private String offlineNameFormat;
  private String infoFormat;
  private String offlineInfoFormat;
  private String borderFormat;

  private String partyChatFormat;
  private String partyChatMessageRegex;
  private String quit;
  private String kicked;
  private String timeout;

  public String PARTY_OBJECTIVE = "SP_";

  public PartyManager(SnazzyPartiesPlugin plugin) {
    this.plugin = plugin;
    this.parties = new ArrayList<>();
    this.invitations = new HashMap<>();
    this.playerParty = new HashMap<>();
    prefix = plugin.getSettings().getString("config.prefix", "&d<&lP&d>&r");
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
    quit = plugin.getSettings()
        .getString("config.language.party-quit", "&f{name} has left the party.");
    kicked = plugin.getSettings()
        .getString("config.language.party-kick", "&f{name} was kicked from the party!");
    timeout = plugin.getSettings()
        .getString("config.language.party-timeout", "&f{name} timed out.");

    Map<Integer, ChatColor> chatColorOrdinals = new HashMap<>();
    for (ChatColor chatColor : ChatColor.values()) {
      chatColorOrdinals.put(chatColor.ordinal(), chatColor);
    }

    for (int i = 16; i >= 0; i--) {
      partyBoardKeys.put(i, ChatColor.BLACK + "" + chatColorOrdinals.get(i));
    }

    defaultBoard = Bukkit.getScoreboardManager().getMainScoreboard();

    partyChatFormat = plugin.getSettings().getString("config.language.party-chat-format",
        "&b[Party] %player_name%: #");
    partyChatMessageRegex = Pattern.quote("#");
  }

  public String getPrefix() {
    return prefix;
  }

  public Scoreboard getDefaultBoard() {
    return defaultBoard;
  }

  public List<Party> getParties() {
    return parties;
  }

  public HashMap<UUID, List<Invitation>> getInvitations() {
    return invitations;
  }

  public void sendPartyMessage(Player player, String message) {
    message = partyChatFormat.replaceFirst(partyChatMessageRegex, message);
    for (Player member : getOnlinePlayers(getParty(player))) {
      member.sendMessage(Text.configHandler(player, message));
    }
  }

  public void partyAnnounce(Player player, String message) {
    Party party = getParty(player);
    if (party == null) {
      return;
    }
    partyAnnounce(party, player.getDisplayName() + ": " + message);
  }

  public void partyAnnounce(Party party, String message) {
    message = Text.colorize(message.replace("{prefix}", prefix));
    for (PartyMember player : getOnlinePartyMembers(party)) {
      Bukkit.getPlayer(player.getUUID()).sendMessage(message);
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
    playerParty.put(player.getUniqueId(), party);
    player.setScoreboard(party.getScoreboard());
    player.sendMessage(Text.configHandler(player, plugin.getSettings()
        .getString("config.language.party-create", "Congrats boss your party has been created")));
  }

  public void disbandParty(Party party) {
    partyAnnounce(party,
        plugin.getConfig()
            .getString("config.language.party-disband", "Your party has been disbanded"));
    getOnlinePartyMembers(party)
        .forEach(player -> Bukkit.getPlayer(player.getUsername()).setScoreboard(defaultBoard));
    party.getMembers()
        .forEach(partyMember -> playerParty.remove(partyMember.getUUID()));
    party.getPartyTask().cancel();
    parties.remove(party);
  }

  public boolean addPlayer(Party party, Player player) {
    return addPlayer(party, new PartyMember(player));
  }

  public boolean addPlayer(Party party, PartyMember partyMember) {
    if (party.getPartySize() >= party.getMaxPartySize()) {
      return false;
    }
    party.getMembers().add(partyMember);
    playerParty.put(partyMember.getUUID(), party);
    resetScoreboard(party);
    addToScoreboard(Bukkit.getPlayer(partyMember.getUUID()));
    return true;
  }

  public void removePlayer(Player target, RemoveReason reason) {
    removePlayer(target.getUniqueId(), reason);
  }

  public void removePlayer(UUID uuid, RemoveReason reason) {
    Party party = getParty(uuid);
    if (party.getPartySize() == 1) {
      disbandParty(party);
      return;
    }
    if (party.getLeader().getUUID().equals(uuid)) {
      promoteNextInLine(party);
    }
    partyAnnounce(party,
        removeReasonHandler(reason).replace("{name}", party.getMember(uuid).getUsername()));
    resetScoreboard(party);
    party.getMembers().remove(party.getMember(uuid));
    playerParty.remove(uuid);
    Player player = Bukkit.getPlayer(uuid);
    if (player != null && player.isValid()) {
      player.setScoreboard(defaultBoard);
    }
  }

  private String removeReasonHandler(RemoveReason reason) {
    switch (reason) {
      case QUIT:
        return quit;
      case KICKED:
        return kicked;
      case TIMEOUT:
        return timeout;
    }
    return "Something went horribly wrong, but hey {name} was kicked from your party!";
  }

  public void invitePlayer(Player cmdSender, Player target) {
    if (hasParty(target)) {
      cmdSender.sendMessage(Text.configHandler(cmdSender, plugin.getSettings()
          .getString("config.language.has-party.target", "They're already in a party")));
      return;
    }
    Party party = getParty(cmdSender);
    invitePlayer(party, cmdSender, target);
  }

  public void invitePlayer(Party party, Player sender, Player target) {
    if (hasParty(target)) {
      return;
    }
    List<Invitation> list = new ArrayList();
    if (invitations.get(target.getUniqueId()) != null) {
      list = invitations.get(target.getUniqueId());
    }
    list.add(new Invitation(party));
    invitations.put(target.getUniqueId(), list);
    target.sendMessage(Text.configHandler(Bukkit.getPlayer(party.getLeader().getUUID()),
        plugin.getSettings().getString("config.language.party-invite",
            "&fYou've been invited to %player_name%'s party!")));

    TextComponent acceptButton = new TextComponent("[ACCEPT]");
    acceptButton.setColor(net.md_5.bungee.api.ChatColor.GREEN);
    acceptButton.setBold(true);
    acceptButton.setClickEvent(
        new ClickEvent(Action.RUN_COMMAND, "/party accept " + sender.getName()));

    TextComponent declineButton = new TextComponent("[DECLINE]");
    declineButton.setColor(net.md_5.bungee.api.ChatColor.RED);
    declineButton.setBold(true);
    declineButton.setClickEvent(
        new ClickEvent(Action.RUN_COMMAND, "/party decline " + sender.getName()));

    target.spigot().sendMessage(
        new ComponentBuilder("  ")
            .append(acceptButton)
            .append("  ")
            .append(declineButton)
            .create()
    );
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

  public Set<Player> getNearbyPlayers(Party party, Location location, double range) {
    Set<Player> players = new HashSet<>();
    for (Player player : getOnlinePlayers(party)) {
      if (player.getWorld() != location.getWorld()) {
        continue;
      }
      if (player.getLocation().distanceSquared(location) <= Math.pow(range, 2)) {
        players.add(player);
      }
    }
    return players;
  }

  public Party getParty(Player player) {
    return getParty(player.getUniqueId());
  }

  public Party getParty(UUID uuid) {
    return playerParty.get(uuid);
  }

  public boolean hasParty(Player player) {
    return getParty(player.getUniqueId()) != null;
  }

  public void promotePlayer(Player player) {
    Party party = getParty(player.getUniqueId());
    party.setLeader(player);
    partyAnnounce(party, Text.configHandler(
        Bukkit.getPlayer(party.getLeader().getUUID()),
        plugin.getSettings().getString("config.language.party-new-leader",
            "&f%player_name% is now the leader of the party!")));
  }

  private void promoteNextInLine(Party party) {
    for (PartyMember member : party.getMembers()) {
      if (member.getUUID() != party.getLeader().getUUID()) {
        party.setLeader(member);
        partyAnnounce(party, Text.configHandler(
            Bukkit.getPlayer(member.getUUID()),
            plugin.getSettings().getString("config.language.party-new-leader",
                "&f%player_name% is now the leader of the party!")));
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

  public Scoreboard setupScoreboard(String partyName) {
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    Objective objective = scoreboard.registerNewObjective(PARTY_OBJECTIVE, "dummy", Text.colorize(partyName));
    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    return scoreboard;
  }

  public void addToScoreboard(Player player) {
    Scoreboard scoreboard = getParty(player.getUniqueId()).getScoreboard();
    //scoreboard.getObjective(DisplaySlot.SIDEBAR);
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
      removePlayer(uuid, RemoveReason.TIMEOUT);
    }
  }

  private String getScoreboardKey(int i) {
    return partyBoardKeys.get(i);
  }

  public void updateScoreboard(Party party) {
    if (party == null) {
      Bukkit.getLogger().warning("Null party trying to be updated!");
      return;
    }
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

    Team teamLine = board.getTeam(String.valueOf(lineNumber));
    if (teamLine == null) {
      teamLine = board.registerNewTeam(String.valueOf(lineNumber));
      teamLine.addEntry(getScoreboardKey(lineNumber));
    }

    if (player == null) {
      teamLine.setPrefix(Text.colorize(text));
    } else {
      teamLine.setPrefix(PlaceholderAPI.setPlaceholders(player, Text.colorize(text)));
    }

    Objective objective = board.getObjective(DisplaySlot.SIDEBAR);
    if (objective == null) {
      Bukkit.getLogger().warning("Null objective!");
      return;
    }
    objective.getScore(getScoreboardKey(lineNumber)).setScore(lineNumber);
  }

  private void resetScoreboard(Party party) {
    party.setScoreboard(setupScoreboard(party.getPartyName()));
    getOnlinePartyMembers(party).forEach(
        partyMember -> addToScoreboard(Bukkit.getPlayer(partyMember.getUUID())));
  }

  public boolean mergeParty(Party party1, Party party2) {
    int combinedSize = party1.getPartySize() + party2.getPartySize();
    if (combinedSize < party1.getMaxPartySize() && combinedSize < party2.getMaxPartySize()) {
      party2.getMembers().forEach(partyMember -> addPlayer(party1, partyMember));
      return true;
    }
    return false;
  }
}
