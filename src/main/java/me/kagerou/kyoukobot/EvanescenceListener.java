package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;

public class EvanescenceListener implements MessageCreateListener {
	public boolean react(DiscordAPI api, Message message)
	{
		String content = message.getContent();
		if (!content.equalsIgnoreCase("wake me up") && !content.equalsIgnoreCase("wake me up!") &&
				!content.equalsIgnoreCase("i can't wake up") && !content.equalsIgnoreCase("i can't wake up!") &&
				!content.equalsIgnoreCase("i cant wake up") && !content.equalsIgnoreCase("i cant wake up!"))
			return false;
		message.reply("Wake me up inside!");
		return true;
	}

	@Override
	public void onMessageCreate(DiscordAPI api, Message message) {
		react(api, message);
	}

}
