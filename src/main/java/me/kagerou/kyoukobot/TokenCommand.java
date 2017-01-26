package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class TokenCommand implements CommandExecutor {
	@Command(aliases = {"k!token"}, description = "Links my GitHub repository.", usage = "k!token", showInHelpPage = false)
	public String onCommand(DiscordAPI api, Message message) {
		return "Steal it here: https://github.com/KagerouCodes/KyoukoBot";
	}
}
