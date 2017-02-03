package me.kagerou.kyoukobot;

import java.util.TreeMap;
import java.util.Timer;
import java.util.TimerTask;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.UserStatus;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageReceiver;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import me.kagerou.kyoukobot.TatsumakiRequest.RequestType;

class TatsumakiRequest implements Comparable<TatsumakiRequest>
{
	enum RequestType
	{
		DAILY, REP;
	}
	private String name;
	private Channel channel;
	private RequestType type;
	
	TatsumakiRequest(String name, Channel channel, RequestType type)
	{
		this.name = name;
		this.channel = channel;
		this.type = type;
	}
	
	@Override
	public int compareTo(TatsumakiRequest tr)
	{
		int result = name.compareTo(tr.name);
		if (result != 0)
			return result;
		result = channel.getId().compareTo(tr.channel.getId());
		if (result != 0)
			return result;
		return type.compareTo(tr.type);
	}
	
	public boolean equals(TatsumakiRequest tr)
	{
		return name.equals(tr.name) && channel.getId().equals(tr.channel.getId()) && (type == tr.type);  
	}
}

class TatsumakiWaiter extends TimerTask
{
	User user;
	boolean active = true;
	public boolean cancel()
	{
		active = false;
		return super.cancel();
	}
	TatsumakiWaiter(User user)
	{
		this.user = user;
	}
	@Override
	public void run()
	{
		active = false; //pretty sure i'm doing this wrong
	} 
}

public class TatsumakiListener implements MessageCreateListener {
	private static String TatsumakiID;
	TreeMap<TatsumakiRequest, TatsumakiWaiter> WaitingRoom = null;
	Timer timer;
	static final long WaiterDelay = 5000;
	TatsumakiListener(String id, TreeMap<TatsumakiRequest, TatsumakiWaiter> WaitingRoom, Timer timer)
	{
		TatsumakiID = id;
		this.WaitingRoom = WaitingRoom;
		this.timer = timer;
	}
	
	boolean fitsTatsumakiCommand(String msg, String command)
	{
		return msg.toLowerCase().startsWith(command.toLowerCase()) && (msg.charAt(0) == command.charAt(0)) &&
				((msg.length() == command.length()) || (msg.charAt(command.length()) == ' '));  
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
	
	String parseTatsumakiName(String content)
	{
		try {
			int startingBold = content.indexOf("**");
			int endNickname = -1;
			if (content.contains(" has given <@")) //that one message that actually has only one bold tag, not two
				endNickname = content.indexOf(" has given <@"); 
			else
				endNickname = content.lastIndexOf("**", content.lastIndexOf("**", content.lastIndexOf("**") - 2) - 2);
			return content.substring(startingBold + 2, endNickname);
		}
		catch (Exception e)
		{
			return "";
		}
	}
	
	long parseTatsumakiDelay(String content, RequestType type)
	{
		if (content.endsWith("you can award a reputation point!**"))
			return -1;
		if (content.endsWith("200 daily credits!**") || content.endsWith("a reputation point!**"))
			return 86400000;
		try {
			int timeStart = content.indexOf("in ") + 3; //TODO learn the darn regex!
			//int timeEnd = content.length() - 3;
			final String hoursString = " hours, ";
			final String minutesString = " minutes and ";
			final String secondsString = " seconds";
			int hoursIndex = content.indexOf(hoursString, timeStart);
			long hours = Long.parseLong(content.substring(timeStart, hoursIndex));
			int minutesIndex = content.indexOf(minutesString, hoursIndex + hoursString.length());
			long minutes = Long.parseLong(content.substring(hoursIndex + hoursString.length(), minutesIndex));
			int secondsIndex = content.indexOf(secondsString, minutesIndex + minutesString.length());
			long seconds = Long.parseLong(content.substring(minutesIndex + minutesString.length(), secondsIndex));
			return (((hours * 60) + minutes) * 60 + seconds) * 1000;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}
	}
	
	@Override
	public void onMessageCreate(DiscordAPI api, Message message)
	{
		String id = message.getAuthor().getId();
		String content = message.getContent();
		if (id.equals(TatsumakiID))
		{
			System.out.println("Tatsumaki's message: " + content);
			TatsumakiRequest.RequestType type = null;
			if (content.startsWith("🏧"))
				type = RequestType.DAILY;
			else
				if (content.startsWith("🆙") && !content.endsWith(" leveled up!**"))
					type = RequestType.REP;
				else
				{
					System.out.println("The message was unrelated.");
					return;
				}
			System.out.println("The message has a type: " + type);
			String name = parseTatsumakiName(content);
			System.out.println("The message features a username: " + name);
			TatsumakiWaiter waiter = WaitingRoom.get(new TatsumakiRequest(name, message.getChannelReceiver(), type));
			if ((waiter != null) && waiter.active)
			{
				waiter.cancel();
				long delay = parseTatsumakiDelay(content, type);
				if (delay == -1) //sanity check
				{
					System.out.println("Failed to parse Tatsumaki's message >_<");
					return;
				}
				if (type == RequestType.DAILY)
					KyoukoBot.Database.setDailyDelay(waiter.user, delay, timer); //remove the darn waiter from the room!
				else
					KyoukoBot.Database.setRepDelay(waiter.user, delay, timer);
			}
			else
				if (waiter == null)
					System.out.println("No waiter found.");
				else
					System.out.println("Waiter wasn't active.");
		}
		else
			if (fitsTatsumakiCommand(content, "t!daily") && userIsOnReceiver(TatsumakiID, message.getReceiver()) && KyoukoBot.Database.isSubscribed(message.getAuthor()))
			{
				TatsumakiWaiter waiter = new TatsumakiWaiter(message.getAuthor());
				TatsumakiRequest request = new TatsumakiRequest(message.getAuthor().getName(), message.getChannelReceiver(), RequestType.DAILY);
				WaitingRoom.put(request, waiter);
				timer.schedule(waiter, WaiterDelay);
			}
			else
				if (fitsTatsumakiCommand(content, "t!rep") && userIsOnReceiver(TatsumakiID, message.getReceiver()) && KyoukoBot.Database.isSubscribed(message.getAuthor()))
				{
					TatsumakiWaiter waiter = new TatsumakiWaiter(message.getAuthor());
					WaitingRoom.put(new TatsumakiRequest(message.getAuthor().getName(), message.getChannelReceiver(), RequestType.REP), waiter);
					timer.schedule(waiter, WaiterDelay);
				}
	}
}
