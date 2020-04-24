package land.face.listeners;

import java.util.regex.Pattern;
import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

  private SnazzyPartiesPlugin plugin;
  private String partyChatTrigger;
  private String partyChatTriggerRegex;

  public ChatListener(SnazzyPartiesPlugin plugin) {
    this.plugin = plugin;
    partyChatTrigger = plugin.getSettings()
        .getString("config.party-chat-trigger-prefix", "$");
    partyChatTriggerRegex = Pattern.quote(partyChatTrigger);
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
    SnazzyPartiesPlugin.getInstance().getPartyManager().sendPartyMessage(chatEvent.getPlayer(), msg);
  }
}
