package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import de.btobastian.sdcf4j.CommandHandler;

public class ExtraListener implements MessageCreateListener { //Twitch emotes + wrong commands + easter eggs
	TwitchListener twitchListener;
	EvanescenceListener evaListener;
	WrongCommandListener wrongListener;
	
	ExtraListener(CommandHandler handler)
	{
		twitchListener = new TwitchListener();
		wrongListener = new WrongCommandListener(handler);
		evaListener = new EvanescenceListener();
	}
	
	@Override
	public void onMessageCreate(DiscordAPI api, Message message) {
		if (!twitchListener.react(api, message) & !evaListener.react(api, message))
			wrongListener.onMessageCreate(api, message);
	}
}
