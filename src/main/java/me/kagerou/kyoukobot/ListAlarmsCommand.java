package me.kagerou.kyoukobot;

import java.util.TreeMap;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import me.kagerou.kyoukobot.NewDataBase.Person;

public class ListAlarmsCommand implements CommandExecutor {
	@Command(aliases = {"k!alarms", "k!list"}, description = "Lists your alarms.", requiredPermissions = "admin", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, Server server, String args[])
    {
		Person person = KyoukoBot.Database.get(message.getAuthor().getId());
		TreeMap<String, RemindTask> alarms;
		if (person != null)
			alarms = person.alarms;
		else
			alarms = new TreeMap<String, RemindTask>();
		StringBuilder result = new StringBuilder();
		for (String msg: alarms.keySet())
		//for (Map.Entry<String, RemindTask> entry: alarms.entrySet())
			//result.append("\n`").append(entry.getKey()).append('`').append(" in ").append((entry.getValue().getTime() - System.currentTimeMillis()) / 1000).append(" seconds.");
			result.append("\n`").append(msg).append('`').append(" in ").append(KyoukoBot.msToTimeString(KyoukoBot.Database.getTimeLeft(message.getAuthor().getId(), msg)));
		if (result.length() == 0)
			result.append("`You have no alarms set up.`");
		else
			result.insert(0, "**Your alarms:**\n");
		message.reply(result.toString());
    }
}
