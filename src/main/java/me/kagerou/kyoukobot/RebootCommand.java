package me.kagerou.kyoukobot;

import java.util.concurrent.TimeUnit;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class RebootCommand implements CommandExecutor {
	@Command(aliases = {"k!reboot"}, description = "Cheesy admin-only command.", usage = "k!reboot", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, String[] args)
	{
		if (!message.getAuthor().getId().equals(KyoukoBot.adminID))
			message.reply("Y-you're touching me inappropriately!");
		else
		{
			try {
				message.reply("`Rebooting...`").get(3, TimeUnit.SECONDS);
			}
			catch (Exception e)
			{}
			System.out.println("Rebooting and reloading all data...");
			KyoukoBot.reboot(true);
			message.reply("`I'm back!`");
		}
	}
}
