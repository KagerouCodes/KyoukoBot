package main.java.me.kagerou.kyoukobot;

import java.io.File;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//sends a file from the bot's directory (owner-only)
//needed because i don't have direct access to the machine the bot's running on
public class RequestCommand implements CommandExecutor {
	@Command(aliases = {"k!request"}, description = "Cheesy admin-only command.", usage = "k!request filename", requiredPermissions = "admin", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, String args[])
    {
		if (args.length == 0)
		{
			message.reply("`Specify the filename.`");
			return;
		}
		String FileName = KyoukoBot.getArgument(message, false);
		File result = new File(System.getProperty("user.dir") + '/' + FileName);
		if (!result.exists() || result.isDirectory())
		{
			message.reply("`File not found.`");
			return;
		}
		message.getReceiver().sendFile(result);
    }
}
