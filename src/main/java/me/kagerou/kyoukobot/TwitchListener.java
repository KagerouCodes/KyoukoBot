package me.kagerou.kyoukobot;

import java.util.PriorityQueue;
import java.util.Random;
import java.util.function.Predicate;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;

class TwitchListener implements MessageCreateListener
{
	Random rnd;
	TwitchListener()
	{
		rnd = new Random(System.currentTimeMillis());
	}
	static int EmoteLimit = 3;
	
	boolean CheckSymbol(String str, int index, Predicate<Character> pred)
	{
		boolean result = (index < 0) || (index >= str.length()) || pred.test((str.charAt(index))); 
		return result;
	}
	
	class IndexPair implements Comparable<IndexPair>
	{
		int emote_index, symb_index;
		IndexPair(int emote_index, int symb_index)
		{
			this.emote_index = emote_index;
			this.symb_index = symb_index;
		}
		public int compareTo(IndexPair pair)
		{
			if (symb_index != pair.symb_index)
				return symb_index - pair.symb_index;
			return emote_index - pair.emote_index;
		}
	}
	
	public boolean react(DiscordAPI api, Message message)
	{
		String msg = message.getContent().toLowerCase();
		if (message.getAuthor().isBot() || msg.startsWith("k!"))
			return false;
		PriorityQueue<IndexPair> Indexes = new PriorityQueue<IndexPair>();
		for (int emote_index = 0; emote_index < KyoukoBot.Emotes.size(); emote_index++)
		{
			Emote emote = KyoukoBot.Emotes.get(emote_index);
			int index = -1;
			do {
				index = msg.indexOf(emote.name, index + 1);
				if ((index != -1) && CheckSymbol(msg, index - 1, (ch) -> !Character.isLetterOrDigit(ch)) &&
						CheckSymbol(msg, index + emote.name.length(), (ch) -> !Character.isLetterOrDigit(ch)) &&
						(CheckSymbol(msg, index - 1, (ch) -> ch != ':') ||
						CheckSymbol(msg, index + emote.name.length(), (ch) -> ch != ':')))
				{
					Indexes.add(new IndexPair(emote_index, index));
					break;
				}
			} while (index != -1);
		}
		int to_post = (Indexes.size() < EmoteLimit) ? Indexes.size() : EmoteLimit;
		if (to_post == 0)
			return false;
		for (int i = 0; i < to_post; i++)
		{
			IndexPair pair = Indexes.poll();
			if (KyoukoBot.Emotes.get(pair.emote_index).name.equals("kappa") && (rnd.nextInt(100) == 0))
				KyoukoBot.postFile(message, "http://i.imgur.com/JwmYhu7.png", "kappa");
			else
				KyoukoBot.postFile(message, KyoukoBot.Emotes.get(pair.emote_index).url, KyoukoBot.Emotes.get(pair.emote_index).name);
		}
		return true;
	}
	
	@Override
	public void onMessageCreate(DiscordAPI api, Message message) {
		react(api, message);
	}
}