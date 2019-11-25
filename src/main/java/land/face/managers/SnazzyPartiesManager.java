package land.face.managers;

import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class SnazzyPartiesManager {

    private SnazzyPartiesPlugin plugin;

    public List<Party> parties = new CopyOnWriteArrayList<>();
    public HashMap<UUID, Party> invitations = new HashMap<>();
    public HashMap<UUID, Integer> scoreboardHealth = new HashMap<UUID, Integer>();

    public SnazzyPartiesManager(SnazzyPartiesPlugin plugin){
        this.plugin = plugin;
    }

    public void createParty(Player player){
        Party party = new Party(player, new ArrayList<>(), setupScoreboard());
        parties.add(party);
        addPlayer(party, player);
        SnazzyPartiesTimer timer = new SnazzyPartiesTimer(party);
        party.setTimer(timer);
        player.sendMessage("Congrats boss you've created a party");
    }

    public void disbandParty(Party party){
        systemSendMessage(party, "Your party has been disbanded");
        getOnlinePartyMembers(party).forEach(player -> removePlayerScoreboard(player));
        party.getTimer().cancel();
        parties.remove(party);
    }

    public void addPlayer(Player cmdSender, Player target){
        Party party = getParty(cmdSender);
        addPlayer(party, target);
    }

    public void addPlayer(Party party, Player target){
        if (party.getPartySize() >= party.getMaxPartySize()){
            return;
        }
        party.getMembers().add(target.getUniqueId());
        addToScoreboard(target);
    }

    public void removePlayer(Player cmdSender, Player target, Party.RemoveReasons reason){
        Party party = getParty(cmdSender);
        removePlayer(party, target, reason);
    }

    public void removePlayer(Party party, Player target, Party.RemoveReasons reason){
        if (party.getPartySize() == 1){
            disbandParty(party);
            return;
        }
        if (party.getLeader() == target.getUniqueId()){
            promoteNextInLine(party);
            party.getMembers().remove(target.getUniqueId());
            removePlayerScoreboard(target);
            systemSendMessage(party,party.getPrefix() + target + reason.getMessage());
            systemSendMessage(party, party.getPrefix() + target + " is now the leader of the party!");
            return;
        }
        removePlayerScoreboard(target);
        party.getMembers().remove(target.getUniqueId());
        systemSendMessage(party,target + reason.getMessage());
    }

    public void invitePlayer(Player cmdSender, Player target){
        if (hasParty(target)){
            cmdSender.sendMessage("They're already in a party");
            return;
        }
        Party party = getParty(cmdSender);
        invitePlayer(party, target);
    }

    public void invitePlayer(Party party, Player target){
        if (hasParty(target)){
            return;
        }
        invitations.put(target.getUniqueId(), party);
        target.sendMessage("You've been invited to " + Bukkit.getPlayer(party.getLeader()).getDisplayName() + "'s party");
    }

    public void sendMessage(Player cmdSender, String message){
        Party party = getParty(cmdSender);
        for (Player player : getOnlinePartyMembers(party)){
            player.sendMessage(party.getPrefix() + cmdSender.getDisplayName() + ": " + message);
        }
    }

    public void systemSendMessage(Party party, String message){
        for (Player player : getOnlinePartyMembers(party)){
            player.sendMessage(party.getPrefix() + message);
        }
    }

    public List<Player> getOnlinePartyMembers(Player player) {
        return getOnlinePartyMembers(getParty(player));
    }

    public List<Player> getOnlinePartyMembers(Party party) {
        ArrayList<Player> list = new ArrayList<>();
        for (UUID uuid : party.getMembers()){
            if (Bukkit.getServer().getPlayer(uuid) != null){
                list.add(Bukkit.getPlayer(uuid));
            }
        }
        return list;
    }

    public Party getParty(Player player){
        for (Party party : parties){
            if (party.getMembers().contains(player.getUniqueId())){
                return party;
            }
        }
        return null;
    }

    public Boolean hasParty(Player player){
        return getParty(player) != null;
    }

    public void promotePlayer(Player player){
        getParty(player).setLeader(player);
        systemSendMessage(getParty(player), "ayo " + player + " has been promoted to leader!");
    }

    public void promoteNextInLine(Party party){
        UUID currentLeader = party.getLeader();
        for (UUID uuid : party.getMembers()) {
            if (uuid != currentLeader){
                promotePlayer(Bukkit.getPlayer(uuid));
                return;
            }
        }
    }

    public Boolean isLeader(Player player){
        if (!hasParty(player)){
            return false;
        }
        return getParty(player).getLeader() == player.getUniqueId();
    }

    public Boolean areInSameParty(Player p1, Player p2){
        return getParty(p1) == getParty(p2);
    }

    @Deprecated
    public Scoreboard setupScoreboard() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Team team = scoreboard.registerNewTeam("Party");
        Objective objective = scoreboard.registerNewObjective("objective", "dummy", "Party");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        return scoreboard;
    }

    @Deprecated
    public void addToScoreboard(Player player) {
        Scoreboard scoreboard = getParty(player).getScoreboard();
        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        //objective.getScore(player).setScore((int) Math.round(player.getHealth()));
        player.setScoreboard(scoreboard);
        updateScoreboard(player);
    }

    @Deprecated
    public void updateScoreboard(Player player) {
        updateScoreboard(getParty(player));
    }

    @Deprecated
    public void updateScoreboard(Party party) {
        int i = party.getMaxPartySize() * 2;
        Scoreboard scoreboard = party.getScoreboard();
        Objective objective = scoreboard.getObjective("objective");
        for (Player player : getOnlinePartyMembers(party)) {
            if (scoreboardHealth.get(player.getUniqueId()) != null) {
                scoreboard.resetScores(ChatColor.RED + String.valueOf(scoreboardHealth.get(player.getUniqueId())) + " ❤");
            }
            objective.getScore(player).setScore(i);
            int value = (int) Math.round(player.getHealth());
            scoreboardHealth.put(player.getUniqueId(), value);
            objective.getScore(ChatColor.RED + String.valueOf(value) + " ❤").setScore(i-1);
            player.setScoreboard(scoreboard);
            i = i - 2;
        }
    }

    @Deprecated
    public void changeScoreboardLeader(Player newLeader) {

        int i = getParty(newLeader).getMaxPartySize() * 2;
        Party party = getParty(newLeader);
        Scoreboard scoreboard = party.getScoreboard();
        Objective objective = scoreboard.getObjective("objective");
        for (Player player : getOnlinePartyMembers(party)) {
            if (player == newLeader) {
                scoreboard.resetScores(player);
                objective.getScore(ChatColor.BOLD + player.getName()).setScore(i);
            }
            else if (player == Bukkit.getPlayer(party.getLeader())) {
                scoreboard.resetScores(player);
                objective.getScore(player.getName()).setScore(i);
            }
            else if (player != newLeader && player != Bukkit.getPlayer(party.getLeader())){
                objective.getScore(player).setScore(i);
            }
            if (scoreboardHealth.get(player.getUniqueId()) != null) {
                scoreboard.resetScores(ChatColor.RED + String.valueOf(scoreboardHealth.get(player.getUniqueId())) + " ❤");
            }
            objective.getScore(player).setScore(i);
            int value = (int) Math.round(player.getHealth());
            scoreboardHealth.put(player.getUniqueId(), value);
            objective.getScore(ChatColor.RED + String.valueOf(value) + " ❤").setScore(i-1);
            i = i - 2;
        }
    }

    public void removePlayerScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

}
