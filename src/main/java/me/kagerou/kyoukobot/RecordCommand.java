package me.kagerou.kyoukobot;

import java.util.Collection;

import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

@Deprecated
public class RecordCommand implements CommandExecutor {
	@Command(aliases = {"k!record", "k!rec"}, usage = "k!rec[ord] post", description = "Reposts into #recordings channel.")
    public void onCommand(Message message, String args[]) {
		if (message.getAuthor().isBot())
			return;
		Channel recordings = null;
		if (!message.isPrivateMessage())
        {
			if (message.getChannelReceiver().getName().equals("recordings"))
				return;
        	Collection<Channel> col = message.getChannelReceiver().getServer().getChannels();
        	for (Channel ch: col)
        	{
        		if (ch.getName().equals("recordings"))
        		{
        			recordings = ch;
        			break;
        		}
        	}
        }
		if (recordings == null)
		{
			message.reply("`No #recordings channel found.`");
			return;
        }
		if (args.length == 0)
		{
			message.reply("`Enter the message to repost.`");
			return;
		}
		String text = message.getContent().substring(message.getContent().indexOf(' ') + 1);
		recordings.sendMessage("[from " + message.getAuthor().getMentionTag() + "] " + text);
	}
}
