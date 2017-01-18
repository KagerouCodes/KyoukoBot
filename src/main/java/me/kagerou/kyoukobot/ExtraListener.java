package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import de.btobastian.sdcf4j.CommandHandler;

public class ExtraListener implements MessageCreateListener { //Twitch emotes + wrong commands + easter eggs
	TwitchListener twitchListener;
	PhrasesListener phrasesListener;
	WrongCommandListener wrongListener;
	
	ExtraListener(CommandHandler handler)
	{
		twitchListener = new TwitchListener();
		wrongListener = new WrongCommandListener(handler);
		phrasesListener = new PhrasesListener();
	}
	
	@Override
	public void onMessageCreate(DiscordAPI api, Message message) {
		if (!message.getAuthor().isBot())
			if (!twitchListener.react(api, message) & !phrasesListener.react(api, message))
				wrongListener.onMessageCreate(api, message);
	}
}
