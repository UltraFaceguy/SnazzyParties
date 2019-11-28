package land.face.managers;

import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import land.face.data.PartyMember;
import land.face.utils.Text;
import land.face.utils.Timer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class SnazzyPartiesManager {

    private SnazzyPartiesPlugin plugin;

    public List<Party> parties = new CopyOnWriteArrayList<>();
    public HashMap<UUID, Party> invitations = new HashMap<>();
    public HashMap<UUID, String> scoreboardHealth = new HashMap<UUID, String>();
    public String[] list = new String[6];

    public SnazzyPartiesManager(SnazzyPartiesPlugin plugin){
        this.plugin = plugin;
    }

    @Deprecated
    public void createParty(Player player){
        Party party = new Party(new PartyMember(player), new ArrayList<>(), setupScoreboard());
        parties.add(party);
        addPlayer(party, player);
        Timer timer = new Timer(party);
        party.setTimer(timer);
        player.sendMessage("Congrats boss you've created a party");
    }

    public void disbandParty(Party party){
        systemSendMessage(party, "Your party has been disbanded");
        getOnlinePartyMembers(party).forEach(player ->  Bukkit.getPlayer(player.getUsername()).setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard()));
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
        party.getMembers().add(new PartyMember(target));
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
        if (party.getLeader().getUUID() == target.getUniqueId()){
            promoteNextInLine(party);
            systemSendMessage(party,party.getPrefix() + target.getName() + reason.getMessage());
            systemSendMessage(party, party.getPrefix() + target.getName() + " is now the leader of the party!");
            return;
        }
        else {
            systemSendMessage(party,target.getName() + reason.getMessage());
        }
        removePlayerScoreboard(target);
        party.getMembers().removeIf(partyMember -> partyMember.getUsername().equals(target.getName()));
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
        target.sendMessage("You've been invited to " + party.getLeader().getUsername() + "'s party");
    }

    public void sendMessage(Player cmdSender, String message){
        Party party = getParty(cmdSender);
        for (PartyMember player : getOnlinePartyMembers(party)){
            Bukkit.getPlayer(player.getUsername()).sendMessage(party.getPrefix() + cmdSender.getDisplayName() + ": " + message);
        }
    }

    public void systemSendMessage(Party party, String message){
        for (PartyMember player : getOnlinePartyMembers(party)){
            Bukkit.getPlayer(player.getUsername()).sendMessage(party.getPrefix() + message);
        }
    }

    public List<PartyMember> getOnlinePartyMembers(Player player) {
        return getOnlinePartyMembers(getParty(player));
    }

    /*
     * Change return to PartyMember
     */
    public List<PartyMember> getOnlinePartyMembers(Party party) {
        ArrayList<PartyMember> list = new ArrayList<>();
        for (PartyMember member : party.getMembers()){
            if (Bukkit.getServer().getPlayer(member.getUUID()) != null){
                list.add(member);
            }
        }
        return list;
    }

    public Party getParty(Player player){
        for (Party party : parties){
            for (PartyMember member : party.getMembers()) {
                if (member.getUUID() == player.getUniqueId()){
                    return party;
                }
            }
        }
        return null;
    }

    public Boolean hasParty(Player player){
        return getParty(player) != null;
    }

    public void promotePlayer(Player player){
        getParty(player).setLeader(player);
        systemSendMessage(getParty(player), "ayo " + player.getName() + " has been promoted to leader!");
    }

    public void promoteNextInLine(Party party){
        UUID currentLeader = party.getLeader().getUUID();
        for (PartyMember member : party.getMembers()) {
            if (member.getUUID() != currentLeader){
                party.setLeader(member);
                return;
            }
        }
    }

    public Boolean isLeader(Player player){
        if (!hasParty(player)){
            return false;
        }
        return getParty(player).getLeader().getUUID() == player.getUniqueId();
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
        scoreboard.getObjective(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
        updateScoreboard(player);
    }

    @Deprecated
    public void updateScoreboard(Player player) {
        updateScoreboard(getParty(player));
    }

    /**
     * Bold for Leader
     * Grey for offline
     * Something to remove people who left party
     *
     * Store player names somewhere
     */
    @Deprecated
    public void updateScoreboard(Party party) {
        int i = party.getMaxPartySize() * 2;
        Scoreboard scoreboard = party.getScoreboard();
        Objective objective = scoreboard.getObjective("objective");

        for (PartyMember member : party.getMembers()) {
            String healthTag = String.valueOf(i/2).replaceAll("([0-9])", "&$1"); //HP Tag
            String usernameTag = String.valueOf(i).replaceAll("([0-9])", "&$1&r"); //Usr Tag
            boolean online = false;
            Integer value = null;
            Player player = null;
            if (Bukkit.getPlayer(member.getUsername()) != null){
                online = true;
                player = Bukkit.getPlayer(member.getUsername());
                value = (int) Math.round(player.getHealth());
            }

            //Decides player's scoreboard naming coloring
            StringBuilder nameFormat = new StringBuilder();
            if (!online) {
                nameFormat.append("&7");
            }
            else {
                nameFormat.append("&f");
            }
            if (party.getLeader().getUUID() == member.getUUID()){
                nameFormat.append("&l");
            }

            nameFormat.append(member.getUsername());

            if (member.getScoreboardUsername() != null) {
                scoreboard.resetScores(Text.colorize(member.getScoreboardUsername()));
            }
            objective.getScore(Text.colorize(nameFormat.toString())).setScore(i);
            member.setScoreboardUsername(nameFormat.toString());

            if (member.getScoreboardHP() != null) {
                scoreboard.resetScores(Text.colorize(member.getScoreboardHP()));
            }

            if (value == null) {
                objective.getScore(Text.colorize(member.getScoreboardHP())).setScore(i-1);
            }
            else {
                String health = healthTag + "&c" + value + " ❤";
                objective.getScore(Text.colorize(health)).setScore(i-1);
                member.setScoreboardHP(health);
            }

            if (online){
                player.setScoreboard(scoreboard);
            }
            i = i-2;
        }

        /*
        for (int j = 0 ; j < 5 ; j++) {
            String str = String.valueOf(i/2).replaceAll("([0-9])", "&$1"); //HP Tag
            String str2 = String.valueOf(i).replaceAll("([0-9])", "&$1&r"); //Usr Tag
            if (list[j] != null) {
                scoreboard.resetScores(Text.colorize(list[j]));
            }
            objective.getScore(Text.colorize(str2 + player.getName())).setScore(i);
            String string = str + "&c" + value + " ❤";
            objective.getScore(Text.colorize(string)).setScore(i-1);
            player.setScoreboard(scoreboard);
            i = i - 2;
            list[j] = string;
        }
         */

        /*
        for (Player player : getOnlinePartyMembers(party)) {
            StringBuilder str = new StringBuilder().append("&").append(i/2);
            if (scoreboardHealth.get(player.getUniqueId()) != null) {
                scoreboard.resetScores(Text.colorize(str + "&c" + String.valueOf(scoreboardHealth.get(player.getUniqueId())) + " ❤"));
            }
            objective.getScore(player).setScore(i);
            int value = (int) Math.round(player.getHealth());
            scoreboardHealth.put(player.getUniqueId(), value);
            objective.getScore(Text.colorize(str + "&c" + String.valueOf(scoreboardHealth.get(player.getUniqueId())) + " ❤")).setScore(i-1);
            player.setScoreboard(scoreboard);
            i = i - 2;
        }
         */
    }

    public void resetScoreboard(Party party) {
        party.setScoreboard(setupScoreboard());
        getOnlinePartyMembers(party).forEach(partyMember -> addToScoreboard(Bukkit.getPlayer(partyMember.getUsername())));
    }

    public void removePlayerScoreboard(Player player) {
        Party party = getParty(player);
        Scoreboard scoreboard = party.getScoreboard();

        for (PartyMember member : party.getMembers()) {
            if (member.getUsername().equals(player.getName())){
                scoreboard.resetScores(member.getScoreboardHP());
                scoreboard.resetScores(member.getScoreboardUsername());
            }
        }

        getOnlinePartyMembers(party).forEach(player1 -> Bukkit.getPlayer(player1.getUsername()).setScoreboard(scoreboard));
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

}
