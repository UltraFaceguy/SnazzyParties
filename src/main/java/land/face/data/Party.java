package land.face.data;

import land.face.managers.SnazzyPartiesTimer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;
import java.util.UUID;

public class Party {

    private UUID leader;
    private List<UUID> members;

    private Boolean friendlyFire;
    private Boolean expSharing;
    private Boolean lootSharing;

    private Scoreboard scoreboard;
    private SnazzyPartiesTimer timer;

    private final int MAX_PLAYERS = 8;
    private static final String PREFIX = ChatColor.LIGHT_PURPLE + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + "> " + ChatColor.GRAY;

    public enum RemoveReasons {
        Quit (" has left the party."),
        Kicked (" was kicked from the party!"),
        TimeOut (" timed out.");
        private String message;
        RemoveReasons(String message){
            this.message = message;
        }
        public String getMessage() {
            return message;
        }
    }

    public Party(Player leader, List<UUID> members, Scoreboard scoreboard) {
        this.leader = leader.getUniqueId();
        this.members = members;
        this.friendlyFire = false;
        this.expSharing = false;
        this.lootSharing = false;
        this.scoreboard = scoreboard;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(Player newLeader) {
        setLeader(newLeader.getUniqueId());
    }

    public void setLeader(UUID newLeader) {
        leader = newLeader;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public void setMembers(List<UUID> newMembers) {
        members = newMembers;
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

    public void setFriendlyFire(Boolean bool){
        friendlyFire = bool;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public void setScoreboard(Scoreboard newScoreboard) {
        scoreboard = newScoreboard;
    }

    public SnazzyPartiesTimer getTimer() {
        return timer;
    }

    public void setTimer(SnazzyPartiesTimer newTimer) {
        timer = newTimer;
        timer.run();
    }
}
