package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;


public class SetGameCommand implements CommandExecutor {
	@Command(aliases = {"k!setgame"}, description = "Cheesy admin-only command.", usage = "k!setgame game", requiredPermissions = "admin", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, String[] args)
	{
		String game = message.getContent().substring("k!setgame".length()).trim();
		KyoukoBot.Database.setGame(game);
		api.setGame(game);
	}
}