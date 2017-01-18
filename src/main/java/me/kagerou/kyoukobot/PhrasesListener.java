package me.kagerou.kyoukobot;

import java.util.ArrayList;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;

public class PhrasesListener implements MessageCreateListener {
	
	enum PhraseType //can't put it inside Phrase, wut??
	{
		EXACT, START, CONTAINS;
	}
	
	class Reaction
	{
		String value, response;
		PhraseType type;
		Reaction(String value, PhraseType type, String response)
		{
			this.value = value.toLowerCase();
			this.type = type;
			this.response = response;
		}
	}
	
	private ArrayList<Reaction> reactions;
	
	PhrasesListener()
	{
		reactions = new ArrayList<Reaction>();
		reactions.add(new Reaction("wake me up", PhraseType.EXACT, "Wake me up inside!"));
		reactions.add(new Reaction("wake me up!", PhraseType.EXACT, "Wake me up inside!"));
		reactions.add(new Reaction("i can't wake up", PhraseType.EXACT, "Wake me up inside!"));
		reactions.add(new Reaction("i can't wake up!", PhraseType.EXACT, "Wake me up inside!"));
		reactions.add(new Reaction("i cant wake up", PhraseType.EXACT, "Wake me up inside!"));
		reactions.add(new Reaction("i cant wake up!", PhraseType.EXACT, "Wake me up inside!"));
		
		//reactions.add(new Reaction(":Dab:", PhraseType.START, ":Dab2:"));
		//reactions.add(new Reaction(":Dab2:", PhraseType.START, ":Dab:"));
		reactions.add(new Reaction("<:Dab:271137562494500864>", PhraseType.START, "<:Dab2:271137400837767168>"));
		reactions.add(new Reaction("<:Dab2:271137400837767168>", PhraseType.START, "<:Dab:271137562494500864>"));
	}
	public boolean matches(String str, Reaction r)
	{
		str = str.toLowerCase();
		switch (r.type)
		{
			case EXACT:
				return str.equals(r.value);
			case START:
				return str.startsWith(r.value);
			case CONTAINS:
				return str.contains(r.value);
			default:
				return false; //compiler, please
		}
	}
	
	public boolean react(DiscordAPI api, Message message)
	{
		String content = message.getContent();
		boolean result = false;
		for (Reaction r: reactions)
			if (matches(content, r))
			{
				message.reply(r.response);
				result = true;
			}
		return result;
		/*if (!content.equalsIgnoreCase("wake me up") && !content.equalsIgnoreCase("wake me up!") &&
				!content.equalsIgnoreCase("i can't wake up") && !content.equalsIgnoreCase("i can't wake up!") &&
				!content.equalsIgnoreCase("i cant wake up") && !content.equalsIgnoreCase("i cant wake up!"))
			return false;
		message.reply("Wake me up inside!");
		return true;*/
	}

	@Override
	public void onMessageCreate(DiscordAPI api, Message message) {
		react(api, message);
	}

}
