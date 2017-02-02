package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class RemindMeCommand implements CommandExecutor { //TODO listing and canceling personal alarms??
	@Command(aliases = {"k!remindme", "k!remind", "k!alarm"}, description = "Reminds you about things.", usage = "k!remindme delay [message]\nDelay is set in seconds.", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, Server server, String args[])
    {
		if (!message.getAuthor().getId().equals(KyoukoBot.adminID))
		{
			message.reply("Y-you're touching me inappropriately!");
			return;
		}
		long seconds = -1;
		String[] splitContent = message.getContent().split("\\s+", 3);//.split(" ", 3);
		if (splitContent.length == 1)
		{
			message.reply("`Enter the delay (in seconds).`");
			return;
		}
		try {
			seconds = Long.parseLong(splitContent[1]);
		}
		catch (NumberFormatException e)
		{
			message.reply("`Enter the delay (in seconds).`");
			return;
		}
		if (seconds < 0)
		{
			message.reply("`Delay should be a positive number.`");
			return;
		}
		long alarmTime = System.currentTimeMillis() + seconds * 1000;
		String msg;
		if (splitContent.length == 3)
			msg = splitContent[2];
		else
			msg = "Alarm!";
		KyoukoBot.Database.registerReminder(message.getAuthor(), msg, alarmTime, KyoukoBot.timer, true);
		message.reply("Scheduled an alarm in " + seconds + " seconds!");
	}
}
