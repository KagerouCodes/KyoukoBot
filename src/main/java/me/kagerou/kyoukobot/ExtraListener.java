package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import de.btobastian.sdcf4j.CommandHandler;
//Twitch emotes + wrong commands + easter eggs
public class ExtraListener implements MessageCreateListener
{
	TwitchListener twitchListener;
	PhrasesListener phrasesListener; //this one is for easter eggs
	WrongCommandListener wrongListener;
	RecordingsListener recordingsListener;
	
	static String RecordingsIDBeta = "256392588272074753";
	static String RecordingsID = "253167649154924544";
	
	ExtraListener(CommandHandler handler)
	{
		twitchListener = new TwitchListener();
		wrongListener = new WrongCommandListener(handler);
		phrasesListener = new PhrasesListener();
		recordingsListener = new RecordingsListener(KyoukoBot.release ? RecordingsID : RecordingsIDBeta);
	}
	
	@Override
	public void onMessageCreate(DiscordAPI api, Message message) {
		if (!message.getAuthor().isBot()) //react does the same thing as onMessageCreate but returns true/false based on whether the listener needed to respond
			if (!recordingsListener.react(api, message) && (!twitchListener.react(api, message) & !phrasesListener.react(api, message)))
				wrongListener.onMessageCreate(api, message); //only check for a wrong command if there were no Twich emotes or phrases in the message
	}
}
