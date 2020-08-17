package main.java.me.kagerou.kyoukobot;

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
import main.java.me.kagerou.kyoukobot.TatsumakiRequest.RequestType;
//the class is used to index requests for Tatsumaki's responses by the username, Discord channel and their type (t!rep or t!daily) 
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
	//next two functions are needed for this class to be a valid key in a TreeMap
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

//this class is a task the only purpose of which is to exist until the response from Tatsumaki is received or the task expires
//there is probably a better way to do this
class TatsumakiWaiter extends TimerTask
{
	User user;
	boolean active = true; //it escapes me how there's nothing like isActive() function in the TimerTask class 
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
//listens for t!daily and t!rep commands and Tatsumaki's responses for them
public class TatsumakiListener implements MessageCreateListener {
	private static String TatsumakiID; //Tatsumaki's user ID
	TreeMap<TatsumakiRequest, TatsumakiWaiter> WaitingRoom = null; //a task is stored here when a t!daily/t!rep command was invoked but not handled yet 
	Timer timer;
	static final long WaiterDelay = 30000;
	
	TatsumakiListener(String id, TreeMap<TatsumakiRequest, TatsumakiWaiter> WaitingRoom, Timer timer)
	{
		TatsumakiID = id;
		this.WaitingRoom = WaitingRoom;
		this.timer = timer;
	}
	//checks if a message fits Tatsumaki's command (first symbol in the command name is case-sensitive, the others aren't)
	boolean fitsTatsumakiCommand(String msg, String command)
	{
		return msg.toLowerCase().startsWith(command.toLowerCase()) && (msg.charAt(0) == command.charAt(0)) &&
				((msg.length() == command.length()) || (msg.charAt(command.length()) == ' '));  
	}
	//checks if a user with a specified id is online on a receiver, be it a channel or a DM session
	boolean userIsOnReceiver(String id, MessageReceiver receiver)
	{
		if (receiver instanceof User)
			//return id.equals(receiver.getId());
			return false; //Kyouko is not going to chat with Tatsumaki, lol
		if (receiver instanceof Channel) //sanity check, should always be true
		{
			Channel channel = (Channel) receiver;
			User user;
			return ((user = channel.getServer().getMemberById(id)) != null) && (user.getStatus() != UserStatus.OFFLINE); //not sure if i need to check for offline status
		}
		return false;
	}
	//extracts the username from Tatsumaki's message
	//in regex, it would be something like matching (?:\*\*([^@]+)\*\*[^@]*\*\*[^@]*\*\*)|(?:\*\*([^@]+?) has given <@)
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
	//parses seconds left until the next t!daily/t!rep command from a Tatsumaki's message 
	long parseTatsumakiDelay(String content, RequestType type)
	{
		if (content.endsWith("you can award a reputation point!**"))
			return -1; //could've returned 0 but that would activate a new alarm
		//if (content.endsWith("200 daily credits!**") || content.endsWith("a reputation point!**"))
		if (content.endsWith("200 daily credits!**") || content.contains(" has given <@"))
			return 86400000; //seconds in a day
		try { //a perfect task for regex like ".+in (\d+) hours, (\d+) minutes and (\d+) seconds" 
			int timeStart = content.lastIndexOf("in ") + 3;
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
	
	//if t!daily/t!rep command from a subbed user is detected, generate a request and a waiter in the "waiting room"
	//if a relevant Tatsumaki's message is detected, check if there is an active waiter for it and handle it
	@Override
	public void onMessageCreate(DiscordAPI api, Message message)
	{
		String id = message.getAuthor().getId();
		String content = message.getContent();
		if (id.equals(TatsumakiID)) //handling Tatsumaki's messages
		{
			System.out.println("Tatsumaki's message: " + content); //some debug info
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
			//check is a request for that message is in the "waiting room" and still active
			TatsumakiRequest request = new TatsumakiRequest(name, message.getChannelReceiver(), type);
			TatsumakiWaiter waiter = WaitingRoom.get(request);
			if ((waiter != null) && waiter.active)
			{
				long delay = parseTatsumakiDelay(content, type);
				if (delay == -1) //sanity check, also is true if the message just says a user can award a rep point
				{
					System.out.println("Failed to parse Tatsumaki's message >_<");
					return;
				}
				waiter.cancel();
				if (type == RequestType.DAILY)
					KyoukoBot.Database.setDailyDelay(waiter.user, delay, timer);
				else
					KyoukoBot.Database.setRepDelay(waiter.user, delay, timer);
				WaitingRoom.remove(request); //hopefully, this works out
			}
			else
				if (waiter == null)
					System.out.println("No waiter found.");
				else
					System.out.println("Waiter wasn't active.");
		}
		else //handling non-Tatsumaki messages, putting waiters in the room if needed
			if (fitsTatsumakiCommand(content, "t!daily") && userIsOnReceiver(TatsumakiID, message.getReceiver()) && KyoukoBot.Database.isSubscribed(message.getAuthor()))
			{ //t!daily commands for subscribed users if Tatsumaki is present
				TatsumakiWaiter waiter = new TatsumakiWaiter(message.getAuthor());
				TatsumakiRequest request = new TatsumakiRequest(message.getAuthor().getName(), message.getChannelReceiver(), RequestType.DAILY);
				WaitingRoom.put(request, waiter);
				timer.schedule(waiter, WaiterDelay);
			}
			else
				if (fitsTatsumakiCommand(content, "t!rep") && userIsOnReceiver(TatsumakiID, message.getReceiver()) && KyoukoBot.Database.isSubscribed(message.getAuthor()))
				{ //t!rep commands for subscribed users if Tatsumaki is present
					TatsumakiWaiter waiter = new TatsumakiWaiter(message.getAuthor());
					WaitingRoom.put(new TatsumakiRequest(message.getAuthor().getName(), message.getChannelReceiver(), RequestType.REP), waiter);
					timer.schedule(waiter, WaiterDelay);
				}
	}
}
