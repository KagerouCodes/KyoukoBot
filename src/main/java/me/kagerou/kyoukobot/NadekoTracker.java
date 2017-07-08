package me.kagerou.kyoukobot;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Iterables;

import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.User;

public class NadekoTracker extends TimerTask
{
	Timer timer;
	boolean active;
	User Nadeko, admin;
	Channel BotTesting;
	String NadekoID;
	String channelID;
	Pattern pat;
	static final long CheckDelay = 10 * 60 * 1000;//20 * 1000;
	static final long InitialDelay = 2 * 60 * 1000;//20 * 1000
	
	NadekoTracker(String NadekoID, String channelID, Timer timer, boolean active)
	{
		this.NadekoID = NadekoID;
		this.channelID = channelID;
		try {
			Nadeko = KyoukoBot.api.getUserById(NadekoID).get();
		}
		catch (Exception e)
		{
			Nadeko = null;
			System.out.println("Nadeko not found.");
			e.printStackTrace();
		}
		this.timer = timer;
		this.active = active;
		timer.schedule(this, InitialDelay, CheckDelay);
		pat = Pattern.compile("Type (.*) for flowers", Pattern.CASE_INSENSITIVE);
	}

	@Override
	public void run()
	{
		//((ImplDiscordAPI) KyoukoBot.api).getUserMap().remove(NadekoID);
		if (Nadeko == null)
			try {
				Nadeko = KyoukoBot.api.getUserById(NadekoID).get();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		if (Nadeko == null)
		{
			System.out.println("Nadeko not found.");
			active = false;
			return;
		}
		String game = Nadeko.getGame();
		System.out.println("Nadeko's current game: " + game);
		if (game == null)
		{
			System.out.println("Nadeko's game not found.");
			active = false;
			return;
			
		}
		Matcher match = pat.matcher(game);
		if (!match.matches())
			active = false;
		else
			if (!active)
			{
				String msg = "**Type " + match.group(1) + " to get Nadeko flowers!**";
				
				if (admin == null)
					admin = Iterables.find(KyoukoBot.api.getUsers(), (x) -> x.getId().equals(KyoukoBot.adminID), null);
				if (admin != null)
					admin.sendMessage(msg);
				else
					System.out.println("Couldn't find the owner.");
				
				if ((BotTesting == null) && (channelID != null) && !channelID.isEmpty())
					BotTesting = KyoukoBot.api.getChannelById(channelID);
				if (BotTesting != null)
					BotTesting.sendMessage(msg);
				else
					System.out.println("Couldn't find the bot testing channel.");
				active = true;
			}
	}

}
