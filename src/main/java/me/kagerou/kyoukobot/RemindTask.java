package me.kagerou.kyoukobot;

import java.util.Random;
import java.util.TimerTask;

import de.btobastian.javacord.DiscordAPI;
//a TimerTask for personal alarms
public class RemindTask extends TimerTask
{
	private String id, message;
	private DiscordAPI api;
	private NewDataBase database;
	private long time;
	RemindTask(String id, DiscordAPI api, String message, long time, NewDataBase database)
	{
		this.id = id;
		this.api = api;
		this.database = database;
		this.time = time;
		if (message.isEmpty())
			this.message = "Alarm!";//"Alarm " + Integer.toString(new Random().nextInt(999999999));
		else
			this.message = message;
	}
	public void run()
	{
		try { //try to send the message
			api.getUserById(id).get().sendMessage(message).get();
			System.out.println("Sent the scheduled message to the user " + id + ".");
			database.removeReminder(this);
		}
		catch (Exception e)
		{ //if it's not sent, postpone it by 5 seconds
			System.out.println("Failed to send the scheduled message to the user " + id + "."); //alarms don't trigger after i close and open my laptop >_<
			this.time = Math.max(time + 5000, System.currentTimeMillis());
			KyoukoBot.Database.refreshReminder(this, KyoukoBot.timer); //whoops, might be the wrong timer
		}
	}
	public String getId() {
		return id;
	}
	public String getMessage() {
		return message;
	}
	public long getTime() {
		return time;
	}
}