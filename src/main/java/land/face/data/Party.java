package land.face.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import land.face.SnazzyPartiesPlugin;
import land.face.tasks.PartyTask;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class Party {

  private PartyMember leader;
  private List<PartyMember> members;

  private Boolean friendlyFire;
  private Boolean expSharing;
  private Boolean lootSharing;

  private Scoreboard scoreboard;
  private String partyName;
  private PartyTask partyTask;

  private final int MAX_PLAYERS = 5;
  private static final String PREFIX = SnazzyPartiesPlugin.getInstance().getConfig()
      .getString("prefix");

  public enum RemoveReasons {
    Quit(SnazzyPartiesPlugin.getInstance().getSettings().getString("config.message.remove-reason.quit", "&f%player_name% has left the party.")),
    Kicked(SnazzyPartiesPlugin.getInstance().getSettings().getString("config.message.remove-reason.kick", "&f%player_name% was kicked from the party!")),
    TimeOut(SnazzyPartiesPlugin.getInstance().getSettings().getString("config.message.remove-reason.timeout", "&f%player_name% timed out."));
    private String message;

    RemoveReasons(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }

  public Party(PartyMember leader, Scoreboard scoreboard, String partyName) {
    this.leader = leader;
    this.friendlyFire = false;
    this.expSharing = false;
    this.lootSharing = false;
    this.scoreboard = scoreboard;
    this.partyName = partyName;
    members = new ArrayList<>();
    members.add(leader);
    partyTask = new PartyTask(this);
  }

  public PartyMember getLeader() {
    return leader;
  }

  public void setLeader(Player newLeader) {
    setLeader(new PartyMember(newLeader));
  }

  public void setLeader(PartyMember newLeader) {
    leader = newLeader;
  }

  public List<PartyMember> getMembers() {
    return members;
  }

  public String getPrefix() {
    return PREFIX;
  }

  public int getMaxPartySize() {
    return MAX_PLAYERS;
  }

  public int getPartySize() {
    return members.size();
  }

  public Boolean getFriendlyFire() {
    return friendlyFire;
  }

  public void setFriendlyFire(Boolean bool) {
    friendlyFire = bool;
  }

  public Scoreboard getScoreboard() {
    return scoreboard;
  }

  public void setScoreboard(Scoreboard newScoreboard) {
    scoreboard = newScoreboard;
  }

  public PartyTask getPartyTask() {
    return partyTask;
  }

  public PartyMember getMember(Player player) {
    return getMember(player.getUniqueId());
  }

  public String getPartyName() {
    return partyName;
  }

  public void setPartyName(String partyName) {
    this.partyName = partyName;
  }

  public PartyMember getMember(UUID uuid) {
    for (PartyMember member : members) {
      if (uuid == member.getUUID()) {
        return member;
      }
    }
    return null;
  }

  public boolean isMember(Player player) {
    return getMember(player) != null;
  }
}
