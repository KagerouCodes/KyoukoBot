package main.java.me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//makes the bot say what you want, admin-only
public class SayCommand implements CommandExecutor
{
	@Command(aliases = {"k!say"}, description = "Cheesy admin-only command.", requiredPermissions = "admin", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, Server server, String args[])
    {
		message.reply(KyoukoBot.getArgument(message, false));
    }
}
