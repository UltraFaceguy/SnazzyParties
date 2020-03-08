package land.face.data;

import java.util.UUID;

public class Invitation {

  private Party party;
  private UUID inviter;
  private long timestamp;

  public Invitation(Party party) {
    this.party = party;
    this.inviter = party.getLeader().getUUID();
    timestamp = System.currentTimeMillis();
  }

  public Party getParty() {
    return party;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public UUID getInviter() {
    return inviter;
  }
}
