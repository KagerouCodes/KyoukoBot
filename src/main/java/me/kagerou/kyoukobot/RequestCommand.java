package me.kagerou.kyoukobot;

import java.io.File;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class RequestCommand implements CommandExecutor {
	@Command(aliases = {"k!request"}, description = "Cheesy admin-only command.", usage = "k!request filename", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, String args[])
    {
		if (!message.getAuthor().getId().equals(KyoukoBot.adminID))
		{
			message.reply("Y-you're touching me inappropriately!");
			return;
		}
		if (args.length == 0)
		{
			message.reply("`Specify the filename.`");
			return;
		}
		String FileName = message.getContent().split(" ", 2)[1].trim();
		File result = new File(System.getProperty("user.dir") + '/' + FileName);
		if (!result.exists() || result.isDirectory())
		{
			message.reply("`File not found.`");
			return;
		}
		message.getReceiver().sendFile(result);
    }
}
