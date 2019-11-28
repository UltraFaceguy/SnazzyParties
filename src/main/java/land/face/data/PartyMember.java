package land.face.data;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class PartyMember {

    Player player;
    UUID uuid;
    String username;

    Party party;
    ArrayList<Party> invitations = new ArrayList<>();
    String scoreboardUsername;
    String scoreboardHP;

    public PartyMember(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.username = player.getDisplayName();
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID newUUID) {
        this.uuid = newUUID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party newParty) {
        this.party = party;
    }

    public ArrayList getInvitations() {
        return invitations;
    }

    public void setInvitations(ArrayList newInvitations) {
        this.invitations = newInvitations;
    }

    public String getScoreboardUsername() {
        return scoreboardUsername;
    }

    public void setScoreboardUsername(String string) {
        scoreboardUsername = string;
    }

    public String getScoreboardHP() {
        return scoreboardHP;
    }

    public void setScoreboardHP(String string) {
        scoreboardHP = string;
    }

}
