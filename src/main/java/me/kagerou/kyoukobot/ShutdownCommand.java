package me.kagerou.kyoukobot;

import java.util.concurrent.TimeUnit;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class ShutdownCommand implements CommandExecutor {
	@Command(aliases = {"k!shutdown"}, description = "Cheesy admin-only command.", usage = "k!shutdown", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, String[] args)
	{
		if (!message.getAuthor().getId().equals(KyoukoBot.adminID))
			message.reply("Y-you're touching me inappropriately!");
		else
		{
			try {
				message.reply("`Shutting down...`").get(3, TimeUnit.SECONDS);
			}
			catch (Exception e)
			{}
			api.disconnect();
			System.exit(0);
		}
	}
}
