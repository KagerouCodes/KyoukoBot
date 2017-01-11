package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class UptimeCommand implements CommandExecutor {
	static int[] divisors = {0, 24, 60, 60};
	static String[] time_units = {"day(s)", "hour(s)", "minute(s)", "second(s)"};
	@Command(aliases = {"k!uptime"}, description = "Cheesy admin-only command.", usage = "k!uptime", showInHelpPage = false)
    public String onCommand(DiscordAPI api, Message message, String args[])
	{
		long time = (System.currentTimeMillis() - KyoukoBot.connect_time) / 1000; //seconds
		long time_divided[] = new long[4];
		for (int i = 3; i >= 0; i--)
			if (divisors[i] != 0)
			{
				time_divided[i] = time % divisors[i];
				time /= divisors[i];
			}
			else
			{
				time_divided[i] = time;
				time = 0;
			}
		String result = "`Uptime:";
		for (int i = 0; i <= 3; i++)
			if (time_divided[i] != 0)
				result += " " + time_divided[i] + " " + time_units[i];
		if (result.length() == "`Uptime:".length())
			result += " 0 " + time_units[3];
		result += '`';
		return result;
	}
}
