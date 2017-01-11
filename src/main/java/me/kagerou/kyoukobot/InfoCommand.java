package me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class InfoCommand implements CommandExecutor {
	@Command(aliases = {"k!info"}, description = "Shows some information about me.")
	public String onCommand(Message message, String command, String[] args)
	{
		String result = "`I am Kyouko, a Discord bot written by Kagerou#4570 in Java. Current version: " + KyoukoBot.version +
				(KyoukoBot.release ? "" : " (beta)") + ". Type k!help to see the list of supported commands. " + 
				"If you want to test me, consider doing so in ";
		if (!message.isPrivateMessage())
			for (Channel channel: message.getChannelReceiver().getServer().getChannels())
			{
				//System.out.println(channel.getName());
				if (channel.getName().equalsIgnoreCase("bot-testing"))
				{
					result += " `" + channel.getMentionTag() + "` or ";
					break;
				}
			}
		result += "private chat ^_~\nMy owner accepts bug reports and feature requests!`";
		return result;
	}
}