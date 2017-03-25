package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//lets the user know the command is called "k!setintro" now; "k!intro" is shorter but people would accidentally misuse it instead of k!who all the time
public class IntroCommand implements CommandExecutor
{
	final int MaxIntroLength = 200;
	@Command(aliases = {"k!intro"}, description = "A redirect to k!setintro command.", showInHelpPage = false)
    public String onCommand(DiscordAPI api, Message message, String args[])
	{
		return "`This command has been renamed to k!setintro. Type k!help setintro or k!help who for further details.`";
	}
}
