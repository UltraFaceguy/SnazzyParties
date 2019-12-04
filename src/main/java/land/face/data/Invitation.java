package land.face.data;

public class Invitation {

    private Party party;
    private long timestamp;

    public Invitation(Party party){
        this.party = party;
        timestamp = System.currentTimeMillis();
    }

    public Party getParty() {
        return party;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isValid() {
        return System.currentTimeMillis() - timestamp  < 600000;
    }
}
