package land.face.utils;

import land.face.SnazzyPartiesPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Text {
    public static String colorize(String message){
        return message.replaceAll("&([0-9a-fk-or])", ChatColor.COLOR_CHAR + "$1");
    }
    public static String configHandler(Player player, String message) {
        message = message.replace("{name}", player.getDisplayName());
        message = message.replace("{prefix}", SnazzyPartiesPlugin.getInstance().getPartyManager().getPrefix());
        message = PlaceholderAPI.setPlaceholders(player, message);
        return colorize(message);
    }
    public static String configHandler(String username, String message) {
        message = message.replace("{name}", username);
        message = message.replace("{prefix}", SnazzyPartiesPlugin.getInstance().getPartyManager().getPrefix());
        return colorize(message);
    }
}