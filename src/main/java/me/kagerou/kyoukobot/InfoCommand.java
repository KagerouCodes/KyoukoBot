package me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//a simple info command, TODO put a link to the github there
public class InfoCommand implements CommandExecutor {
	@Command(aliases = {"k!info"}, description = "Shows some information about me.")
	public String onCommand(Message message, Server server, String[] args)
	{
		String result = "`I am Kyouko, a Discord bot written by Kagerou#4570 in Java using Javacord. Current version: " + KyoukoBot.version +
				(KyoukoBot.release ? "" : " (beta)") + ". Type k!help to see the list of supported commands. " + 
				"If you want to test me, consider doing so in ";
		Channel channel; //invite the user into #bot-testing if there is such a channel
		if ((channel = KyoukoBot.findChannelByName("bot-testing", server)) != null)
			result += " ` " + channel.getMentionTag() + " ` or ";
		result += "private chat ^_~\nMy owner accepts bug reports and feature requests!`";
		return result;
	}
}