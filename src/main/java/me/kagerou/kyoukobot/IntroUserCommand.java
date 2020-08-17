package main.java.me.kagerou.kyoukobot;

import java.util.Map;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//lets the bot owner introduce a user
public class IntroUserCommand implements CommandExecutor {
	final int MaxIntroLength = 200;
	@Command(aliases = {"k!introuser"}, description = "Cheesy admin-only command.", usage = "k!introuser name@ text", requiredPermissions = "admin", showInHelpPage = false)
    public String onCommand(DiscordAPI api, Message message, String args[])
	{
		if (args.length == 0)
			return "Not enough arguments!";
		String msg = message.getContent();
		if (msg.indexOf('@') == -1)
			return "Usage: k!introuser name@ text"; //very awkward interface but it'll do; not even supporting mentions for now
		String name = msg.substring("k!introuser".length(), msg.indexOf('@')).toLowerCase().trim();
		String intro = msg.substring(msg.indexOf('@') + 1).trim();
		if (intro.length() > MaxIntroLength) //still cutting the intro if it's too long
			intro = intro.substring(0, MaxIntroLength);
		//KyoukoBot.Database.set(name, intro);
		Map.Entry<String, DataBase.Person> entry;
		if ((entry = KyoukoBot.Database.findIDedEntry(name)) != null) //if there is an entry with the featured name, change it (IDed one takes the priority)
			KyoukoBot.Database.setNameAndIntro(entry.getKey(), entry.getValue().name, intro);
		else
			if ((entry = KyoukoBot.Database.findUnIDedEntry(name)) != null)
				KyoukoBot.Database.setNameAndIntro(entry.getKey(), entry.getValue().name, intro);
			else //if there are no relevant entries, create one
				KyoukoBot.Database.setNameAndIntro(KyoukoBot.Database.firstNegativeID(), name, intro);
		if (intro.isEmpty())
			return "Forgot about " + name + ".";
		return "So, that's who " + name + " is!";
	}
}