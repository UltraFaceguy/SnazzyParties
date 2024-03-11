package land.face.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import land.face.managers.PartyManager;
import land.face.tasks.PartyTask;
import land.face.utils.Text;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class Party {

  private PartyMember leader;
  private List<PartyMember> members;

  private boolean friendlyFire;
  private boolean expSharing;
  private boolean lootSharing;
  private boolean canMerge = true;

  private Scoreboard scoreboard;
  private String partyName;
  private PartyTask partyTask;

  private static final int MAX_PLAYERS = 5;
  private static final List<String> titles = List.of("", "೧", "೨", "೩", "೪", "೫");

  public Party(PartyMember leader, String partyName) {
    this.leader = leader;
    this.friendlyFire = false;
    this.expSharing = false;
    this.lootSharing = false;
    this.partyName = "\uF808\uF804" + titles.get(1) + "\uF808\uF804";
    this.scoreboard = PartyManager.setupScoreboard(this.partyName);
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

  public void refreshPartyName() {
    setPartyName("sneed");
  }

  public void setPartyName(String partyName) {
    this.partyName = "\uF808\uF804" + titles.get(members.size()) + "\uF808\uF804";
    Objects.requireNonNull(scoreboard.getObjective(PartyManager.PARTY_OBJECTIVE))
        .setDisplayName(this.partyName);
  }

  public boolean CanMerge() {
    return canMerge;
  }

  public void setCanMerge(boolean canMerge) {
    this.canMerge = canMerge;
  }

  public PartyMember getMember(UUID uuid) {
    for (PartyMember member : members) {
      if (uuid.equals(member.getUUID())) {
        return member;
      }
    }
    return null;
  }

  public boolean isMember(Player player) {
    return getMember(player) != null;
  }
}
