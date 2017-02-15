package me.kagerou.kyoukobot;

import java.util.concurrent.TimeUnit;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class ShutdownCommand implements CommandExecutor {
	@Command(aliases = {"k!shutdown"}, description = "Cheesy admin-only command.", usage = "k!shutdown", requiredPermissions = "admin", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, String[] args)
	{
		try {
			message.reply("`Shutting down...`").get(3, TimeUnit.SECONDS);
		}
		catch (Exception e)
		{}
		api.disconnect();
		//api.setGame("Shutting down...");
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {}
		System.exit(0);
	}
}
