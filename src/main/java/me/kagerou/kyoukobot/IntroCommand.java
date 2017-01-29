package me.kagerou.kyoukobot;

import java.util.Map;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class IntroCommand implements CommandExecutor {
	final int MaxIntroLength = 200;
	@Command(aliases = {"k!intro", "k!introduce"}, description = "Lets you introduce yourself.", usage = "k!intro text")
    public String onCommand(DiscordAPI api, Message message, String args[])
	{
		if (args.length == 0)
		{
			//KyoukoBot.Database.set(message.getAuthor().getName(), "");
			KyoukoBot.Database.setNameAndIntro(message.getAuthor().getId(), message.getAuthor().getName(), "");
			return "Forgot about you >_<";
		}
		String intro = message.getContent().substring(message.getContent().indexOf(' ') + 1).trim().replace('\n', ' ');
		if (intro.length() > MaxIntroLength)
			intro = intro.substring(0, MaxIntroLength);
		//KyoukoBot.Database.set(message.getAuthor().getName(), intro);
		KyoukoBot.Database.setNameAndIntro(message.getAuthor().getId(), message.getAuthor().getName(), intro);
		Map.Entry<String, NewDataBase.Person> unIDedMatch;
		if ((unIDedMatch = KyoukoBot.Database.findUnIDedEntry(message.getAuthor().getName())) != null)
			KyoukoBot.Database.removeEntry(unIDedMatch.getKey());
		return "Nice to meet you~";
	}
}
