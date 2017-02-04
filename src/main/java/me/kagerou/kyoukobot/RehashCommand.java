package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class RehashCommand implements CommandExecutor {
	static final String dirName = "memes";

	@Command(aliases = {"k!rehash"}, description = "Cheesy admin-only command.", requiredPermissions = "admin", showInHelpPage = false)
    public String onCommand(DiscordAPI api, Message message, String args[])
	{
		return KyoukoBot.memeBase.reHash();
	}
}
