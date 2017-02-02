package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.UserStatus;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageReceiver;
import de.btobastian.javacord.listener.message.MessageCreateListener;

public class TatsumakiListener implements MessageCreateListener {
	private static String TatsumakiID;
	TatsumakiListener(String id)
	{
		TatsumakiID = id;
	}
	
	boolean fitsTatsumakiCommand(String msg, String command)
	{
		return msg.toLowerCase().startsWith(command.toLowerCase()) && (msg.charAt(0) == command.charAt(0)) && ((msg.length() == command.length()) || (msg.charAt(command.length()) == ' '));  
		/*if (!msg.toLowerCase().startsWith(command))
			return false;
		if (msg.charAt(0) != command.charAt(0))
			return false;
		if (msg.length() == command.length())
			return true;
		return (msg.charAt(command.length()) == ' ');*/
	}
	
	boolean userIsOnReceiver(String id, MessageReceiver receiver)
	{
		if (receiver instanceof User)
			//return id.equals(receiver.getId());
			return false; //Kyouko is not going to chat with Tatsumaki, lol
		if (receiver instanceof Channel) //sanity check, should always be true
		{
			Channel channel = (Channel) receiver;
			User user;
			return ((user = channel.getServer().getMemberById(id)) != null) && (user.getStatus() != UserStatus.OFFLINE);
		}
		return false;
	}
	
	@Override
	public void onMessageCreate(DiscordAPI api, Message message)
	{
		String id = message.getAuthor().getId();
		String content = message.getContent();
		if (id.equals(TatsumakiID))
		{
			//TODO
		}
		else
			if (fitsTatsumakiCommand(content, "t!daily") && userIsOnReceiver(TatsumakiID, message.getReceiver()))
			{
				//TODO initiate waiting for relevant Tatsumaki's message on a server for 5 seconds
			}
			else
				if (fitsTatsumakiCommand(content, "t!rep") && userIsOnReceiver(TatsumakiID, message.getReceiver()))
				{
					//TODO initiate waiting for relevant Tatsumaki's message on a server for 5 seconds
				}
	}
}
