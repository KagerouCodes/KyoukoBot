package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;


public class IntroUserCommand implements CommandExecutor {
	final int MaxIntroLength = 200;
	@Command(aliases = {"k!introuser"}, description = "Cheesy admin-only command.", usage = "k!introuser name@ text", showInHelpPage = false)
    public String onCommand(DiscordAPI api, Message message, String args[])
	{
		if (!message.getAuthor().getId().equals(KyoukoBot.adminID))
			return "Y-you're touching me inappropriately!";
		if (args.length == 0)
			return "Not enough arguments!";
		String msg = message.getContent();
		if (msg.indexOf('@') == -1)
			return "Usage: k!introuser name@ text";
		String name = msg.substring("k!introuser".length(), msg.indexOf('@')).trim();
		String intro = msg.substring(msg.indexOf('@') + 1).trim();
		if (intro.length() > MaxIntroLength)
			intro = intro.substring(0, MaxIntroLength);
		KyoukoBot.Database.set(name, intro);
		if (intro.isEmpty())
			return "Forgot about " + name + ".";
		return "So, that's who " + name + " is!";
	}
}