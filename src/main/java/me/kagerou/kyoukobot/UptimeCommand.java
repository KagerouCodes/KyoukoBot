package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class UptimeCommand implements CommandExecutor {	
	@Command(aliases = {"k!uptime"}, description = "Cheesy admin-only command.", usage = "k!uptime", showInHelpPage = false)
    public String onCommand(DiscordAPI api, Message message, String args[])
	{
		return "`Uptime: " + KyoukoBot.msToTimeString(System.currentTimeMillis() - KyoukoBot.connect_time) + '`';
	}
}
