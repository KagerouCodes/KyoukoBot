package me.kagerou.kyoukobot;

import java.util.concurrent.TimeUnit;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//reboots the bot (owner only)
public class RebootCommand implements CommandExecutor {
	@Command(aliases = {"k!reboot"}, description = "Cheesy admin-only command.", usage = "k!reboot", requiredPermissions = "admin", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, String[] args)
	{
		try {
			message.reply("`Rebooting...`").get(3, TimeUnit.SECONDS);
		}
		catch (Exception e)
		{}
		System.out.println("Rebooting and reloading all data...");
		KyoukoBot.reboot(true);
		message.reply("`I'm back!`"); //this never happens but it's here in case i implement rebooting without shutting down the application again
	}
}
