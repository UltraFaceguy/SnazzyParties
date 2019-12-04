package land.face.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import java.util.regex.Pattern;
import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

  private SnazzyPartiesPlugin plugin;
  private String partyChatTrigger;
  private String partyChatFormat;
  private String partyChatTriggerRegex;
  private String partyChatMessageRegex;

  public ChatListener(SnazzyPartiesPlugin plugin) {
    this.plugin = plugin;
    partyChatTrigger = plugin.getSettings()
        .getString("config.party-chat-trigger-prefix", "$");
    partyChatFormat = plugin.getSettings()
        .getString("config.language.party-chat-format", "&b[Party] %player_name%: #");
    partyChatTriggerRegex = Pattern.quote(partyChatTrigger);
    partyChatMessageRegex = Pattern.quote("#");
  }

  @EventHandler
  public void onPartyChat(AsyncPlayerChatEvent chatEvent) {
    Party party = plugin.getPartyManager().getParty(chatEvent.getPlayer().getUniqueId());
    if (party == null) {
      return;
    }
    if (!ChatColor.stripColor(chatEvent.getMessage()).startsWith(partyChatTrigger)) {
      return;
    }
    chatEvent.setCancelled(true);
    String msg = chatEvent.getMessage().replaceFirst(partyChatTriggerRegex, "");
    msg = partyChatFormat.replaceFirst(partyChatMessageRegex, msg);
    for (Player player : plugin.getPartyManager().getOnlinePlayers(party)) {
      MessageUtils.sendMessage(player, PlaceholderAPI.setPlaceholders(player, msg));
    }
  }
}
