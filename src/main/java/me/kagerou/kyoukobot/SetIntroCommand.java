package me.kagerou.kyoukobot;

import java.util.Map;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//lets the user fill in an introduction which would show up in k!who [all]
public class SetIntroCommand implements CommandExecutor
{
	final int MaxIntroLength = 200;
	@Command(aliases = {"k!setintro", "k!introduce"}, description = "Lets you describe yourself shortly. You and other people would see your info using the k!who command.\nUse this command with no text to erase your info.", usage = "k!setintro text")
    public String onCommand(DiscordAPI api, Message message, String args[])
	{
		String intro = KyoukoBot.getArgument(message, false).replace('\n', ' '); //not allowing newlines, those are too spammy
		//String intro = message.getContent().substring(message.getContent().indexOf(' ') + 1).trim().replace('\n', ' ');
		if (intro.length() > MaxIntroLength) //cut the intro if it's too long
			intro = intro.substring(0, MaxIntroLength);
		//KyoukoBot.Database.set(message.getAuthor().getName(), intro);
		//set the intro in the database, track the name just in case
		KyoukoBot.Database.setNameAndIntro(message.getAuthor().getId(), message.getAuthor().getName(), intro);
		//erase the database entry with no ID if there is one which matches the username
		Map.Entry<String, NewDataBase.Person> unIDedMatch;
		if ((unIDedMatch = KyoukoBot.Database.findUnIDedEntry(message.getAuthor().getName())) != null)
			KyoukoBot.Database.removeEntry(unIDedMatch.getKey());
		if (intro.isEmpty())
			return "Forgot about you >_<";
		return "Nice to meet you~";
	}
}
