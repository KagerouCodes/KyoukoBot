package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

@Deprecated
public class IntroDefaultCommand implements CommandExecutor {
	@Command(aliases = {"k!introdefault"}, description = "Cheesy admin-only command.", requiredPermissions = "admin", usage = "k!introstandard", showInHelpPage = false)
    public String onCommand(DiscordAPI api, Message message, String args[])
	{
		KyoukoBot.Database.defaultFill(KyoukoBot.DatabaseFile);
		return "Reset the database to default!";
	}
}